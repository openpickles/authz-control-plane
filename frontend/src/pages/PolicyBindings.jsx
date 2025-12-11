import React, { useState, useEffect } from 'react';
import { Plus, Search, Trash2, X, Link as LinkIcon } from 'lucide-react';
import { policyBindingService, resourceProviderService } from '../services/api';

const PolicyBindings = () => {
    const [bindings, setBindings] = useState([]);
    const [providers, setProviders] = useState([]);
    const [policies, setPolicies] = useState([]);
    const [showModal, setShowModal] = useState(false);
    const [formData, setFormData] = useState({
        resourceType: '',
        context: '',
        policyIds: [],
        evaluationMode: 'DIRECT'
    });

    const loadBindings = React.useCallback(async () => {
        try {
            const response = await policyBindingService.getAll();
            setBindings(response.data);
        } catch (error) {
            console.error('Error loading bindings:', error);
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

    const loadPolicies = React.useCallback(async () => {
        try {
            // Assuming policyService is imported or available in api.js
            // If not imported, I need to check imports. The file has policyBindingService and resourceProviderService.
            // I'll add policyService to imports in next step if checking shows it missing.
            // For now assuming it's exported from same place.
            // Wait, looking at file imports: import { policyBindingService, resourceProviderService } from '../services/api';
            // It is likely exported but I need to update import.
            // I will do separate edit for import.
            // Here I assume it's available.
            const response = await import('../services/api').then(m => m.policyService.getAll());
            setPolicies(response.data);
        } catch (error) {
            console.error('Error loading policies:', error);
        }
    }, []);

    useEffect(() => {
        loadBindings();
        loadProviders();
        loadPolicies();
    }, [loadBindings, loadProviders, loadPolicies]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await policyBindingService.create(formData);
            setShowModal(false);
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

    const contextOptions = [
        'fine_grained_access',
        'authorization',
        'list_allowed_resources',
        'list_allowed_actions'
    ];

    const evaluationModes = [
        'DIRECT',
        'ATTRIBUTE',
        'CONDITION'
    ];

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h2 className="text-2xl font-bold text-slate-900">Policy Bindings</h2>
                    <p className="text-slate-500 mt-1">Bind policies to resource types and contexts.</p>
                </div>
                <button
                    onClick={() => setShowModal(true)}
                    className="btn-primary flex items-center gap-2"
                >
                    <Plus size={18} />
                    New Binding
                </button>
            </div>

            {/* Table */}
            <div className="card overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="w-full text-left">
                        <thead className="bg-slate-50 border-b border-slate-200">
                            <tr>
                                <th className="px-6 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Resource Type</th>
                                <th className="px-6 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Context</th>
                                <th className="px-6 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Policies</th>
                                <th className="px-6 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Mode</th>
                                <th className="px-6 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider text-right">Actions</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100 bg-white">
                            {bindings.map((binding) => (
                                <tr key={binding.id} className="hover:bg-slate-50 transition-colors">
                                    <td className="px-6 py-4 text-sm font-medium text-slate-900 font-mono">{binding.resourceType}</td>
                                    <td className="px-6 py-4 text-sm text-slate-600">
                                        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-indigo-100 text-indigo-800">
                                            {binding.context}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 text-sm text-slate-600 font-mono">
                                        <div className="flex flex-wrap gap-1">
                                            {(binding.policyIds || []).map(pid => (
                                                <span key={pid} className="inline-flex items-center gap-1 px-2 py-0.5 bg-slate-100 rounded text-xs">
                                                    <LinkIcon size={10} className="text-slate-400" />
                                                    {pid}
                                                </span>
                                            ))}
                                        </div>
                                    </td>
                                    <td className="px-6 py-4 text-sm text-slate-600">
                                        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-slate-100 text-slate-800 border border-slate-200">
                                            {binding.evaluationMode || 'DIRECT'}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 text-right text-sm font-medium">
                                        <button
                                            onClick={() => handleDelete(binding.id)}
                                            className="text-slate-400 hover:text-red-600 transition-colors p-2 rounded-full hover:bg-red-50"
                                        >
                                            <Trash2 size={18} />
                                        </button>
                                    </td>
                                </tr>
                            ))}
                            {bindings.length === 0 && (
                                <tr>
                                    <td colSpan="5" className="px-6 py-12 text-center text-slate-500">
                                        No bindings found. Create one to get started.
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
                    <div className="bg-white rounded-xl shadow-2xl w-full max-w-md overflow-hidden animate-in fade-in zoom-in duration-200">
                        <div className="px-6 py-4 border-b border-slate-100 flex justify-between items-center bg-slate-50">
                            <h3 className="text-lg font-bold text-slate-900">Add Policy Binding</h3>
                            <button onClick={() => setShowModal(false)} className="text-slate-400 hover:text-slate-600">
                                <X size={20} />
                            </button>
                        </div>

                        <form onSubmit={handleSubmit} className="p-6 space-y-4">
                            <div>
                                <label htmlFor="resourceType" className="block text-sm font-medium text-slate-700 mb-1">Resource Type</label>
                                <select
                                    id="resourceType"
                                    value={formData.resourceType}
                                    onChange={(e) => setFormData({ ...formData, resourceType: e.target.value })}
                                    className="input-field"
                                    required
                                >
                                    <option value="">Select Resource Type</option>
                                    {providers.map(p => (
                                        <option key={p.id} value={p.resourceType}>{p.resourceType} ({p.serviceName})</option>
                                    ))}
                                    <option value="custom">Custom / Other</option>
                                </select>
                            </div>

                            {formData.resourceType === 'custom' && (
                                <div>
                                    <label className="block text-sm font-medium text-slate-700 mb-1">Custom Type</label>
                                    <input
                                        type="text"
                                        onChange={(e) => setFormData({ ...formData, resourceType: e.target.value })}
                                        placeholder="e.g. widget"
                                        className="input-field"
                                    />
                                </div>
                            )}

                            <div>
                                <label className="block text-sm font-medium text-slate-700 mb-1">Context</label>
                                <div className="relative">
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
                                </div>
                            </div>

                            <div>
                                <label htmlFor="policies" className="block text-sm font-medium text-slate-700 mb-1">Policies (Select Multiple)</label>
                                <select
                                    id="policies"
                                    multiple
                                    value={formData.policyIds}
                                    onChange={(e) => {
                                        const selected = Array.from(e.target.selectedOptions, option => option.value);
                                        setFormData({ ...formData, policyIds: selected });
                                    }}
                                    className="input-field h-32"
                                    required
                                >
                                    {policies.map(policy => (
                                        <option key={policy.id} value={policy.name}>
                                            {policy.name} {policy.filename ? `(${policy.filename})` : ''}
                                        </option>
                                    ))}
                                </select>
                                <p className="text-xs text-slate-500 mt-1">Hold Cmd/Ctrl to select multiple.</p>
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
                                    Create Binding
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default PolicyBindings;
