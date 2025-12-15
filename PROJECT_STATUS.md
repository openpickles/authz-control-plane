# Project Status

## Implemented Features

### 1. Resource Providers
- **List Providers**: View all registered microservices/providers.
- **Register Provider**: Add new providers with Service Name, Base URL, Resource Type, and Fetch Endpoint.
- **Delete Provider**: Remove existing providers.
- **Fetch Resources**:
    - Backend can fetch resources from registered providers.
    - Frontend can fetch and display resources in the "Add Entitlement" modal.
    - Mock endpoint (`/api/v1/providers/mock/loans`) exists for testing.

### 2. Entitlements
- **List Entitlements**: View all access control rules in a table format.
- **Create Entitlement**:
    - Support for Subject Type (User, Role, Group).
    - Support for Resource Type selection (from Providers or Custom).
    - **Multi-Value Support**:
        - Select multiple Actions (Read, Write, etc.).
        - Select multiple Resource IDs (via MultiSelect or comma-separated input).
- **Delete Entitlement**: Remove access rules.
- **Sync API**: Endpoint (`/api/v1/sync/entitlements`) to expose rules for external enforcement points.

### 3. User Management
- **View Data**: Tabbed interface to view Users, Roles, and Groups.
- **Create Items**: Simple creation of Users, Roles, and Groups (Name only).
- **Delete Items**: Remove Users, Roles, and Groups.

### 4. Policy Management
- **List Policies**: View all policies with status and version.
- **Authoring**:
    - **Monaco Editor**: Professional in-browser editor with syntax highlighting.
    - **File Upload**: Direct upload of `.rego` files.
- **GitOps Integration**:
    - **Git Source**: capabilities to link policies to Git repositories.
    - **Sync**: On-demand synchronization of policies from Git.
- **Sync API**: Endpoint (`/api/v1/sync/policies`) to expose active policies.

- **Sync API**: Endpoint (`/api/v1/sync/policies`) to expose active policies.

### 5. Infrastructure & Deployment
- **Integrated Build**: Backend build process now automatically builds the frontend and packages it into the JAR.
- **Single Artifact**: Deployment simplified to a single JAR or Docker container.

## TODO / Known Issues

### 1. Filtering & Search
- **Entitlement List Filtering**:
    - [ ] **UI**: Search input and "Filters" button exist but are non-functional.
    - [ ] **Logic**: No frontend logic to filter the table.
    - [ ] **Backend**: `EntitlementController` has `getByResource` but it's not wired to the main list view for general filtering.
- **Global Search**: No global search functionality implemented.

### 2. Editing Capabilities
- **Entitlements**:
    - [ ] **Edit Entitlement**: No UI to edit an existing entitlement (only Delete is available). Backend supports `updateEntitlement`.
- **Resource Providers**:
    - [ ] **Edit Provider**: No UI to edit provider details. Backend lacks an update endpoint for Providers.
- **User Management**:
    - [ ] **Edit User/Role/Group**: No UI to rename or modify users/roles/groups.

### 3. Policy Management
- **Frontend**:
    - [ ] **Policy UI**: No visible frontend page for managing Policies (only Entitlements and Providers are visible in the analyzed files).

### 4. Validation & Error Handling
- **Resource Fetching**:
    - [ ] **Error Feedback**: Limited error handling if a provider is unreachable during the "Add Entitlement" flow.
