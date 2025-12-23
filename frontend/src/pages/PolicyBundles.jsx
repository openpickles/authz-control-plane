import React, { useState, useEffect } from 'react';
import { Plus, Download, Package, X } from 'lucide-react';
import { policyBundleService, policyBindingService } from '../services/api';

const PolicyBundles = () => {
    const [bundles, setBundles] = useState([]);
    const [bindings, setBindings] = useState([]);
    const [showModal, setShowModal] = useState(false);
    const [formData, setFormData] = useState({
        name: '',
        description: '',
        bindingIds: []
    });

    const loadBundles = React.useCallback(async () => {
        try {
            const response = await policyBundleService.getAll();
            setBundles(response.data);
        } catch (error) {
            console.error('Error loading bundles:', error);
        }
    }, []);

    const loadBindings = React.useCallback(async () => {
        try {
            const response = await policyBindingService.getAll();
            setBindings(response.data);
        } catch (error) {
            console.error('Error loading bindings:', error);
        }
    }, []);

    useEffect(() => {
        loadBundles();
        loadBindings();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await policyBundleService.create(formData);
            setShowModal(false);
            setFormData({ name: '', description: '', bindingIds: [] });
            loadBundles();
        } catch (error) {
            console.error('Error creating bundle:', error);
        }
    };

    const handleDownload = (id) => {
        policyBundleService.download(id);
    };

    const toggleBindingSelection = (id) => {
        setFormData(prev => {
            const newIds = prev.bindingIds.includes(id)
                ? prev.bindingIds.filter(bid => bid !== id)
                : [...prev.bindingIds, id];
            return { ...prev, bindingIds: newIds };
        });
    };

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h2 className="text-2xl font-bold text-slate-900">Policy Bundles</h2>
                    <p className="text-slate-500 mt-1">Group policy bindings into downloadable bundles.</p>
                </div>
                <button
                    onClick={() => setShowModal(true)}
                    className="btn-primary flex items-center gap-2"
                >
                    <Plus size={18} />
                    New Bundle
                </button>
            </div>

            {/* Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {bundles.map((bundle) => (
                    <div key={bundle.id} className="card p-6 hover:shadow-md transition-shadow">
                        <div className="flex justify-between items-start mb-4">
                            <div className="p-3 bg-indigo-50 rounded-lg text-indigo-600">
                                <Package size={24} />
                            </div>
                            <button
                                onClick={() => handleDownload(bundle.id)}
                                className="text-slate-400 hover:text-indigo-600 transition-colors"
                                title="Download Bundle"
                            >
                                <Download size={20} />
                            </button>
                        </div>
                        <h3 className="text-lg font-bold text-slate-900 mb-2">{bundle.name}</h3>
                        <p className="text-slate-600 text-sm mb-4">{bundle.description}</p>
                        <div className="flex items-center gap-2 text-sm text-slate-500">
                            <span className="font-medium text-slate-900">{bundle.bindingIds.length}</span>
                            Bindings Included
                        </div>
                    </div>
                ))}
            </div>

            {bundles.length === 0 && (
                <div className="text-center py-12 text-slate-500">
                    No bundles found. Create one to get started.
                </div>
            )}

            {/* Modal */}
            {showModal && (
                <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-xl shadow-2xl w-full max-w-2xl overflow-hidden animate-in fade-in zoom-in duration-200">
                        <div className="px-6 py-4 border-b border-slate-100 flex justify-between items-center bg-slate-50">
                            <h3 className="text-lg font-bold text-slate-900">Create Policy Bundle</h3>
                            <button onClick={() => setShowModal(false)} className="text-slate-400 hover:text-slate-600">
                                <X size={20} />
                            </button>
                        </div>

                        <form onSubmit={handleSubmit} className="p-6 space-y-6">
                            <div className="grid grid-cols-2 gap-6">
                                <div>
                                    <label className="block text-sm font-medium text-slate-700 mb-1">Bundle Name</label>
                                    <input
                                        type="text"
                                        value={formData.name}
                                        onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                        placeholder="e.g., Loan Service Bundle"
                                        className="input-field"
                                        required
                                    />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-slate-700 mb-1">Description</label>
                                    <input
                                        type="text"
                                        value={formData.description}
                                        onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                                        placeholder="Optional description"
                                        className="input-field"
                                    />
                                </div>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-slate-700 mb-3">Select Policy Bindings</label>
                                <div className="border border-slate-200 rounded-lg max-h-60 overflow-y-auto divide-y divide-slate-100">
                                    {bindings.map(binding => (
                                        <label key={binding.id} className="flex items-center p-3 hover:bg-slate-50 cursor-pointer transition-colors">
                                            <input
                                                type="checkbox"
                                                checked={formData.bindingIds.includes(binding.id)}
                                                onChange={() => toggleBindingSelection(binding.id)}
                                                className="w-4 h-4 text-indigo-600 border-slate-300 rounded focus:ring-indigo-500"
                                            />
                                            <div className="ml-3 flex-1 grid grid-cols-3 gap-4 text-sm">
                                                <span className="font-medium text-slate-900">{binding.resourceType}</span>
                                                <span className="text-slate-600">{binding.context}</span>
                                                <span className="text-slate-500 font-mono text-xs">{(binding.policyIds || []).length} policies</span>
                                            </div>
                                        </label>
                                    ))}
                                    {bindings.length === 0 && (
                                        <div className="p-4 text-center text-slate-500 text-sm">
                                            No bindings available. Create bindings first.
                                        </div>
                                    )}
                                </div>
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
                                    Create Bundle
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default PolicyBundles;
