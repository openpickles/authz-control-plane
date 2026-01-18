# Design Document: Distributed Policy Sync

## 1. Executive Summary
This design introduces a **Distributed Policy Management** capability to the Policy Engine. It enables a "Push-Manage-Pull" workflow where domain services (Product applications) act as the source of truth for their default policies, while the Control Plane allows for centralized management and customization.

Crucially, it supports **Distributed Administration**, allowing Local Admins to view and modify policies directly from their Domain Application's interface, while ensuring the Central Control Plane remains the single system of record for all custom overrides.

The workflow consists of five key phases:
1.  **Definition**: Developers define policies, resource types, and bundles in a metadata file (`policy-manifest.yaml`).
2.  **Build Validation**: The build process validates this metadata using a Maven plugin.
3.  **Registration (Push)**: On startup, the application Client Library pushes the metadata to the Control Plane.
4.  **Management (Layering)**: Admins (Central or Local) review policies and add **Custom Layers** or override defaults.
5.  **Execution (Pull)**: The application pulls the final, effective policy bundle (merging Product defaults + Custom layers) for local execution.

---

## 2. Detailed Lifecycle Flow

### Step 1: Definition & Scaffolding
The **Developer** of a domain service creates a `policy-manifest.yaml` (manually or via scaffolding tool). This file defines:
-   **Resource Types**: The data schema (context) the policies operate on.
-   **Policies**: The default "Product" policies (e.g., business logic shipped with the app).
-   **Contexts**: The execution points (e.g., "http_ingress").
-   **Bundles**: Groups of contexts that this application intends to use.

### Step 2: Build-Time Integration
The Developer includes the **Control Plane Library** and configures the **Maven Plugin** in their `pom.xml`.
-   **Dependency**: `policy-engine-client`
-   **Plugin**: `policy-engine-maven-plugin`
-   **Action**: The plugin runs during the build (e.g., `process-resources`), validates the YAML syntax, checks against the schema, and ensures referenced Rego files exist.

### Step 3: Application Startup & Registration (Push)
When the application starts:
1.  The `PolicyEngineClient` auto-configures (if `openpickles.control-plane.enabled=true`).
2.  It reads the `policy-manifest.yaml` and all referenced Rego files.
3.  It **PUSHES** this bundle to the Control Plane (`POST /api/v1/dist/sync`).
    -   This registers the Application/Service in the Control Plane.
    -   It uploads the "Product Layer" of policies.
    -   It registers the Bundles defined in the manifest.

### Step 4: Control Plane Administration (Review & Layering)
Policies can be managed via the **Central Console** OR **Distributed Local Admin**:
1.  **Review**: Admins fetch the current policy state (Product + Custom) for a specific service (`GET /api/v1/policies?service=...`).
2.  **Customize**: Admins can submit a "Custom Layer" override (`POST /api/v1/policies/custom`).
    -   *Distributed Mode*: A Local Admin logging into the Payment Service Admin UI can see the "High Value" policy and tweak the threshold. The Payment Service proxies this request to the Control Plane.
    -   *Central Mode*: A Central Security Admin logs into the Control Plane directly to apply global overrides.
3.  **Binding**: The Control Plane acknowledges the Bundle requirements.

### Step 5: Policy Distribution & Execution (Pull)
1.  The Application (Client Library) polls or listens for updates.
2.  It **PULLS** the effective policy bundle.
    -   Effective Bundle = Product Defaults + Admin Custom Overrides.
3.  The application initializes its local Policy Engine with these policies.
4.  Traffic is evaluated locally against this synchronized policy set.

---

## 2. The Policy Manifest (`policy-manifest.yaml`)

This file is shipped within the domain service (e.g., `src/main/resources/policy-manifest.yaml`). It acts as the contract between the Manifest Source (Service) and the Control Plane.

### YAML Schema Definition

