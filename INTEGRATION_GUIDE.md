# Policy Engine Integration Guide

This guide details how to integrate microservices with the Policy Engine Control Plane. The platform supports two primary workflows:
1.  **Distributed Bootstrapping (Code-First)**: Teams define policies alongside their service code and bootstrap the Control Plane.
2.  **Centralized Management (UI-First)**: Policies are created and managed directly in the Control Plane.

---

## 1. Distributed Bootstrapping (Code-First)

In this model, policies are treated as code. They are version-controlled, tested, and deployed along with the microservice. Upon startup, the service "bootstraps" the Control Plane with its definitions.

### Advantages
- **Independence**: Teams own their default policies.
- **GitOps**: Policy versioning follows service versioning.
- **Consistency**: The Control Plane is always in sync with the deployed service.
- **Governance**: The Control Plane allows security teams to **override** these product policies without changing the service code.

### Step 1: Define `policy-manifest.yaml`

Create a `policy-manifest.yaml` in your service's resources directory (`src/main/resources`). This file defines your service identity, resource types, policies, bindings, and bundles.

```yaml
apiVersion: "v1"
service:
  name: "my-payment-service"
  version: "2.1.0"
  description: "Handles payment processing"
  # Optional: Public Key for verification
  publicKey: "..."

# Define the resources your service protects
resourceTypes:
  - name: "payment_transaction"
    description: "A financial transaction"
    attributes:
      - name: "amount"
        type: "number"

# Define your default policies (Product Policies)
policies:
  - name: "base-payment-policy"
    description: "Default rules for payments"
    file: "policies/payment.rego" # Path relative to classpath
    version: "1.0.0"

# Define how policies apply to resources (Bindings)
bindings:
  - name: "payment-protection"
    resourceType: "payment_transaction"
    context: "payments"
    evaluationMode: "ALL_MUST_ALLOW" # Options: ALL_MUST_ALLOW, ANY_ALLOW, DENY_OVERRIDES
    policies:
      - "base-payment-policy"

# Define the Bundle (The unit of distribution)
bundles:
  - name: "payment-bundle"
    targetService: "my-payment-service"
    refreshInterval: "60s"
    contexts:
      - "payments"
```

#### Manifest Reference

| Field | Description |
| :--- | :--- |
| **`service`** | Identity of the service. `name` must be unique. |
| **`resourceTypes`** | Definitions of resources protected by this service. Used for UI autocomplete. |
| **`policies`** | List of Rego policies. `file` is the classpath location. |
| **`bindings`** | Links policies to resources + contexts. |
| `bindings.evaluationMode` | Strategy for combining policies: `ALL_MUST_ALLOW` (Unanimous), `ANY_ALLOW` (One permits), `DENY_OVERRIDES` (Deny trumps Allow). |
| **`bundles`** | Grouping of contexts. This is what the client downloads. |
| `bundles.refreshInterval` | Suggestion to client on how often to check for updates (if polling). |

### Step 2: Integrate the Client Library

Add the `policy-engine-client` dependency to your project.

**Java (Maven):**
```xml
<dependency>
    <groupId>org.openpickles</groupId>
    <artifactId>policy-engine-client</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### Step 3: Initialize the Client

Configure and start the client on application startup using the `ClientConfig.Builder`.

```java
import org.openpickles.policy.engine.client.PolicyEngineClient;
import org.openpickles.policy.engine.client.ClientConfig;

@Configuration
public class PolicyConfig {

    @Bean(destroyMethod = "stop")
    public PolicyEngineClient policyClient() {
        ClientConfig config = ClientConfig.builder()
            .controlPlaneUrl("http://policy-engine:8080")
            .manifestPath("classpath:policy-manifest.yaml")
            .bundleName("payment-bundle") // Must match manifest
            .authHeader("Basic YWRtaW46YWRtaW4xMjM=") // Base64(admin:admin123)
            .transportType("WEBSOCKET") // or KAFKA, RABBITMQ
            .failFast(false) // Don't crash app if Control Plane is down
            .retryInitialInterval(2000)
            .build();
        
        PolicyEngineClient client = new PolicyEngineClient(config);
        
        // 1. Bootstrap: Sync manifest to Control Plane
        client.bootstrap(); 
        
        // 2. Start: Connect to WebSocket/Kafka for updates
        client.start();
        
        return client;
    }
}
```

---

## 2. Centralized Management (UI-First)

For ad-hoc policies, prototyping, or organization-wide governance rules, you can create policies directly in the Control Plane.

### Workflow
1.  **Access the Dashboard**: Navigate to `Policy Editor`.
2.  **Create Policy**: Write Rego code or upload a file.
    - Set Origin to **CUSTOM** (Administrative Override) or **PRODUCT**.
3.  **Define Binding**: Go to `Policy Bindings` and create a binding.
    - Select Resource Type (e.g., `payment_transaction`).
    - Select Context (e.g., `payments`).
    - Attach the Policy.
4.  **Manage Bundle**: Go to `Policy Bundles` and ensure the Bundle includes the Context (e.g., `payments`).

The connected services will automatically receive the update via WebSocket/Kafka without restarting.

### Policy Layering (Overrides)
One of the most powerful features of the Control Plane is the ability to override "Product" policies defined by teams.

- **Product Policy**: Defined in `policy-manifest.yaml`. Read-only in UI (typically).
- **Custom Policy**: Defined in Control Plane with same name + service owner.
- **Resolution**: The Control Plane merges them. Custom policies take precedence or can evaluate in a chain depending on the binding mode.

---

## 3. Distributed Architecture

The Policy Engine supports a **Federated** model where services can "own" bundles and "subscribe" to others.

### Concepts
- **Owner**: The service that defines the Bundle in its manifest.
- **Subscriber**: Any service that lists the Bundle in `subscribedBundles` (implicit in manifest via dependencies) or configured in the UI.

### Composite Bundles
When a service client requests a download (e.g., `payment-bundle`), the Control Plane performs a **Composite Build**:
1.  Identify the primary bundle.
2.  Identify all other bundles the service is subscribed to (e.g., `shared-audit-bundle`, `corporate-compliance-bundle`).
3.  Merge all bindings and policies.
4.  Generate a single WASM or Rego bundle for the client.
5.  Push the update to the client.

This allows a microservice to be secure by default (Product Policies) while enforcing global compliance (Shared Bundles) automatically.
