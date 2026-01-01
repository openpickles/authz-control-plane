# Project Status

## Implemented
- [x] **Resource Type Management**: Replaced Resource Providers with Resource Types. Supports manual schema definition and external metadata fetching.
- [x] **Dynamic Entitlements UI**: Entitlement forms dynamically render filters based on the selected Resource Type's schema.
- [x] **Policy Management**: Upload Rego files, view list (DataGrid).
- [x] **Git Integration**: Basic structure for syncing policies (Pull/Push).
- [x] **Pagination & Search**: Implemented across all listing pages (Resource Types, Policies, Entitlements).

## Current Focus
- **Verification**: Ensuring all legacy references to ResourceProvider are removed.
- **Testing**: End-to-end testing of the "Fetch Schema" workflow and entitlement creation.

### 2. Entitlements
- **List Entitlements**: View all access control rules in a **DataGrid** with server-side pagination and search.
- **Create Entitlement**:
    - Support for Subject Type (User, Role, Group).
    - Support for Resource Type selection (from Providers or Custom).
    - **Dynamic Filtering**: Fetches schemas from Resource Providers to render domain-specific filters.
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
- **Policy Bundles**: Manage and distribute policy compilations (WASM or JSON).
- **Policy Bindings**: Bind policies to specific resource types and contexts.
- **Authoring**:
    - **Monaco Editor**: Professional in-browser editor with syntax highlighting.
    - **File Upload**: Direct upload of `.rego` files.
- **GitOps Integration**:
    - **Git Source**: capabilities to link policies to Git repositories.
    - **Sync**: On-demand synchronization of policies from Git.
- **Sync API**: Endpoint (`/api/v1/sync/policies`) to expose active policies.

### 5. Infrastructure & Deployment
- **Integrated Build**: Backend build process now automatically builds the frontend and packages it into the JAR.
- **Single Artifact**: Deployment simplified to a single JAR or Docker container.

## TODO / Known Issues

### 1. Filtering & Search
- **Global Search**: No global search functionality implemented.

### 2. Editing Capabilities
- **Entitlements**:
    - [ ] **Edit Entitlement**: No UI to edit an existing entitlement (only Delete is available). Backend supports `updateEntitlement`.
- **Resource Providers**:
    - [ ] **Edit Provider**: No UI to edit provider details.
- **User Management**:
    - [ ] **Edit User/Role/Group**: No UI to rename or modify users/roles/groups.

### 3. Policy Management
- **Frontend**:
    - [ ] **Policies List View**: No dedicated list page for raw policies (managed via Editor or Bindings).

### 4. Validation & Error Handling
- **Resource Fetching**:
    - [ ] **Error Feedback**: Limited error handling if a provider is unreachable during the "Add Entitlement" flow.
