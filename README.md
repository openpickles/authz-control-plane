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
- **Modern & Consistent UI**: Standardized "DataGrid" and "SlideOver" components across all listing pages.

## Testing

### Backend
Run backend tests using Maven:
```bash
cd backend
mvn test
```

### Frontend
Run frontend component tests using Vitest:
```bash
cd frontend
npm test
```
- **User Management**: Manage users, roles, and groups.
- **Modern UI**: Responsive dashboard built with React and TailwindCSS.
- **API-First**: REST APIs for microservices to fetch policies, sync entitlements, and download bundles.


## Tech Stack

- **Backend**: Java 17, Spring Boot 3.3, H2 Database, Spring Security, JPA.
- **Frontend**: React 18, Vite, TailwindCSS, Lucide Icons.

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
