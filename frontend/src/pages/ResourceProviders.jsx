import React, { useState, useEffect } from 'react';
import { Server, Plus, Trash2, Globe, Link as LinkIcon, X } from 'lucide-react';
import { resourceProviderService } from '../services/api';

const ResourceProviders = () => {
    const [providers, setProviders] = useState([]);
    const [showModal, setShowModal] = useState(false);
    const [formData, setFormData] = useState({
        serviceName: '',
        baseUrl: '',
        resourceType: '',
        fetchEndpoint: ''
    });

    const loadProviders = React.useCallback(async () => {
        try {
            const response = await resourceProviderService.getAll();
            setProviders(response.data);
        } catch (error) {
            console.error('Error loading providers:', error);
        }
    }, []);

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        loadProviders();
    }, [loadProviders]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await resourceProviderService.create(formData);
            setShowModal(false);
            setFormData({ serviceName: '', baseUrl: '', resourceType: '', fetchEndpoint: '' });
            loadProviders();
        } catch (error) {
            console.error('Error creating provider:', error);
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Delete this provider?')) {
            try {
                await resourceProviderService.delete(id);
                loadProviders();
            } catch (error) {
                console.error('Error deleting provider:', error);
            }
        }
    };

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h2 className="text-2xl font-bold text-slate-900">Resource Providers</h2>
                    <p className="text-slate-500 mt-1">Register microservices to fetch resources from.</p>
                </div>
                <button
                    onClick={() => setShowModal(true)}
                    className="btn-primary flex items-center gap-2"
                >
                    <Plus size={18} />
                    Register Service
                </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {providers.map((provider) => (
                    <div key={provider.id} className="card p-6 hover:shadow-card-hover transition-all group">
                        <div className="flex justify-between items-start mb-4">
                            <div className="p-3 bg-brand-50 rounded-lg text-brand-600 group-hover:bg-brand-100 transition-colors">
                                <Server size={24} />
                            </div>
                            <button onClick={() => handleDelete(provider.id)} className="text-slate-400 hover:text-red-500 p-1">
                                <Trash2 size={18} />
                            </button>
                        </div>
                        <h3 className="text-lg font-bold text-slate-900 mb-2">{provider.serviceName}</h3>
                        <div className="space-y-3 text-sm">
                            <div className="flex items-center gap-2 text-slate-600">
                                <Globe size={14} className="text-slate-400" />
                                <span className="truncate">{provider.baseUrl}</span>
                            </div>
                            <div className="bg-slate-50 p-3 rounded-md border border-slate-100 space-y-1">
                                <div className="flex justify-between">
                                    <span className="text-slate-500 text-xs uppercase font-semibold">Type</span>
                                    <span className="text-brand-700 font-medium">{provider.resourceType}</span>
                                </div>
                                <div className="flex justify-between">
                                    <span className="text-slate-500 text-xs uppercase font-semibold">Endpoint</span>
                                    <span className="text-green-700 font-mono text-xs">{provider.fetchEndpoint}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                ))}

                {/* Add New Card Placeholder */}
                <button
                    onClick={() => setShowModal(true)}
                    className="border-2 border-dashed border-slate-200 rounded-lg p-6 flex flex-col items-center justify-center text-slate-400 hover:border-brand-300 hover:text-brand-600 hover:bg-brand-50/50 transition-all h-full min-h-[200px]"
                >
                    <Plus size={32} className="mb-2" />
                    <span className="font-medium">Register New Provider</span>
                </button>
            </div>

            {/* Modal */}
            {showModal && (
                <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-xl shadow-2xl w-full max-w-md overflow-hidden animate-in fade-in zoom-in duration-200">
                        <div className="px-6 py-4 border-b border-slate-100 bg-slate-50 flex justify-between items-center">
                            <h3 className="text-lg font-bold text-slate-900">Register Microservice</h3>
                            <button onClick={() => setShowModal(false)} className="text-slate-400 hover:text-slate-600">
                                <X size={20} />
                            </button>
                        </div>
                        <form onSubmit={handleSubmit} className="p-6 space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-slate-700 mb-1">Service Name</label>
                                <input
                                    type="text"
                                    value={formData.serviceName}
                                    onChange={(e) => setFormData({ ...formData, serviceName: e.target.value })}
                                    placeholder="e.g., Loan Service"
                                    className="input-field"
                                    required
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-slate-700 mb-1">Base URL</label>
                                <input
                                    type="text"
                                    value={formData.baseUrl}
                                    onChange={(e) => setFormData({ ...formData, baseUrl: e.target.value })}
                                    placeholder="e.g., http://localhost:8081"
                                    className="input-field"
                                    required
                                />
                            </div>
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-sm font-medium text-slate-700 mb-1">Resource Type</label>
                                    <input
                                        type="text"
                                        value={formData.resourceType}
                                        onChange={(e) => setFormData({ ...formData, resourceType: e.target.value })}
                                        placeholder="e.g., loan-account"
                                        className="input-field"
                                        required
                                    />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-slate-700 mb-1">Fetch Endpoint</label>
                                    <input
                                        type="text"
                                        value={formData.fetchEndpoint}
                                        onChange={(e) => setFormData({ ...formData, fetchEndpoint: e.target.value })}
                                        placeholder="e.g., /api/loans"
                                        className="input-field"
                                        required
                                    />
                                </div>
                            </div>
                            <div className="flex gap-3 pt-4">
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
                                    Register
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ResourceProviders;
