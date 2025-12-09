# Resource Provider Integration Guide

This document outlines the requirements for integrating a microservice (Resource Provider) with the Policy Engine. Integration involves two parts:
1.  **Control Plane Integration**: Exposing metadata so the Policy Engine UI can manage rules.
2.  **Data Plane Integration**: Downloading and enforcing policy bundles.

---

## Part 1: Control Plane Integration

To allow admins to create Entitlements and Policies for your service, you must expose endpoints that the Policy Engine can query.

### 1.1 Action Metadata Endpoint
**Method**: `GET`
**Path**: `/metadata` (or configured path)
**Purpose**: Tells the Policy Engine what actions are supported by your resources (e.g., `view`, `approve`).

**Response Format**:
```json
{
  "resourceType": "loan-service:loan",
  "actions": ["view", "create", "approve", "reject", "delete"]
}
```

### 1.2 Filter Schema Endpoint
**Method**: `GET`
**Path**: `/schema`
**Purpose**: Tells the Policy Engine how to filter your resources in the UI.

**Response Format**:
```json
{
  "filters": [
    {
      "key": "status",
      "label": "Loan Status",
      "type": "select",
      "options": [
        { "label": "Active", "value": "ACTIVE" },
        { "label": "Closed", "value": "CLOSED" }
      ]
    },
    {
      "key": "minAmount",
      "label": "Minimum Amount",
      "type": "number"
    }
  ]
}
```

### 1.3 Resource Search Endpoint
**Method**: `GET`
**Path**: `/resources`
**Purpose**: Returns resources matching the user's filter criteria.

**Request Example**: `GET /resources?status=ACTIVE&minAmount=5000`

**Response Format**:
```json
[
  { "id": "L-101", "name": "Home Loan - Alice" },
  { "id": "L-102", "name": "Car Loan - Bob" }
]
```

---

## Part 2: Data Plane Integration (Enforcement)

Your service is responsible for enforcing policies locally. You should not call the Policy Engine for every request.

### 2.1 Downloading the Bundle
The Policy Engine exposes an endpoint to download a Policy Bundle containing all relevant rules for your service.

**Endpoint**: `GET /api/v1/bundles/{bundleId}/download`
**Format**: `.tar.gz` (Gzipped Tarball)

**Bundle Contents**:
*   `/policies/*.rego`: Individual policy files (OPA compatible).
*   `/data.json`: Aggregated bindings and entitlements.

### 2.2 Using OPA (Open Policy Agent)
We recommend running OPA as a sidecar or library.

1.  **Startup**: Download the bundle from Policy Engine.
2.  **Load**: Load the `.rego` files and `data.json` into OPA.
3.  **Enforce**: On every request, query OPA.

**Example OPA Query**:
```rego
# Input
{
  "user": "alice",
  "action": "approve",
  "resource": "L-101"
}

# Policy Logic (Simplified)
allow {
  # Check if user has entitlement
  some entitlement
  entitlement = data.entitlements[_]
  entitlement.subject.id == input.user
  entitlement.resourceIds[_] == input.resource
  entitlement.actions[_] == input.action
  
  # Check if policy allows (e.g. time of day)
  # ...
}
```

### 2.3 Periodic Sync
Your service (or sidecar) should poll the download endpoint periodically (e.g., every 5 minutes) to fetch updates. Use `If-Modified-Since` or ETags if supported to reduce bandwidth.

---

## Part 3: Policy Management (GitOps)

For teams preferring a **GitOps** workflow, policies can be managed in a version control system (e.g., GitHub, GitLab) instead of the UI.
1.  **Store Policies**: Maintain `.rego` files in a Git repository.
2.  **Configure Sync**: In the Policy Engine UI, create a Policy and select **Git Repository** as the source.
3.  **Sync**: Trigger a sync via UI or API (`POST /api/v1/policies/{id}/sync`) to update the Policy Engine's copy.
4.  **Distribution**: The Policy Engine continues to serve the bundled policies to the Data Plane (Resource Providers) as described in Part 2.