```yaml
# policy-manifest.yaml
apiVersion: "openpickles.org/v1"
service:
  name: "payment-service"
  description: "Core Payment Processing Service"
  version: "1.2.0" # Product version, used for drift detection

# ---------------------------------------------------------
# 1. Resource Type Definitions
# Defines the Data Schema available for policies.
# Kept separate because multiple bindings might use the same Resource Type.
# ---------------------------------------------------------
resourceTypes:
  - key: "payment_transaction"
    name: "Payment Transaction"
    description: "Represents a monetary transaction attempt"
    attributes:
      - name: "amount"
        type: "number"
        description: "Transaction amount in base units (e.g., cents)"
      - name: "currency"
        type: "string"
        description: "ISO 4217 Currency Code"
      - name: "merchant_id"
        type: "string"
        description: "ID of the merchant initiating the charge"
      - name: "risk_score"
        type: "number"
        description: "Pre-calculated risk score (0-100)"

# ---------------------------------------------------------
# 2. Flow-Centric Bindings
# Defines the Flows (Contexts) and the Policies that apply to them.
# The 'Context' and 'Policies' are defined inline for simplicity.
# ---------------------------------------------------------
bindings:
  - context: 
      key: "http_ingress"
      name: "HTTP Ingress"
      description: "Evaluated at the API Gateway / Entry filter"
    resourceType: "payment_transaction"
    mode: "PBC_CHAIN" # Default Execution Mode
    policies: 
      - name: "high-value-txn-check"
        description: "Flag transactions over 10k for audit"
        file: "policies/high_value.rego"

      - name: "merchant-blocklist"
        description: "Block specific blacklisted merchants based on ID"
        file: "policies/blocklist.rego"

  - context:
      key: "pre_authorization"
      name: "Pre-Authorization"
    resourceType: "payment_transaction"
    policies:
      - name: "fraud-check"
        file: "policies/fraud.rego"

# ---------------------------------------------------------
# 3. Bundle Configuration
# Requests bundles specifically for this service.
# Bundles are defined as a combination of one or more Contexts.
# ---------------------------------------------------------
bundles:
  - name: "payment-service-all"
    refresh_interval: "60s"
    contexts:
      - "http_ingress"
      - "pre_authorization"

  - name: "payment-service-ingress-only"
    refresh_interval: "300s"
    contexts:
      - "http_ingress"

---

## 3. Low-Level Design (LLD)

### 3.1 Backend Architecture (`policy-engine-backend`)

#### Data Model Updates

1.  **`Policy` Entity**:
    -   `origin` (Enum): `PRODUCT` | `CUSTOM` (Default: CUSTOM)
    -   `serviceOwner` (String): e.g., "payment-service" (from manifest).
    -   `isDirty` (Boolean): Flag to indicate if a User has modified a Product policy.

2.  **`ResourceType` Entity**:
    -   `attributes` (JSON/Structure): Storage for the attribute list defined in YAML.
    -   *Strategy*: Attributes are always overwritten by the "live" service schema to ensure the Policy Editor has the latest field definitions.

#### `SyncController` (New)
-   **Endpoint**: `POST /api/v1/dist/sync`
-   **Payload**: `ManifestSyncRequest` (DTO mapping of the YAML)
-   **Service Logic (`SyncService`)**:
    1.  **Ingest Resources**: Upsert Resource Types and Attributes. 
    2.  **Ingest Contexts**: Upsert Context definitions.
    3.  **Ingest Policies (Layering)**:
        -   *Match by Name*:
        -   **New Policy**: Create with `origin=PRODUCT`.
        -   **Existing (CUSTOM)**: Ignore update. Log "Skipped Product Update for Custom Policy".
        -   **Existing (PRODUCT)**: Update content. (Represents a Product Version Upgrade).
    4.  **Ingest Bindings**: 
        -   Ensure specified bindings exist. Add if missing. Do not remove extra bindings added by the user.
    5.  **Provision Bundles**: 
        -   Iterate through `request.bundles`.
        -   For each bundle definition, find all Bindings associated with the listed `contexts`.
        -   Create/Update `PolicyBundle` entity with the calculated list of Binding IDs.

#### `AdminController` (New - Distributed Management)
-   **Endpoint**: `GET /api/v1/policies`
    -   **Params**: `service={serviceName}`
    -   **Response**: List of policies with their `layer` status (PRODUCT vs CUSTOM).
    -   **Usage**: Used by Domain App Admin UI to display current policy configuration.

-   **Endpoint**: `POST /api/v1/policies/custom`
    -   **Payload**: `CustomPolicyRequest` (policyName, serviceName, regoContent)
    -   **Logic**:
        -   Finds the original PRODUCT policy (if exists).
        -   Creates/Updates a `Policy` record with `origin=CUSTOM`.
        -   This ensures the PRODUCT policy remains untouched as a "base", while the CUSTOM policy takes precedence in execution.

#### `BundleController` (Update)
-   **Endpoint**: `GET /api/v1/bundles/{name}/download?service={serviceName}`
-   **Purpose**: Used by the Client Library to pull the effective policy set.
-   **Logic**:
    1.  Look up the Bundle by `name` and requesting `serviceName`.
    2.  Resolve all policies.
    3.  **Apply Layers**: For each policy, check if a `CUSTOM` version exists. If so, use that; otherwise, use the `PRODUCT` limit.
    4.  Return the consolidated JSON bundle (compatible with OPA/Client).

### 3.2 Client Library Architecture (`policy-engine-client`)

#### `ManifestLoader`
-   **Function**: Scans classpath for `policy-manifest.yaml`.
-   **Parsing**: Uses Jackson YAML to parse into `ClientManifest` POJO.
-   **Content Resolution**: Resolves relative paths (`policies/high_value.rego`) to actual file content strings.

#### `PolicyEngineClient` Updates
-   **`bootstrap()` Method**:
    -   Called automatically on `ApplicationReadyEvent` (if configured).
    -   Loads Manifest.
    -   POSTs to `/api/v1/dist/sync`.
    -   Handles errors (Configurable: `FailFast` vs `LogAndContinue`).

---

## 4. Operational Workflow

1.  **Developer** updates `payment-service` code and modifies `high_value.rego`.
2.  **CI/CD** builds new version `1.2.1`.
3.  **Deployment** starts `payment-service` in Staging.
4.  **Startup**: Client library detects `policy-manifest.yaml`.
5.  **Phone Home**: Client POSTs manifest to Control Plane.
6.  **Control Plane**:
    -   Sees `high-value-txn-check` updates (Product origin).
    -   Updates the Policy content in DB.
    -   Updates Resource Type attributes if changed.
7.  **Snyk/Audit**: Control Plane now has the latest code. Admin can run Snyk scans on the new product policy.
8.  **Bundle Gen**: Service downloads the new bundle(s) containing the updated logic.

---


## 6. Concurrency & Multi-Instance Handling

When multiple instances of a Domain Service (e.g., `payment-service` running on 10 Kubernetes pods) start up simultaneously, they will all attempt to push the same `policy-manifest.yaml` to the Control Plane.

### 6.1 Idempotency Strategy
The `POST /api/v1/dist/sync` endpoint MUST be **Idempotent**.
-   **Logic**:
    1.  **Hash Check**: The Client calculates a hash of the manifest content (SHA-256).
    2.  **Request**: The Sync Request includes this `manifestHash` and the `serviceVersion`.
    3.  **Backend Processing**:
        -   The Backend checks if `payment-service` with `version=1.2.0` and `hash=...` has already been processed.
        -   **If Match**: Return `200 OK` (No-op).
        -   **If New/Changed**: Acquire a lock (e.g., Database Row Lock on `Service` record) -> Update Policies -> Release Lock.

### 6.2 Race Condition Handling
In the event of a race condition (millisecond difference):
-   **Database Constraint**: A unique constraint `(service_name, policy_name, origin='PRODUCT')` ensures only one write succeeds.
-   **Optimistic Failure**: If Instance B tries to write while Instance A is writing, Instance B may receive a `ConcurrentModificationException` (or specific SQL error).
-   **Client Retry**: The Client Library should silently catch `409 Conflict` or specific concurrency errors and assume success (since the goal was to ensure the state exists, and "Conflict" implies someone else just created it).

---

## 7. Frontend / User Experience Design

The Control Plane UI must be updated to reflect the Service-Centric nature of the system.

### 7.1 Service Dashboard (`/services`)
-   **View**: A list of all registered Domain Services.
-   **Columns**: Service Name, Version, Last Synced, Status (Healthy/Drifted).
-   **Action**: Clicking a service drills down into the **Service Detail View**.

### 7.2 Service Detail View (`/services/{name}`)
This view visualizes the mapping hierarchy: **Service -> Bundles -> Bindings -> Policies**.

#### A. Policy Overview Tab
-   **Tree View**:
    -   **Bundles**: Lists all bundles defined by the service (e.g., `payment-service-all`).
    -   **Bindings**: Under each bundle, shows the bindings (e.g., `http_ingress`).
    -   **Policies**: Under each binding, shows the list of active policies.
-   **Indicators**:
    -   Policies from **Product** layer are shown with a "Lock" icon (Immutable).
    -   Policies from **Custom** layer are shown with an "Edit" icon.

#### B. Customization Flow
**User Story**: "As an Admin, I want to override the `high-value-check` for the `payment-service`."

1.  **Select**: Admin navigates to `Service Detail` -> `Bindings` -> `high-value-check`.
2.  **Action**: Clicks "Customize" (or "Override").
3.  **Editor Modal**:
    -   **Left Pane**: Read-only view of the original `Product` policy (v1.2.0).
    -   **Right Pane**: Editable editor for the `Custom` policy.
    -   **Template**: Pre-filled with the Product policy content as a starting point.
4.  **Save**:
    -   Backend creates a new `Policy` version with `origin=CUSTOM`.
    -   The UI updates to show the Custom policy is now effective.
    -   The "Effective Policy" view highlights the diff.

---

## 8. Developer Experience & Guidelines

To ensure smooth adoption by service developers, the following practices are recommended:

### 8.1 IDE Support (Schema Validation)
To prevent YAML errors, we will publish a **JSON Schema** for the `policy-manifest.yaml`.
Developers should add this to their `package.json` or IDE settings:
```yaml
# In VS Code or IntelliJ: Map "policy-manifest.yaml" to:
# https://control-plane.openpickles.org/schemas/v1/policy-manifest.json
```

### 8.2 Convention over Configuration
-   **Policy Location**: Place all Rego files in `src/main/resources/policies/`.
-   **File Paths**: The manifest `file` path should be relative to `src/main/resources/` (e.g., `policies/auth.rego`).

### 8.3 Local Development (Disconnected Mode)
Developers need to verify policies without running a local Control Plane.
-   **Local Fallback**: If the Control Plane is unreachable or the app is in `dev` profile, the Client Library should **automatically use the embedded Rego files** locally.
-   **Mock Context**: A simple `@Test` helper should be provided to simulate context flows:
    ```java
    // Unit Test Example
    PolicyEngine.mockFlow("http_ingress", inputData)
               .expectDecision(Decision.ALLOW);
    ```

### 8.4 Versioning
-   **Service Version**: The `service.version` in YAML should ideally match the actual build version.
-   **Recommendation**: Use build templating (Maven/Gradle) to inject the version during build if possible, or use a "Git Hash" strategy to detect changes.

---

## 9. Developer Tooling Strategy (Solutions for Friction)

To proactively eliminate developer friction, we will provide the following tooling:

### 9.1 Build-Time Validation (Maven/Gradle Plugin)
**Problem**: Developers discover YAML errors only after deploying or starting the app.
**Solution**: A `policy-engine-maven-plugin` that runs during the `process-resources` phase.
-   **Validates** `policy-manifest.yaml` against the JSON Schema.
-   **Verifies** that referenced `.rego` files exist.
-   **Checks** Rego syntax (using `opa check` if available, or internal parser).

```xml
<plugin>
    <groupId>org.openpickles.policy</groupId>
    <artifactId>policy-engine-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals><goal>validate</goal></goals>
        </execution>
    </executions>
