# Centralized Policy Engine

A reference implementation of a centralized authorization system for managing OPA policies and entitlements across microservices. Built with Spring Boot and React.

![Dashboard](https://via.placeholder.com/800x400?text=Policy+Engine+Dashboard)

## Features

- **Centralized Policy Management**: Create, edit, and version OPA (Rego) policies.
- **Enhanced Authoring**: Professional editor (Monaco) with syntax highlighting and direct **File Upload** support.
- **GitOps Integration**: Sync policies directly from **Git repositories** for version-controlled workflows.
- **Entitlement Management**: Define fine-grained access control rules (User/Role/Group) with **Server-Side Pagination** and **Search**.
- **Entitlement Sync**: Batch push/upsert entitlements from external domain services.
- **Resource Provider Integration**: Register and manage microservices with **Dynamic Filter Schema** support.
- **Dynamic Bundle Download**: Download policies tailored to specific resource types (e.g., `?resourceTypes=DOCUMENT`).
- **Policy Metadata**: Policies now support `description` and `filename` metadata for better organization.
- **Multi-Policy Binding**: Bind multiple policies to a single context, allowing modular policy composition.
- **Real-time Updates**: Instant policy propagation via **WebSocket**, **Kafka**, or **RabbitMQ**.
- **Modern & Consistent UI**: Standardized "DataGrid" and "SlideOver" components across all listing pages.

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

## Real-time Transport Configuration

The Policy Engine supports multiple transport mechanisms for broadcasting policy updates. The default is **WebSocket**, but **Kafka** and **RabbitMQ** are also fully supported.

**Configuration (`application.yml`):**
```yaml
policy:
  engine:
    transport:
      type: KAFKA # Options: WEBSOCKET (default), KAFKA, RABBITMQ
      kafka:
        topic: policy-updates # Default
      rabbitmq:
        exchange: policy.updates # Default
```

## Getting Started

### Prerequisites

- Java 17+
- Node.js 18+
- Maven (wrapper included)

### Running the Application

You can run the application in two ways:

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
2.  `./mvnw spring-boot:run` (Starts on `http://localhost:8080`)

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
