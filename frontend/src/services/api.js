import axios from 'axios';

const API_URL = 'http://localhost:8080/api/v1';

const api = axios.create({
    baseURL: API_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

export const policyService = {
    getAll: () => api.get('/policies'),
    getById: (id) => api.get(`/policies/${id}`),
    create: (policy) => api.post('/policies', policy),
    update: (id, policy) => api.put(`/policies/${id}`, policy),
    delete: (id) => api.delete(`/policies/${id}`),
    sync: (id) => api.post(`/policies/${id}/sync`),
};

export const entitlementService = {
    getAll: () => api.get('/entitlements'),
    getById: (id) => api.get(`/entitlements/${id}`),
    create: (entitlement) => api.post('/entitlements', entitlement),
    update: (id, entitlement) => api.put(`/entitlements/${id}`, entitlement),
    delete: (id) => api.delete(`/entitlements/${id}`),
};

export const resourceProviderService = {
    getAll: () => api.get('/providers'),
    create: (provider) => api.post('/providers', provider),
    delete: (id) => api.delete(`/providers/${id}`),
    fetchResources: (type, params = {}) => {
        const queryParams = new URLSearchParams({ resourceType: type, ...params });
        return api.get(`/providers/fetch?${queryParams.toString()}`);
    },
    getProviderSchema: (id) => api.get(`/providers/${id}/schema`),
    getProviderMetadata: (id) => api.get(`/providers/${id}/metadata`),
};

export const policyBindingService = {
    getAll: () => api.get('/policy-bindings'),
    getByResourceType: (type) => api.get(`/policy-bindings/search?resourceType=${type}`),
    create: (binding) => api.post('/policy-bindings', binding),
    delete: (id) => api.delete(`/policy-bindings/${id}`),
};

export const policyBundleService = {
    getAll: () => api.get('/bundles'),
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
