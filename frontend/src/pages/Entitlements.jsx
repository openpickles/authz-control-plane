import React, { useState, useEffect } from 'react';
import { Plus, Search, Filter, Trash2, RefreshCw, MoreHorizontal, X, AlertTriangle } from 'lucide-react';
import { entitlementService, resourceProviderService } from '../services/api';
import MultiSelect from '../components/MultiSelect';
import DynamicFilter from '../components/DynamicFilter';

const Entitlements = () => {
    const [entitlements, setEntitlements] = useState([]);
    const [providers, setProviders] = useState([]);
    const [fetchedResources, setFetchedResources] = useState([]);
    const [showModal, setShowModal] = useState(false);
    const [formData, setFormData] = useState({
        resourceType: '',
        resourceIds: [],
        actions: [],
        subjectType: 'USER',
        subjectId: '',
        effect: 'ALLOW'
    });

    const [providerSchema, setProviderSchema] = useState(null);
    const [providerActions, setProviderActions] = useState([]);
    const [activeFilters, setActiveFilters] = useState({});

    const loadEntitlements = React.useCallback(async () => {
        try {
            const response = await entitlementService.getAll();
            setEntitlements(response.data);
        } catch (error) {
            console.error('Error loading entitlements:', error);
        }
    }, []);

    const loadProviders = React.useCallback(async () => {
        try {
            const response = await resourceProviderService.getAll();
            setProviders(response.data);
        } catch (error) {
            console.error('Error loading providers:', error);
        }
    }, []);

    useEffect(() => {
        loadEntitlements();
        loadProviders();
    }, [loadEntitlements, loadProviders]);

    const handleResourceTypeChange = async (e) => {
        const type = e.target.value;
        setFormData({ ...formData, resourceType: type, resourceIds: [] });
        setFetchedResources([]);
        setProviderSchema(null);
        setActiveFilters({});

        if (type && type !== 'custom') {
            // Find provider to get ID
            const provider = providers.find(p => p.resourceType === type);
            if (provider) {
                try {
                    // Fetch Metadata
                    const metadataRes = await resourceProviderService.getProviderMetadata(provider.id);
                    setProviderSchema(metadataRes.data);
                    if (metadataRes.data.actions) {
                        setProviderActions(metadataRes.data.actions);
                    } else {
                        setProviderActions([]);
                    }
                } catch (error) {
                    console.warn('Could not fetch metadata for provider:', error);
                }
            }
            // Initial fetch without filters
            handleSearchResources(type);
        }
    };

    const handleSearchResources = async (typeOverride) => {
        const type = typeof typeOverride === 'string' ? typeOverride : formData.resourceType;
        if (!type || type === 'custom') return;

        try {
            const response = await resourceProviderService.fetchResources(type, activeFilters);
            const resources = response.data.map(r => (typeof r === 'string' ? { id: r, name: r } : r));
            setFetchedResources(resources);
        } catch (error) {
            console.error('Error fetching resources:', error);
            setFetchedResources([]);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await entitlementService.create(formData);
            setShowModal(false);
            setFormData({ resourceType: '', resourceIds: [], actions: [], subjectType: 'USER', subjectId: '', effect: 'ALLOW' });
            loadEntitlements();
        } catch (error) {
            console.error('Error creating entitlement:', error);
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Delete this entitlement?')) {
            try {
                await entitlementService.delete(id);
                loadEntitlements();
            } catch (error) {
                console.error('Error deleting entitlement:', error);
            }
        }
    };

    const resourceOptions = [
        { value: '*', label: 'All (*)' },
        ...fetchedResources.map(r => ({ value: r.id, label: `${r.name || r.id} (${r.id})` }))
    ];

    // Common actions, could be fetched or configured
    const defaultActions = [
        { value: 'read', label: 'Read' },
        { value: 'write', label: 'Write' },
        { value: 'delete', label: 'Delete' },
        { value: 'update', label: 'Update' },
        { value: 'execute', label: 'Execute' },
        { value: '*', label: 'All (*)' }
    ];

    const actionOptions = providerActions.length > 0
        ? providerActions.map(a => ({ value: a, label: a }))
        : defaultActions;

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h2 className="text-2xl font-bold text-slate-900">Entitlements</h2>
                    <p className="text-slate-500 mt-1">Manage fine-grained access control rules.</p>
                </div>
                <button
                    onClick={() => setShowModal(true)}
                    className="btn-primary flex items-center gap-2"
                >
                    <Plus size={18} />
                    New Entitlement
                </button>
            </div>

            {/* Filters */}
            <div className="card p-4 flex gap-4 items-center">
                <div className="relative flex-1 max-w-md">
                    <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-slate-400" size={18} />
                    <input
                        type="text"
                        placeholder="Search entitlements..."
                        className="input-field pl-10"
                    />
                </div>
                <div className="h-8 w-px bg-slate-200 mx-2"></div>
                <button className="btn-secondary flex items-center gap-2">
                    <Filter size={18} />
                    Filters
                </button>
            </div>

            {/* Table */}
            <div className="card overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="w-full text-left">
                        <thead className="bg-slate-50 border-b border-slate-200">
                            <tr>
                                <th className="px-6 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Subject</th>
                                <th className="px-6 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Type</th>
                                <th className="px-6 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Actions</th>
                                <th className="px-6 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Resource Type</th>
                                <th className="px-6 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Resource IDs</th>
                                <th className="px-6 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Effect</th>
                                <th className="px-6 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider text-right">Actions</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100 bg-white">
                            {entitlements.map((entitlement) => (
                                <tr key={entitlement.id} className="hover:bg-slate-50 transition-colors">
                                    <td className="px-6 py-4 text-sm font-medium text-slate-900">{entitlement.subjectId}</td>
                                    <td className="px-6 py-4 text-sm">
                                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${entitlement.subjectType === 'USER' ? 'bg-blue-100 text-blue-800' :
                                            entitlement.subjectType === 'ROLE' ? 'bg-purple-100 text-purple-800' :
                                                'bg-orange-100 text-orange-800'
                                            }`}>
                                            {entitlement.subjectType}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 text-sm text-slate-600">
                                        <div className="flex flex-wrap gap-1">
                                            {entitlement.actions && entitlement.actions.map(a => (
                                                <span key={a} className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-slate-100 text-slate-800">
                                                    {a}
                                                </span>
                                            ))}
                                        </div>
                                    </td>
                                    <td className="px-6 py-4 text-sm text-slate-600 font-mono">{entitlement.resourceType}</td>
                                    <td className="px-6 py-4 text-sm text-slate-600 font-mono">
                                        <div className="flex flex-wrap gap-1">
                                            {entitlement.resourceIds && entitlement.resourceIds.map(r => (
                                                <span key={r} className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-slate-100 text-slate-800">
                                                    {r}
                                                </span>
                                            ))}
                                        </div>
                                    </td>
                                    <td className="px-6 py-4 text-sm">
                                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${entitlement.effect === 'ALLOW' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                                            }`}>
                                            {entitlement.effect}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 text-right text-sm font-medium">
                                        <button
                                            onClick={() => handleDelete(entitlement.id)}
                                            className="text-slate-400 hover:text-red-600 transition-colors p-2 rounded-full hover:bg-red-50"
                                        >
                                            <Trash2 size={18} />
                                        </button>
                                    </td>
                                </tr>
                            ))}
                            {entitlements.length === 0 && (
                                <tr>
                                    <td colSpan="7" className="px-6 py-12 text-center text-slate-500">
                                        No entitlements found. Create one to get started.
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Modal */}
            {showModal && (
                <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-xl shadow-2xl w-full max-w-lg overflow-hidden animate-in fade-in zoom-in duration-200">
                        <div className="px-6 py-4 border-b border-slate-100 flex justify-between items-center bg-slate-50">
                            <h3 className="text-lg font-bold text-slate-900">Add New Entitlement</h3>
                            <button onClick={() => setShowModal(false)} className="text-slate-400 hover:text-slate-600">
                                <X size={20} />
                            </button>
                        </div>

                        <form onSubmit={handleSubmit} className="p-6 space-y-4">
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-sm font-medium text-slate-700 mb-1">Subject Type</label>
                                    <select
                                        value={formData.subjectType}
                                        onChange={(e) => setFormData({ ...formData, subjectType: e.target.value })}
                                        className="input-field"
                                    >
                                        <option value="USER">User</option>
                                        <option value="ROLE">Role</option>
                                        <option value="GROUP">Group</option>
                                    </select>
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-slate-700 mb-1">Subject ID</label>
                                    <input
                                        type="text"
                                        value={formData.subjectId}
                                        onChange={(e) => setFormData({ ...formData, subjectId: e.target.value })}
                                        placeholder="e.g., admin"
                                        className="input-field"
                                        required
                                    />
                                </div>
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <MultiSelect
                                        label="Actions"
                                        options={actionOptions}
                                        value={formData.actions}
                                        onChange={(newActions) => setFormData({ ...formData, actions: newActions })}
                                        placeholder="Select actions..."
                                    />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-slate-700 mb-1">Effect</label>
                                    <select
                                        value={formData.effect}
                                        onChange={(e) => setFormData({ ...formData, effect: e.target.value })}
                                        className="input-field"
                                    >
                                        <option value="ALLOW">Allow</option>
                                        <option value="DENY">Deny</option>
                                    </select>
                                </div>
                            </div>

                            <div className="border-t border-slate-100 pt-4 mt-2">
                                <label className="block text-sm font-medium text-slate-700 mb-1">Resource Type</label>
                                <select
                                    value={formData.resourceType}
                                    onChange={handleResourceTypeChange}
                                    className="input-field mb-3"
                                    required
                                >
                                    <option value="">Select Resource Type</option>
                                    {providers.map(p => (
                                        <option key={p.id} value={p.resourceType}>{p.resourceType} ({p.serviceName})</option>
                                    ))}
                                    <option value="custom">Custom / Other</option>
                                </select>

                                {formData.resourceType === 'custom' ? (
                                    <div className="grid grid-cols-2 gap-4">
                                        <div>
                                            <label className="block text-sm font-medium text-slate-700 mb-1">Custom Type</label>
                                            <input
                                                type="text"
                                                onChange={(e) => setFormData({ ...formData, resourceType: e.target.value })}
                                                placeholder="e.g. widget"
                                                className="input-field"
                                            />
                                        </div>
                                        <div>
                                            <label className="block text-sm font-medium text-slate-700 mb-1">Resource IDs</label>
                                            <input
                                                type="text"
                                                value={formData.resourceIds.join(', ')}
                                                onChange={(e) => setFormData({ ...formData, resourceIds: e.target.value.split(',').map(s => s.trim()) })}
                                                placeholder="e.g., 12345, 67890"
                                                className="input-field"
                                                required
                                            />
                                            <p className="text-xs text-slate-400 mt-1">Comma separated values</p>
                                        </div>
                                    </div>
                                ) : (
                                    <>
                                        {providerSchema && (
                                            <DynamicFilter
                                                schema={providerSchema}
                                                filters={activeFilters}
                                                onChange={setActiveFilters}
                                                onSearch={handleSearchResources}
                                            />
                                        )}

                                        <div className="flex justify-between items-center mb-1">
                                            <label className="block text-sm font-medium text-slate-700">Resource IDs</label>
                                            <button
                                                type="button"
                                                onClick={handleSearchResources}
                                                className="text-xs text-brand-600 hover:text-brand-800 flex items-center gap-1"
                                                title="Refresh Resources"
                                            >
                                                <RefreshCw size={12} /> Refresh
                                            </button>
                                        </div>

                                        <MultiSelect
                                            options={resourceOptions}
                                            value={formData.resourceIds}
                                            onChange={(newIds) => setFormData({ ...formData, resourceIds: newIds })}
                                            placeholder="Select resources..."
                                        />

                                        {fetchedResources.length === 0 && formData.resourceType && (
                                            <p className="text-xs text-amber-600 mt-1 flex items-center gap-1">
                                                <AlertTriangle size={12} />
                                                No resources found or provider not reachable.
                                            </p>
                                        )}
                                    </>
                                )}
                            </div>

                            <div className="flex gap-3 pt-4 border-t border-slate-100">
                                <button
                                    type="button"
                                    onClick={() => setShowModal(false)}
                                    className="btn-secondary flex-1"
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    className="btn-primary flex-1"
                                >
                                    Create Entitlement
                                </button>
                            </div>
                        </form>
                    </div>
                </div >
            )}
        </div >
    );
};

export default Entitlements;
