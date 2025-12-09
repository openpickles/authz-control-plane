# Design: Scalable Resource Filtering for Entitlements

## Problem Statement
We need to assign entitlements to resources (e.g., Loan Accounts, IoT Devices). These resources are managed by external **Resource Providers** (microservices).
*   **Scale**: There can be millions of resources.
*   **Current State**: We fetch *all* resources from a provider and filter client-side. This fails at scale (performance, memory, network).
*   **Goal**: Provide a user-friendly way to find and select specific resources from millions of records without importing all data into the Policy Engine.

## Industry Patterns

### 1. Salesforce (Lookup Filters & List Views)
*   **Lookups**: When searching for a record, you don't just see a list. You type a search term (server-side search).
*   **Lookup Filters**: Admins define static filters (e.g., "Only show Active Accounts").
*   **List Views**: Users create their own views with criteria (e.g., "My Accounts in California").

### 2. Jira (JQL & Basic Search)
*   **Basic**: Dropdowns for common fields (Project, Type, Status). These are often hierarchical or dependent.
*   **Advanced (JQL)**: Query language for complex logic.
*   **Auto-complete**: As you type, the server returns matches.

---

## Proposed Options

### Option 1: Standardized Search Protocol (The "Google Search" Approach)
The Policy Engine defines a strict search contract that all Resource Providers must implement.

*   **Workflow**:
    1.  User types "John" in the Policy Engine UI.
    2.  Policy Engine calls Provider: `GET /resources?q=John&limit=20&offset=0`.
    3.  Provider performs the search (SQL `LIKE`, ElasticSearch, etc.) and returns results.
*   **Pros**: Simple to implement on Policy Engine side. Familiar UX.
*   **Cons**: "Fuzzy" search might be too broad. Doesn't allow precise filtering (e.g., "Active loans only").

### Option 2: Dynamic Filter Schema (The "Salesforce/Jira" Approach) - **RECOMMENDED**
The Policy Engine asks the Provider: "How can I filter your resources?" The Provider returns a schema of filterable fields.

*   **Workflow**:
    1.  **Discovery**: Policy Engine calls Provider: `GET /resources/schema`.
    2.  **Schema Response**:
        ```json
        {
          "filters": [
            { "key": "status", "label": "Loan Status", "type": "select", "options": ["Active", "Closed"] },
            { "key": "amount", "label": "Min Amount", "type": "number" },
            { "key": "region", "label": "Region", "type": "text" }
          ]
        }
        ```
    3.  **UI Rendering**: Policy Engine dynamically renders these inputs (Dropdown for Status, Number box for Amount).
    4.  **Search**: User fills them in. Policy Engine calls: `GET /resources?status=Active&amount=5000`.
*   **Pros**:
    *   **Extremely Flexible**: Works for any domain (Loans, Devices, Users) without changing Policy Engine code.
    *   **Scalable**: Filtering happens at the source (Provider database).
    *   **User Friendly**: Users see relevant filters (Status, Region) instead of just a generic search box.
*   **Cons**: Requires Providers to implement the schema endpoint and filtering logic.

### Configuration Source: Where do these filters live?
In this **Dynamic** approach, the filter definitions are **NOT** stored in the Policy Engine's `application.yaml` or database. They are **owned by the Resource Provider**.

*   **Why?**: This ensures the Policy Engine remains generic. If the "Loan Service" adds a new field "Interest Rate", it simply updates its `/schema` response. The Policy Engine automatically renders the new filter without a deployment or config change.
*   **Mechanism**:
    1.  **Resource Provider**: Implements `GET /schema` (or `/meta/filters`).
    2.  **Policy Engine**: Stores only the **Base URL** of the provider (which is already implemented in the `ResourceProvider` entity).
    3.  **Runtime**: When a user selects "Loan Service" in the UI, the Policy Engine proxies a call to `[Provider_Base_URL]/schema` to get the live configuration.

### Data Flow: UI vs Backend
To answer "Who fetches the filters?": **The Policy Engine Backend**.

*   **The Flow**: `Browser (UI)` -> `Policy Engine Backend` -> `Resource Provider`
*   **Why not UI directly to Provider?**:
    1.  **Security**: Resource Providers often require internal authentication (mTLS, API Keys) that should not be exposed to the browser.
    2.  **Network**: Providers might be in a private VPC not accessible from the public internet.
    3.  **CORS**: Browsers block cross-origin requests unless explicitly allowed, which complicates Provider implementation.
*   **Implementation**:
    *   The Policy Engine exposes a proxy endpoint: `GET /api/v1/providers/{providerId}/schema`.
    *   The UI calls this endpoint.
    *   The Backend looks up the Provider's URL and makes the server-to-server call.

    *   The UI calls this endpoint.
    *   The Backend looks up the Provider's URL and makes the server-to-server call.

### Caching Strategy (Reliability & Performance)
To address the risk of "Provider down = No filters shown", the Policy Engine Backend will implement **Caching**.

*   **Mechanism**:
    1.  **Check Cache**: When UI requests schema, Backend first checks its local cache (e.g., In-Memory/Redis).
    2.  **Cache Hit**: Return immediately. Fast and works even if Provider is down.
    3.  **Cache Miss**: Call Provider `GET /schema`.
        *   **Success**: Save to cache with a TTL (e.g., 1 hour) and return.
        *   **Failure**: If the Provider is down and cache is empty, return a default/empty schema or a generic error.
*   **Benefits**:
    *   **Resilience**: UI continues to work during temporary Provider outages.
    *   **Performance**: Reduces network latency for frequent page loads.
    *   **Reduced Load**: Prevents hammering the Provider with schema requests.

#### Alternative: Static Configuration (Not Recommended)
If a Provider cannot be modified to expose a schema, we *could* store the filter config in the Policy Engine's database as a JSON blob attached to the `ResourceProvider` entity.
*   **Pros**: Works with legacy providers.
*   **Cons**: Tightly couples Policy Engine to Provider internals. Requires admin overhead to keep configs in sync.

### Option 3: Metadata Sync (The "Data Warehouse" Approach)
Periodically import lightweight metadata (ID, Name, Tags) of *all* resources into the Policy Engine's database.

*   **Workflow**:
    1.  Nightly job fetches all 10M resource IDs + Metadata from Provider.
    2.  Store in Policy Engine DB (indexed).
    3.  UI searches Policy Engine's local DB.
*   **Pros**: Fast UI response. No realtime dependency on Provider during UI interaction.
*   **Cons**:
    *   **Stale Data**: Resources created 5 mins ago might not appear.
    *   **Storage**: Duplicating 10M records is expensive and complex to maintain.

---

## Recommendation
**Adopt Option 2 (Dynamic Filter Schema)**.

It offers the best balance of **usability** (domain-specific filters) and **scalability** (filtering happens at the source). It avoids the complexity of data synchronization (Option 3) while being more powerful than simple text search (Option 1).

### Proposed UI Changes
1.  **Resource Provider Config**: No changes needed.
2.  **Entitlement Creation Modal**:
    *   When a Resource Type is selected, fetch its **Filter Schema**.
    *   Render the filters (inputs, dropdowns) above the results list.
    *   "Search" button triggers the parameterized fetch.
    *   Results list is paginated.
