import React, { useState, useEffect, useCallback } from 'react';
import { Plus, Trash2, Link as LinkIcon, Box } from 'lucide-react';
import { policyBindingService, resourceTypeService, policyService } from '../services/api';
import MultiSelect from '../components/MultiSelect';
import DataGrid from '../components/DataGrid';
import SlideOver from '../components/SlideOver';

const PolicyBindings = () => {
    // List State
    const [bindings, setBindings] = useState([]);
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(0);
    const [size] = useState(10);
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [search, setSearch] = useState('');

    // Shared State for Rendering and Form
    const [policies, setPolicies] = useState([]);
    const [resourceTypes, setResourceTypes] = useState([]);

    // Creation State
    const [showCreate, setShowCreate] = useState(false);
    const [formData, setFormData] = useState({
        resourceType: '',
        context: '',
        policyIds: [],
        evaluationMode: 'DIRECT'
    });

    // Load Bindings (Paginated)
    const loadBindings = useCallback(async () => {
        setLoading(true);
        try {
            const params = { page, size, search };
            const response = await policyBindingService.getAll(params);
            const data = response.data.content || (Array.isArray(response.data) ? response.data : []);
            setBindings(data);
            setTotalElements(response.data.totalElements || data.length);
            setTotalPages(response.data.totalPages || 1);
        } catch (error) {
            console.error('Error loading bindings:', error);
        } finally {
            setLoading(false);
        }
    }, [page, size, search]);

    // Load Helpers (Policies and Providers)
    const loadHelpers = useCallback(async () => {
        try {
            // Load Resource Types (Page)
            const typeRes = await resourceTypeService.getAll({ size: 100 });
            setResourceTypes(typeRes.data.content || (Array.isArray(typeRes.data) ? typeRes.data : []));

            // Load Policies (now Page) - Get first 100 for dropdown
            const polRes = await policyService.getAll({ size: 100 });
            setPolicies(polRes.data.content || (Array.isArray(polRes.data) ? polRes.data : []));
        } catch (error) {
            console.error('Error loading helpers:', error);
        }
    }, []);

    useEffect(() => {
        const timer = setTimeout(() => loadBindings(), 300);
        return () => clearTimeout(timer);
    }, [loadBindings]);

    useEffect(() => {
        loadHelpers();
    }, [loadHelpers]);

    // Handlers
    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await policyBindingService.create(formData);
            setShowCreate(false);
            setFormData({ resourceType: '', context: '', policyIds: [], evaluationMode: 'DIRECT' });
            loadBindings();
        } catch (error) {
            console.error('Error creating binding:', error);
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Delete this binding?')) {
            try {
                await policyBindingService.delete(id);
                loadBindings();
            } catch (error) {
                console.error('Error deleting binding:', error);
            }
        }
    };

    const getPolicyName = (id) => {
        const policy = policies.find(p => p.id === id);
        return policy ? policy.name : id;
    };

    const contextOptions = [
        'fine_grained_access',
        'authorization',
        'list_allowed_resources',
        'list_allowed_actions',
        'resource_filtering',
        'ui_access'
    ];

    const evaluationModes = ['DIRECT', 'ATTRIBUTE', 'CONDITION'];

    // Columns
    const columns = [
        {
            header: 'Resource Type',
            accessor: 'resourceType',
            className: 'w-1/4',
            render: (row) => (
                <div className="flex items-center gap-3">
                    <div className="p-2 bg-slate-100 rounded-lg text-slate-500">
                        <Box size={18} />
                    </div>
                    <span className="font-mono text-sm font-medium text-slate-900">{row.resourceType}</span>
                </div>
            )
        },
        {
            header: 'Context',
            accessor: 'context',
            render: (row) => (
                <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-indigo-50 text-indigo-700 ring-1 ring-inset ring-indigo-700/10">
                    {row.context}
                </span>
            )
        },
        {
            header: 'Policies',
            accessor: 'policies',
            render: (row) => (
                <div className="flex flex-wrap gap-1">
                    {(row.policyIds || []).map(pid => (
                        <span key={pid} className="inline-flex items-center gap-1 px-2 py-0.5 bg-slate-50 rounded text-xs border border-slate-200 text-slate-600">
                            <LinkIcon size={10} className="text-slate-400" />
                            {getPolicyName(pid)}
                        </span>
                    ))}
                    {(!row.policyIds || row.policyIds.length === 0) && <span className="text-slate-400 text-xs italic">No policies</span>}
                </div>
            )
        },
        {
            header: 'Mode',
            accessor: 'evaluationMode',
            render: (row) => (
                <span className="text-xs font-medium text-slate-500 bg-slate-100 px-2 py-1 rounded">
                    {row.evaluationMode || 'DIRECT'}
                </span>
            )
        },
        {
            header: 'Actions',
            accessor: 'actions',
            className: 'text-right',
            render: (row) => (
                <div className="flex justify-end">
                    <button
                        onClick={() => handleDelete(row.id)}
                        className="text-slate-400 hover:text-red-600 transition-colors p-1"
                        title="Delete Binding"
                    >
                        <Trash2 size={18} />
                    </button>
                </div>
            )
        }
    ];

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center mb-6">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-slate-900">Policy Bindings</h1>
                    <p className="mt-2 text-sm text-slate-500">
                        Bind policies to specific resource types and execution contexts.
                    </p>
                </div>
            </div>

            <DataGrid
                columns={columns}
                data={bindings}
                loading={loading}
                page={page}
                size={size}
                totalElements={totalElements}
                totalPages={totalPages}
                onPageChange={setPage}
                onSearch={setSearch}
                actionButton={
                    <button
                        onClick={() => setShowCreate(true)}
                        className="btn-primary flex items-center gap-2 text-sm"
                    >
                        <Plus size={16} />
                        Create Binding
                    </button>
                }
            />

            {/* Creation SlideOver */}
            <SlideOver
                isOpen={showCreate}
                onClose={() => setShowCreate(false)}
                title="Create Policy Binding"
                size="md"
            >
                <form onSubmit={handleSubmit} className="space-y-6">
                    <div>
                        <label className="block text-sm font-medium text-slate-700 mb-1">Resource Type</label>
                        <select
                            value={formData.resourceType}
                            onChange={(e) => setFormData({ ...formData, resourceType: e.target.value })}
                            className="input-field"
                            required
                        >
                            <option value="">Select Resource Type</option>
                            {resourceTypes.map(p => (
                                <option key={p.id} value={p.key}>{p.name} ({p.key})</option>
                            ))}
                            <option value="custom">Custom / Other</option>
                        </select>
                        {formData.resourceType === 'custom' && (
                            <input
                                type="text"
                                onChange={(e) => setFormData({ ...formData, resourceType: e.target.value })}
                                placeholder="e.g. widget"
                                className="input-field mt-2"
                                required
                            />
                        )}
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-slate-700 mb-1">Context</label>
                        <input
                            type="text"
                            list="contexts"
                            value={formData.context}
                            onChange={(e) => setFormData({ ...formData, context: e.target.value })}
                            placeholder="e.g., fine_grained_access"
                            className="input-field"
                            required
                        />
                        <datalist id="contexts">
                            {contextOptions.map(p => (
                                <option key={p} value={p} />
                            ))}
                        </datalist>
                        <p className="text-xs text-slate-500 mt-1">Defines when this binding is applied.</p>
                    </div>

                    <div>
                        <MultiSelect
                            label="Policies"
                            options={policies.map(p => ({
                                value: p.id,
                                label: `${p.name} (${p.filename || 'no-file'})`
                            }))}
                            value={formData.policyIds}
                            onChange={(newIds) => setFormData({ ...formData, policyIds: newIds })}
                            placeholder="Select policies..."
                        />
                        <p className="text-xs text-slate-500 mt-1">Policies to execute for this resource/context.</p>
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-slate-700 mb-1">Evaluation Mode</label>
                        <select
                            value={formData.evaluationMode}
                            onChange={(e) => setFormData({ ...formData, evaluationMode: e.target.value })}
                            className="input-field"
                            required
                        >
                            {evaluationModes.map(mode => (
                                <option key={mode} value={mode}>{mode}</option>
                            ))}
                        </select>
                    </div>

                    <div className="pt-6 flex items-center justify-end gap-x-6 border-t border-slate-900/10">
                        <button type="button" className="text-sm font-semibold leading-6 text-slate-900" onClick={() => setShowCreate(false)}>
                            Cancel
                        </button>
                        <button
                            type="submit"
                            className="rounded-md bg-indigo-600 px-8 py-2 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
                        >
                            Create Binding
                        </button>
                    </div>
                </form>
            </SlideOver>
        </div>
    );
};

export default PolicyBindings;
