# OpenPickles Policy Engine

![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)
![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Version](https://img.shields.io/badge/version-1.0.0-orange)

## 📖 The "Why": Solving Policy Sprawl

In modern cloud-native architectures, **Authorization** and **Business Logic** often become the hardest components to manage at scale.

### The Problem
- **Fragmentation**: Policy logic (Validation, RBAC, ABAC) is scattered across codebases in `if/else` blocks or isolated Rego files.
- **Audit Nightmares**: "Who has access to the PII dataset?" becomes a question that requires grepping through 50 repositories.
- **deployment Bottlenecks**: Changing a simple business rule (e.g., "Lower the transaction limit to $5k for Bronze users") requires a code change, PR review, and redeployment of the service.
- **Governance Gaps**: Security teams have no way to enforce global constraints (e.g., "All APIs must be accessible only via TLS") without asking every team to update their code.

### The Solution: OpenPickles Policy Engine
OpenPickles is a **Centralized Control Plane** for **Distributed Policy Enforcement**. It is designed to bridge the gap between **Governance** (Security Teams) and **Agility** (Product Teams).

It allows you to:
1.  **Decouple Policy from Code**: Use [Open Policy Agent (OPA)](https://www.openpolicyagent.org/) and Rego for logic.
2.  **Centralize Visibility**: A single dashboard to view, edit, and audit policies across *all* services.
3.  **Enforce Dynamically**: Push policy updates (Security Patches, Business Rule Changes) to 1,000+ services in milliseconds via WebSocket/Kafka without restarting them.
4.  **Layered Control**: Developers define *Product Defaults*, but Security Admins can transparently *Override* them with *Global Compliance Rules*.

---

## 🏗 High-Level Architecture

The system operates on a **Hybrid Model**:

```mermaid
graph TD
    subgraph "Control Plane (Centralized)"
        UI[Admin Dashboard] --> API[Management API]
        Git[Git Repo (Policies)] -. Sync .-> API
        DB[(Database)] <--> API
    end

    subgraph "Data Plane (Distributed)"
        SvcA[Payment Service]
        SvcB[Order Service]
    end

    API -- "Push Updates (WebSocket/Kafka)" --> SvcA
    API -- "Push Updates (WebSocket/Kafka)" --> SvcB

    SvcA -- "1. Bootstrap (Manifest)" --> API
    SvcB -- "1. Bootstrap (Manifest)" --> API
```

1.  **Bootstrapping**: Services describe their own needs (Resources, Contexts, Default Policies) in a `policy-manifest.yaml`.
2.  **Sync**: On startup, services register with the Control Plane.
3.  **Governance**: Admins use the UI to review, modify, or override these policies.
4.  **Distribution**: The Engine compiles policies (Rego/WASM) into **Bundles** and pushes them to services.

---

## 🚀 Key Features

### 1. Hybrid Policy Authoring
- **Code-First (GitOps)**: Developers write policies in their IDE, test them locally, and `git push`. The system syncs them automatically.
- **UI-First (Hot-Patching)**: Need to stop an attack or change a rule *now*? Edit directly in the Monaco-based web editor and push instantly.

### 2. Intelligent Hierarchy
The engine supports a sophisticated override model:
- **Product Policies** (Bottom-Up): Defined by service owners. (e.g., "Users can read their own data").
- **Custom Policies** (Top-Down): Defined by Admins. (e.g., "NO ONE can read data tagged `TopSecret`").
- **Resolution**: The engine merges these automatically, ensuring Compliance always wins over Convenience.

### 3. Build-Time Safety
Includes a **Maven Plugin** (`policy-engine-maven-plugin`) that validates your `policy-manifest.yaml` and Rego syntax *during the build*, preventing bad configurations from ever reaching production.

### 4. Enterprise Ready
- **Transport Agnostic**: Native support for **WebSocket** (Simple), **Kafka** (Scale), and **RabbitMQ**.
- **WASM Support**: Compiles Rego to WebAssembly for bare-metal performance.
- **Audit Logging**: Every policy evaluation and change is recorded.

---

## 🆚 Comparison

| Feature | OpenPickles | OPA Gatekeeper | Styra DAS | OPAL (Permit.io) |
| :--- | :--- | :--- | :--- | :--- |
| **Primary Focus** | **Microservices & App Logic** | Kubernetes Admission Control | General Purpose OPA Mgmt | OPA Real-time Sync (The "Pipe") |
| **Model** | **Control Plane + UI** (Self-Hosted) | K8s Controller | Commercial SaaS | Sync Engine / Sidecar |
| **Policy Source** | **Hybrid** (Manifest + UI Overrides) | CRDs (K8s Resources) | UI / Git | Git / 3rd Party APIs |
| **Mgmt Interface**| **Included (Dashboard)** | kubectl | Proprietary UI | CLI / API (UI is SaaS) |
| **Best For** | **Enterprise App Dev teams** | Platform Engineers | Enterprises needing SaaS | Teams building their own AuthZ |

### Why OpenPickles?

**Vs. OPA Gatekeeper**: Gatekeeper is excellent for K8s clusters ("Don't run root containers"), but it doesn't help your Java microservice decide "Can User A edit Document B?". OpenPickles is purpose-built for that **Application Layer Authorization**.

**Vs. OPAL (Open Policy Administration Layer)**:
OPAL is a fantastic tool for *synchronizing* OPA with Git and Databases. However, OPAL is primarily a "Pipe"—it moves data.
**OpenPickles is a Platform**:
1.  **Management UI**: We provide a built-in "Pane of Glass" for authorized personnel to view and edit policies. OPAL generally requires you to build your own UI or use Permit.io's SaaS.
2.  **Opinionated Hierarchy**: OpenPickles understands "Services", "Bundles", and "Product vs. Custom" policies tailored for large organizations. OPAL is unopinionated.
3.  **Bootstrapping**: OpenPickles allows services to *self-register* their policy requirements via `policy-manifest.yaml`.

**Vs. Styra DAS**: OpenPickles is the **Open Source, Self-Hosted alternative**. You own the data, you own the control plane, and you can customize the enforcement capabilities (like our unique Composite Bundles) without a commercial contract.

---

## 🛠 Getting Started

### 1. Run the Control Plane
```bash
./start-dev.sh
```
Access the dashboard at `http://localhost:8080`.
*Default Credentials: `admin` / `admin123`*

### 2. Detailed Guides
- **[Integration Guide](INTEGRATION_GUIDE.md)**: Full walkthrough of integrating a Java Spring Boot service.
- **[Frontend Development](frontend/README.md)**: Guide for contributing to the dashboard.
- **[Backend Architecture](backend/README.md)**: Deep dive into the Spring Boot Control Plane.

### 3. Prerequisties
- Java 17+
- Maven 3.8+
- Node.js 18+ (for UI development)

---

## 🔒 Security Architecture

The Control Plane uses a simplified Standard-Based security model:
1.  **Service Authentication**: Agents/Clients authenticate using **Basic Auth** (or Client Credentials) to register and download bundles.
2.  **User Authentication**: The Dashboard uses **Form-Based Auth** (expandable to OIDC/OAuth2).
3.  **Secure Default**: The `policy-engine-client` defaults to `fail-open` (configurable) to ensure your service stays alive even if the Control Plane is unreachable, using cached policies.

### Client Features & Resiliency
The client library is designed for cloud-native reliability:
- **Local Caching**: Policies are cached in-memory. If the Control Plane goes down, the client continues to enforce the last known good policy.
- **Fail-Open/Close**: Configurable default behavior (`failFast`) for startup connectivity issues.
- **Automatic Reconnection**: Exponential backoff with jitter for WebSocket/Broker connections.
- **Background Sync**: Can start in the background to avoid blocking service startup (critical for mesh sidecars).

## Client Configuration

### Quick Implementation
Add the dependency and instantiate the client:

```java
// 1. Configure
ClientConfig config = ClientConfig.builder()
    .controlPlaneUrl("http://localhost:8080")
    .manifestPath("classpath:policy-manifest.yaml")
    .bundleName("my-service-bundle")
    .transportType("WEBSOCKET")
    .build();

// 2. Start
PolicyEngineClient client = new PolicyEngineClient(config);
client.bootstrap(); // Sync manifest
client.start();     // Start listening for updates

// 3. Evaluate
EvaluationResult result = client.evaluate("my-policy", inputMap);
if (result.isAllowed()) {
    // Grant Access
}
```

The `policy-engine-client` can be configured via the `ClientConfig` builder key properties:

---

## 🤝 Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details on how to submit Pull Requests, report issues, and request features.

## 📄 License

This project is licensed under the **Apache 2.0 License**. See [LICENSE](LICENSE) for more details.
