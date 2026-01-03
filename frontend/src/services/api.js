import axios from 'axios';

const API_URL = '/api/v1';

const api = axios.create({
    baseURL: API_URL,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true,
});

export const policyService = {
    getAll: (params) => api.get('/policies', { params }),
    getById: (id) => api.get(`/policies/${id}`),
    create: (policy) => api.post('/policies', policy),
    update: (id, policy) => api.put(`/policies/${id}`, policy),
    delete: (id) => api.delete(`/policies/${id}`),
    sync: (id) => api.post(`/policies/${id}/sync`),
    push: (id, commitMessage) => api.post(`/policies/${id}/push`, { commitMessage }),
};

export const evaluationService = {
    validate: (content) => api.post('/evaluation/validate', { content }),
    test: (testRequest) => api.post('/evaluation/test', testRequest),
};

export const entitlementService = {
    getAll: (params) => api.get('/entitlements', { params }),
    getById: (id) => api.get(`/entitlements/${id}`),
    create: (entitlement) => api.post('/entitlements', entitlement),
    update: (id, entitlement) => api.put(`/entitlements/${id}`, entitlement),
    delete: (id) => api.delete(`/entitlements/${id}`),
};

export const resourceTypeService = {
    getAll: (params) => api.get('/resource-types', { params }),
    getById: (id) => api.get(`/resource-types/${id}`),
    create: (resourceType) => api.post('/resource-types', resourceType),
    update: (id, resourceType) => api.put(`/resource-types/${id}`, resourceType),
    delete: (id) => api.delete(`/resource-types/${id}`),
    refreshSchema: (id) => api.post(`/resource-types/${id}/refresh-schema`),
    getSchema: (id) => api.get(`/resource-types/${id}/schema`),
    fetchResources: (key, params = {}) => {
        const queryParams = new URLSearchParams({ resourceType: key, ...params });
        return api.get(`/resource-types/fetch?${queryParams.toString()}`);
    },
};

export const policyBindingService = {
    getAll: (params) => api.get('/policy-bindings', { params }),
    getByResourceType: (type) => api.get(`/policy-bindings/search?resourceType=${type}`),
    create: (binding) => api.post('/policy-bindings', binding),
    delete: (id) => api.delete(`/policy-bindings/${id}`),
};

export const policyBundleService = {
    getAll: (params) => api.get('/bundles', { params }),
    create: (bundle) => api.post('/bundles', bundle),
    download: (id) => {
        window.open(`${API_URL}/bundles/${id}/download`, '_blank');
    }
};

export const userService = {
    getAll: () => api.get('/users'),
    createUser: (user) => api.post('/users', user),
    deleteUser: (id) => api.delete(`/users/${id}`),

    getAllRoles: () => api.get('/users/roles'),
    createRole: (role) => api.post('/users/roles', role),

    getAllGroups: () => api.get('/users/groups'),
    createGroup: (group) => api.post('/users/groups', group),
};

export default api;