</plugin>
```

### 9.2 Policy Test Kit (`policy-engine-test`)
**Problem**: Unit testing policy integration is complex and requires full context setup.
**Solution**: A lightweight test library compatible with JUnit 5.
-   **Provides** `@PolicyTest` annotation.
-   **Injects** a local, in-memory Policy Engine.
-   **Loads** the local manifest automatically.

```java
@PolicyTest
class PaymentPolicyTest {
    @Test
    void testHighValueAudit(PolicyEngine engine) {
        var txn = new PaymentTxn(20000, "USD"); // High value
        var result = engine.evaluate("pre_authorization", txn);
        assertTrue(result.isAllowed());
    }
}
```

### 9.3 Scaffolding CLI (`policy:init`)
**Problem**: Creating the initial `policy-manifest.yaml` structure requires referring to documentation and is error-prone.
**Solution**: An interactive "Wizard" goal in the build plugin.
-   **Command**: `mvn policy:init` (or `gradle policyInit`)
-   **Interactive Prompts**:
    1.  "What is the service name?" (Default: ArtifactId)
    2.  "Do you want to define a Resource Type now? (y/n)"
    3.  "Define your first Flow/Context name (e.g., http_ingress):"
-   **Output**: Generates a valid `policy-manifest.yaml` and creates the `src/main/resources/policies/` directory.

