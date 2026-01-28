# Centralized Policy Engine

A robust, distributed authorization platform designed for microservices. It combines the flexibility of **Distributed Policy-as-Code** (Teams own their policies) with the governance of a **Centralized Control Plane**.

**[📖 Read the Integration Guide](INTEGRATION_GUIDE.md)** for detailed usage instructions.

![Dashboard](https://via.placeholder.com/800x400?text=Policy+Engine+Dashboard)

## Features

- **Centralized Policy Management**: Create, edit, and version OPA (Rego) policies.
- **Enhanced Authoring**: Professional editor (Monaco) with syntax highlighting and direct **File Upload** support.
- **GitOps Integration**: Sync policies directly from **Git repositories** for version-controlled workflows.
- **Entitlement Management**: Define fine-grained access control rules (User/Role/Group) with **Server-Side Pagination** and **Search**.
- **Distributed Policy-as-Code**: Teams develop policies alongside their service code. The Control Plane automatically bootstraps these definitions via `policy-manifest.yaml`.
- **Hybrid Management**: Support for both **Code-First** (Bootstrapping) and **UI-First** (Direct Creation) policy workflows.
- **Layered Customization**: Security teams can overrides Product policies (defined by devs) with Custom policies using a safe "Copy-on-Write" model.
- **Resource Provider Integration**: Register and manage microservices with **Dynamic Filter Schema** support.
- **Dynamic Bundle Download**: Download policies tailored to specific resource types (e.g., `?resourceTypes=DOCUMENT`).
- **Policy Metadata**: Policies now support `description` and `filename` metadata for better organization.
- **Flexible Policy Binding**: Support for **PBC Model** (RBAC -> ABAC -> ReBAC), **RBAC Only**, and **Direct** evaluation modes.
- **Real-time Updates**: Instant policy propagation via **WebSocket**, **Kafka**, or **RabbitMQ**.
- **Shared Bundles & Federation**: Support for **Shared Bundles** where services can define ("own") bundles and subscribe to others. The Control Plane merges all subscribed bundles into a single composite configuration for the client ("Composite Download").
- **Modern & Consistent UI**: Standardized "DataGrid" and "SlideOver" components across all listing pages.

## Federated Policy Model

The Policy Engine now supports a **Federated Model** for policy distribution:

1.  **Ownership**: A service that defines a bundle in its `policy-manifest.yaml` becomes the **Owner**. It controls the bundle's `contexts` and configuration.
2.  **Subscription**: Other services can include the same bundle name in their manifest to **Subscribe**. They will receive the policies but cannot modify the bundle definition.
3.  **Composite Download**: When a service requests its configuration, the Control Plane aggregates all bundles it owns or subscribes to into a single, seamless download.

### Example Manifest (`policy-manifest.yaml`)
```yaml
bundles:
  - name: "my-service-bundle"       # Private/Owned bundle
    targetService: "my-service"
    contexts: ["finance", "payments"]
  - name: "shared-compliance-bundle" # Shared/Subscribed bundle
    targetService: "compliance-service" # Defined by another service
    contexts: ["audit-logs"]
```

## Testing

### 1. Backend Unit Tests
Run backend tests using Maven (JUnit 5):
```bash
cd backend
mvn test
```

### 2. Frontend Unit Tests
Run frontend component tests using Vitest:
```bash
cd frontend
npm test
```

### 3. End-to-End (E2E) Tests
We use **Playwright** for full system testing, covering critical user flows.

**Prerequisites:**
- Backend running on `http://localhost:8080`.
- Frontend running on `http://localhost:5173`.

**Test Suites:**
- **Authentication**: `tests/auth.spec.js` - Login, Logout, invalid credentials.
- **User Management**: `tests/users.spec.js` - Create/List/Delete Users, Roles, Groups.
- **Resources**: `tests/resources.spec.js` - Create/Validate Resource Types.
- **Policies**: `tests/policy-editor.spec.js` - Create Policy, Syntax Check, Git Push simulation.
- **Bundles**: `tests/bundles.spec.js` - Create Bundle, Trigger Build.

**Run Tests:**
```bash
cd frontend
npx playwright test
```
*Note: Tests will run using default credentials (`admin`/`admin123`). To run verify against custom credentials, ensure `TEST_USERNAME` and `TEST_PASSWORD` env vars are set matches the backend.*

*Tip: Use `npx playwright show-report` to view detailed HTML test results including traces and videos of failures.*

### 4. Client Library Integration Tests
Verifies the full lifecycle of the **Java Client Library** (`policy-engine-client`) using a mock consumer app.

**Architecture:**
- **Client Library**: The JAR being tested. Handles WebSocket subscription and HTTP downloads.
- **Reference App**: A mock Spring Boot app (`policy-engine-reference-app`) acting as the consumer.
- **Test Driver**: `test-client-integration.sh` script that acts as the test runner and verifier.

**Run Integration Suite:**
```bash
./test-client-integration.sh
```

**What it does:**
1. Checks if Backend is UP.
2. Builds the `policy-engine-reference-app` (simulating a client service).
3. Starts the Reference App (Driver) on port `9090`.
4. Creates a dummy Policy and Bundle on the Backend.
5. Triggers a **Remote Build** via API.
6. Verifies the Client received the WebSocket notification and downloaded the bundle (Authenticated).

**Troubleshooting:**
- Check `driver.log` for client-side logs (Auth headers, WebSocket frames).
- Check `backend.log` for delivery logic.

### 5. CI/CD Workflow
This project includes a GitHub Actions workflow `.github/workflows/quality-check.yml` that automatically runs on every Push and Pull Request to `main`.

**Pipeline Stages:**
1. **Backend Unit Tests**: `mvn test`
2. **Frontend Unit Tests**: `npm test`
3. **End-to-End Tests**: Boots the full backend and runs `npx playwright test`.


## Tech Stack

- **Backend**: Java 17, Spring Boot 3.3, H2 Database, Spring Security, JPA.
- **Frontend**: React 18, Vite, TailwindCSS, Lucide Icons.

## Client Configuration

The `policy-engine-client` can be configured via the `ClientConfig` builder key properties:

| Property | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `controlPlaneUrl` | String | - | URL of the Policy Engine Control Plane (e.g., `http://localhost:8080`) |
| `manifestPath` | String | - | Path to `policy-manifest.yaml` (classpath: or file:) |
| `bundleName` | String | - | Name of the bundle to download (must match manifest) |
| `authHeader` | String | - | Authorization header value (e.g., `Bearer <token>` or `Basic <cred>`) |
| `failFast` | boolean | `false` | If true, throws exception if initial connection fails |
| `retryInitialInterval`| long | `2000` | Initial retry interval in ms |
| `retryMaxInterval` | long | `60000`| Maximum retry interval in ms |
| `retryMultiplier` | double | `2.0` | Multiplier for exponential backoff |

## Real-time Transport Configuration

The Policy Engine supports multiple transport mechanisms for broadcasting policy updates. The default is **WebSocket**, but **Kafka** and **RabbitMQ** are also fully supported.

### 1. WebSocket (Default)
Simple, direct connection to the Control Plane. No extra infrastructure required.
```yaml
policy:
  engine:
    transport:
      type: WEBSOCKET
```

### 2. Kafka
Production-grade durability and scale.
```yaml
policy:
  engine:
    transport:
      type: KAFKA
      kafka:
        topic: policy-updates
        bootstrap-servers: localhost:9092 # Set in ClientConfig
        group-id: my-service-group      # Set in ClientConfig
```

### 3. RabbitMQ
Standard AMQP messaging.
```yaml
policy:
  engine:
    transport:
      type: RABBITMQ
      rabbitmq:
        exchange: policy.updates
        host: localhost                 # Set in ClientConfig
        port: 5672                      # Set in ClientConfig
        username: guest                 # Set in ClientConfig
        password: guest                 # Set in ClientConfig
```

## API Overview

The Control Plane exposes a comprehensive REST API for management and integration.

| Category | Endpoint Base | Description |
| :--- | :--- | :--- |
| **Evaluation** | `/api/v1/evaluation` | Real-time policy evaluation requests |
| **Sync** | `/api/v1/sync` | Used by clients to bootstrap definitions (`policy-manifest.yaml`) |
| **Bundles** | `/api/v1/bundles` | Download policy bundles (WASM/Rego) |
| **Policies** | `/api/v1/policies` | CRUD operations for OPA policies |
| **Resources** | `/api/v1/resource-types` | Manage resource type definitions |
| **Services** | `/api/v1/services` | Registry of connected microservices |
| **Audit** | `/api/v1/audit` | Access policy evaluation logs |


## Getting Started

### Prerequisites

- Java 17+
- Node.js 18+
- Maven (wrapper included)

### Quick Start (Development)
We provide helper scripts for zero-config local development:

1.  **Start Backend** (builds & runs in background):
    ```bash
    ./start-dev.sh
    ```
    *   Starts on `http://localhost:8080`.
    *   Logs output to `backend/backend.log`.
    *   Uses default credentials: `admin` / `admin123`.

2.  **Stop Backend**:
    ```bash
    ./stop-dev.sh
    ```

### Configuration & Security

**Default Credentials (Local Dev):**
- Username: `admin`
- Password: `admin123`

**Production Overrides:**
To secure the application for production or custom environments, set the following environment variables:
```bash
export ADMIN_USERNAME=myuser
export ADMIN_PASSWORD=mypassword
```

### Running the Application Manually

#### 1. Integrated Build (Recommended for Production)
This will build both the frontend and backend, bundle them into a single JAR, and run it.

1.  Navigate to the `backend` directory:
    ```bash
    cd backend
    ```
2.  Clean and package (this automates the `npm build`):
    ```bash
    ./mvnw clean package
    ```
3.  Run the JAR:
    ```bash
    java -jar target/policy-engine-0.0.1.jar
    ```
    The application (UI and API) will be available at `http://localhost:8080`.

#### 2. Development Mode
Run frontend and backend separately for hot-reloading.

**Backend**:
1.  `cd backend`
2.  `./mvnw spring-boot:run` (Starts on `http://localhost:8080` with default admin credentials)

**Frontend**:
1.  `cd frontend`
2.  `npm install`
3.  `npm run dev` (Starts on `http://localhost:5173`)

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1.  Fork the repository.
2.  Create your feature branch (`git checkout -b feature/AmazingFeature`).
3.  Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4.  Push to the branch (`git push origin feature/AmazingFeature`).
5.  Open a Pull Request.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
