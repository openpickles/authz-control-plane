import React, { useState, useEffect, useCallback } from 'react';
import { Plus, Trash2, RefreshCw, AlertTriangle } from 'lucide-react';
import { entitlementService, resourceTypeService } from '../services/api';
import MultiSelect from '../components/MultiSelect';
import DynamicFilter from '../components/DynamicFilter';
import DataGrid from '../components/DataGrid';
import SlideOver from '../components/SlideOver';

const Entitlements = () => {
    // List State
    const [entitlements, setEntitlements] = useState([]);
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(0);
    const [size] = useState(10);
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [search, setSearch] = useState('');

    // Shared State
    const [resourceTypes, setResourceTypes] = useState([]);

    // Creation State
    const [showCreate, setShowCreate] = useState(false);
    const [formData, setFormData] = useState({
        resourceType: '',
        resourceIds: [],
        actions: [],
        subjectType: 'USER',
        subjectId: '',
        effect: 'ALLOW'
    });

    // Dynamic Logic State
    const [fetchedResources, setFetchedResources] = useState([]);
    const [providerSchema, setProviderSchema] = useState(null);
    const [providerActions, setProviderActions] = useState([]);
    const [activeFilters, setActiveFilters] = useState({});

    const loadEntitlements = useCallback(async () => {
        setLoading(true);
        try {
            const params = { page, size, search };
            const response = await entitlementService.getAll(params);
            const data = response.data.content || response.data || [];
            setEntitlements(data);
            setTotalElements(response.data.totalElements || data.length || 0);
            setTotalPages(response.data.totalPages || 1);
        } catch (error) {
            console.error('Error loading entitlements:', error);
            setEntitlements([]);
        } finally {
            setLoading(false);
        }
    }, [page, size, search]);

    const loadResourceTypes = useCallback(async () => {
        try {
            const response = await resourceTypeService.getAll({ size: 100 });
            setResourceTypes(response.data.content || response.data || []);
        } catch (error) {
            console.error('Error loading resource types:', error);
        }
    }, []);

    useEffect(() => {
        const timer = setTimeout(() => loadEntitlements(), 300);
        return () => clearTimeout(timer);
    }, [loadEntitlements]);

    // Initial load of Types
    useEffect(() => {
        loadResourceTypes();
    }, [loadResourceTypes]);

    const handleResourceTypeChange = async (e) => {
        const typeKey = e.target.value;
        const selectedType = resourceTypes.find(p => p.key === typeKey);

        setFormData({ ...formData, resourceType: typeKey, resourceIds: [] });
        setFetchedResources([]);
        setActiveFilters({});

        if (selectedType) {
            try {
                // Parse schema from the stored field
                // It might be a JSON string or null
                let schema = null;
                if (selectedType.schema) {
                    try {
                        schema = typeof selectedType.schema === 'string'
                            ? JSON.parse(selectedType.schema)
                            : selectedType.schema;
                    } catch (err) {
                        console.error("Failed to parse schema JSON", err);
                    }
                }

                // Fallback: Default schema structure if missing
                if (!schema) {
                    schema = { filters: [], actions: ['read', 'write', 'delete'] };
                }

                setProviderSchema(schema);
                setProviderActions(schema.actions || []);
            } catch (error) {
                console.error('Error loading schema from type:', error);
                setProviderSchema(null);
                setProviderActions([]);
            }
        } else {
            setProviderSchema(null);
            setProviderActions([]);
        }
    };

    const handleFilterChange = (key, value) => {
        setActiveFilters(prev => ({ ...prev, [key]: value }));
    };

    const handleSearchResources = async () => {
        if (!formData.resourceType) return;
        try {
            // Pass activeFilters as query params
            const response = await resourceTypeService.fetchResources(formData.resourceType, activeFilters);
            setFetchedResources(response.data || []);
        } catch (error) {
            console.error('Error fetching resources:', error);
            setFetchedResources([]);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const payload = { ...formData };
            if (payload.actions.length === 0) {
                alert('Please select at least one action');
                return;
            }
            if (!payload.resourceType) {
                alert('Please select a resource type');
                return;
            }
            // resourceIds is already an array from MultiSelect

            await entitlementService.create(payload);
            setShowCreate(false);
            setFormData({
                resourceType: '',
                resourceIds: [],
                actions: [],
                subjectType: 'USER',
                subjectId: '',
                effect: 'ALLOW'
            });
            loadEntitlements();
        } catch (error) {
            console.error('Error creating entitlement:', error);
            alert('Failed to create entitlement');
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure?')) {
            try {
                await entitlementService.delete(id);
                loadEntitlements();
            } catch (error) {
                console.error('Error deleting entitlement:', error);
            }
        }
    };

    const columns = [
        { header: 'Subject', accessor: 'subjectId', render: (row) => <span className="font-medium">{row.subjectId}</span> },
        { header: 'Type', accessor: 'subjectType', render: (row) => <span className="text-xs font-mono bg-slate-100 rounded px-1">{row.subjectType}</span> },
        { header: 'Resource Type', accessor: 'resourceType', render: (row) => <span className="text-sm">{row.resourceType}</span> },
        {
            header: 'Resource IDs',
            accessor: 'resourceIds',
            render: (row) => (
                <div className="flex flex-wrap gap-1">
                    {row.resourceIds && row.resourceIds.map(rid => (
                        <span key={rid} className="text-xs bg-blue-50 text-blue-700 px-1 rounded">{rid}</span>
                    ))}
                </div>
            )
        },
        {
            header: 'Actions',
            accessor: 'actions',
            render: (row) => (
                <div className="flex flex-wrap gap-1">
                    {row.actions && row.actions.map(act => (
                        <span key={act} className="text-xs bg-green-50 text-green-700 px-1 rounded capitalize">{act}</span>
                    ))}
                </div>
            )
        },
        {
            header: '',
            accessor: 'id',
            className: 'w-10',
            render: (row) => (
                <button onClick={() => handleDelete(row.id)} className="text-slate-400 hover:text-red-500">
                    <Trash2 size={16} />
                </button>
            )
        }
    ];

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center mb-6">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-slate-900">Entitlements</h1>
                    <p className="mt-2 text-sm text-slate-500">Manage fine-grained access control rules.</p>
                </div>
                <button
                    onClick={() => setShowCreate(true)}
                    className="btn-primary flex items-center gap-2"
                >
                    <Plus size={16} />
                    New Entitlement
                </button>
            </div>

            <DataGrid
                columns={columns}
                data={entitlements}
                loading={loading}
                page={page}
                size={size}
                totalElements={totalElements}
                totalPages={totalPages}
                onPageChange={setPage}
                onSearch={setSearch}
                emptyMessage="No entitlements found."
            />

            <SlideOver
                isOpen={showCreate}
                onClose={() => setShowCreate(false)}
                title="New Entitlement Rule"
            >
                <form onSubmit={handleSubmit} className="space-y-6">
                    {/* Subject Section */}
                    <div className="space-y-4 border-b pb-4">
                        <h3 className="text-sm font-medium text-slate-900">Subject</h3>
                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-slate-700">Type</label>
                                <select
                                    className="input-field mt-1"
                                    value={formData.subjectType}
                                    onChange={e => setFormData({ ...formData, subjectType: e.target.value })}
                                >
                                    <option value="USER">User</option>
                                    <option value="ROLE">Role</option>
                                    <option value="GROUP">Group</option>
                                </select>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-slate-700">ID / Name</label>
                                <input
                                    type="text"
                                    required
                                    className="input-field mt-1"
                                    placeholder="e.g. alice, admin-role"
                                    value={formData.subjectId}
                                    onChange={e => setFormData({ ...formData, subjectId: e.target.value })}
                                />
                            </div>
                        </div>
                    </div>

                    {/* Resource Section */}
                    <div className="space-y-4 border-b pb-4">
                        <h3 className="text-sm font-medium text-slate-900">Resource</h3>
                        <div>
                            <label className="block text-sm font-medium text-slate-700">Type</label>
                            <select
                                className="input-field mt-1"
                                value={formData.resourceType}
                                onChange={handleResourceTypeChange}
                            >
                                <option value="">Select Resource Type</option>
                                {resourceTypes.map(p => (
                                    <option key={p.id} value={p.key}>{p.name} ({p.key})</option>
                                ))}
                            </select>
                        </div>

                        {/* Dynamic Filters */}
                        {providerSchema && providerSchema.filters && (
                            <div className="bg-slate-50 p-4 rounded-lg border border-slate-200 space-y-3">
                                <div className="flex justify-between items-center mb-2">
                                    <span className="text-xs font-semibold text-slate-500 uppercase tracking-wide">Find Resources</span>
                                    <button type="button" onClick={handleSearchResources} className="btn-secondary btn-xs">Search</button>
                                </div>
                                <div className="grid grid-cols-2 gap-3">
                                    {providerSchema.filters.map(filter => (
                                        <DynamicFilter
                                            key={filter.key}
                                            filter={filter}
                                            value={activeFilters[filter.key] || ''}
                                            onChange={(value) => handleFilterChange(filter.key, value)}
                                        />
                                    ))}
                                </div>
                                {fetchedResources.length === 0 && formData.resourceType && (
                                    <p className="text-xs text-amber-600 mt-2 flex items-center gap-1">
                                        <AlertTriangle size={12} />
                                        No resources found or provider not reachable.
                                    </p>
                                )}
                            </div>
                        )}
                        <div>
                            <label className="block text-sm font-medium text-slate-700">Resource IDs</label>
                            <MultiSelect
                                options={fetchedResources.map(r => ({ label: r.name || r.id, value: r.id }))}
                                value={formData.resourceIds}
                                onChange={(ids) => setFormData({ ...formData, resourceIds: ids })}
                                placeholder="Select resources or type IDs..."
                                allowCustom={true}
                            />
                            <p className="text-xs text-slate-500 mt-1">
                                Search above to populate options, or type IDs manually (comma separated).
                            </p>
                        </div>
                    </div>

                    {/* Action Section */}
                    {providerActions.length > 0 && (
                        <div className="space-y-4">
                            <h3 className="text-sm font-medium text-slate-900">Actions</h3>
                            <div className="flex flex-wrap gap-2">
                                {providerActions.map(action => (
                                    <label key={action} className={`
                                        inline-flex items-center px-3 py-1.5 rounded-full text-xs font-medium cursor-pointer transition-colors border
                                        ${formData.actions.includes(action)
                                            ? 'bg-blue-100 text-blue-800 border-blue-200'
                                            : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50'}
                                    `}>
                                        <input
                                            type="checkbox"
                                            className="hidden"
                                            checked={formData.actions.includes(action)}
                                            onChange={(e) => {
                                                if (e.target.checked) {
                                                    setFormData(prev => ({ ...prev, actions: [...prev.actions, action] }));
                                                } else {
                                                    setFormData(prev => ({ ...prev, actions: prev.actions.filter(a => a !== action) }));
                                                }
                                            }}
                                        />
                                        {action}
                                    </label>
                                ))}
                            </div>
                        </div>
                    )}

                    <div className="flex justify-end gap-3 pt-6 border-t">
                        <button
                            type="button"
                            onClick={() => setShowCreate(false)}
                            className="btn-secondary"
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            className="btn-primary"
                        >
                            Create Rule
                        </button>
                    </div>
                </form>
            </SlideOver>
        </div>
    );
};

export default Entitlements;
