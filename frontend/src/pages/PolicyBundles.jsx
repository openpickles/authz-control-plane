import React, { useState, useEffect, useCallback } from 'react';
import { Plus, Download, Package, Box } from 'lucide-react';
import { policyBundleService, policyBindingService } from '../services/api';
import DataGrid from '../components/DataGrid';
import SlideOver from '../components/SlideOver';

const PolicyBundles = () => {
    // List State
    const [bundles, setBundles] = useState([]);
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(0);
    const [size] = useState(10); // Standard page size
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [search, setSearch] = useState('');

    // Creation State
    const [showCreate, setShowCreate] = useState(false);
    const [bindings, setBindings] = useState([]); // For selection in Create
    const [formData, setFormData] = useState({
        name: '',
        description: '',
        bindingIds: [],
        wasmEnabled: false,
        entrypoint: 'allow'
    });

    // Load Bundles (Paginated)
    const loadBundles = useCallback(async () => {
        setLoading(true);
        try {
            const params = { page, size, search };
            const response = await policyBundleService.getAll(params);
            // Default to empty list if no content
            const data = response.data.content || [];
            setBundles(data);
            setTotalElements(response.data.totalElements || 0);
            setTotalPages(response.data.totalPages || 0);
        } catch (error) {
            console.error('Error loading bundles:', error);
            setBundles([]);
        } finally {
            setLoading(false);
        }
    }, [page, size, search]);

    // Load Bindings (for Creation Form) - Load once
    useEffect(() => {
        const fetchBindings = async () => {
            try {
                // TODO: In future, this picker might also need pagination or search
                const response = await policyBindingService.getAll({ size: 100 });
                setBindings(response.data.content || []);
            } catch (error) {
                console.error("Error loading bindings", error);
            }
        };
        fetchBindings();
    }, []);

    useEffect(() => {
        const timer = setTimeout(() => loadBundles(), 300);
        return () => clearTimeout(timer);
    }, [loadBundles]);

    // Handlers
    const handleDownload = (id) => {
        policyBundleService.download(id);
    };

    const handleCreate = async (e) => {
        e.preventDefault();
        try {
            await policyBundleService.create(formData);
            setShowCreate(false);
            setFormData({ name: '', description: '', bindingIds: [], wasmEnabled: false, entrypoint: 'allow' });
            loadBundles(); // Refresh list
        } catch (error) {
            console.error('Error creating bundle:', error);
        }
    };

    const toggleBindingSelection = (id) => {
        setFormData(prev => {
            const newIds = prev.bindingIds.includes(id)
                ? prev.bindingIds.filter(bid => bid !== id)
                : [...prev.bindingIds, id];
            return { ...prev, bindingIds: newIds };
        });
    };

    // Columns Definition
    const columns = [
        {
            header: 'Bundle Name',
            accessor: 'name',
            className: 'w-1/4',
            render: (row) => (
                <div className="flex items-center gap-3">
                    <div className="p-2 bg-indigo-50 text-indigo-600 rounded-lg">
                        <Package size={18} />
                    </div>
                    <div>
                        <div className="font-semibold text-slate-900">{row.name}</div>
                        <div className="text-xs text-slate-500 font-mono">ID: {row.id}</div>
                    </div>
                </div>
            )
        },
        {
            header: 'Description',
            accessor: 'description',
            className: 'w-1/3',
            render: (row) => <span className="text-slate-600 truncate block max-w-xs" title={row.description}>{row.description || '-'}</span>
        },
        {
            header: 'Configuration',
            accessor: 'config',
            render: (row) => (
                <div className="flex gap-2">
                    <span className="inline-flex items-center rounded-md bg-slate-50 px-2 py-1 text-xs font-medium text-slate-600 ring-1 ring-inset ring-slate-500/10">
                        {row.bindingIds?.length || 0} Bindings
                    </span>
                    {row.wasmEnabled ? (
                        <span className="inline-flex items-center rounded-md bg-amber-50 px-2 py-1 text-xs font-medium text-amber-700 ring-1 ring-inset ring-amber-600/20">
                            WASM
                        </span>
                    ) : (
                        <span className="inline-flex items-center rounded-md bg-blue-50 px-2 py-1 text-xs font-medium text-blue-700 ring-1 ring-inset ring-blue-700/10">
                            JSON
                        </span>
                    )}
                </div>
            )
        },
        {
            header: 'Actions',
            accessor: 'actions',
            className: 'text-right',
            render: (row) => (
                <div className="flex justify-end">
                    <button
                        onClick={() => handleDownload(row.id)}
                        className="text-slate-400 hover:text-indigo-600 transition-colors p-1"
                        title="Download Bundle"
                    >
                        <Download size={18} />
                    </button>
                </div>
            )
        }
    ];

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center mb-6">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-slate-900">Policy Bundles</h1>
                    <p className="mt-2 text-sm text-slate-500">
                        Manage and distribute your policy compilations.
                    </p>
                </div>
            </div>

            <DataGrid
                columns={columns}
                data={bundles}
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
                        Create Bundle
                    </button>
                }
            />

            {/* Creation SlideOver */}
            <SlideOver
                isOpen={showCreate}
                onClose={() => setShowCreate(false)}
                title="Create New Bundle"
                size="md"
            >
                <form onSubmit={handleCreate} className="space-y-8">
                    {/* Basic Info */}
                    <div className="space-y-4">
                        <h4 className="text-sm font-medium text-slate-900 border-b border-slate-100 pb-2">General Information</h4>
                        <div>
                            <label className="block text-sm font-medium text-slate-700 mb-1">Bundle Name</label>
                            <input
                                type="text"
                                value={formData.name}
                                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                placeholder="e.g., Payment Service Policies"
                                className="input-field"
                                required
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-slate-700 mb-1">Description</label>
                            <textarea
                                value={formData.description}
                                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                                placeholder="Briefly describe the purpose of this bundle..."
                                className="input-field min-h-[80px]"
                                rows={3}
                            />
                        </div>
                    </div>

                    {/* WASM Config */}
                    <div className="bg-slate-50 rounded-lg p-4 border border-slate-200 space-y-4">
                        <div className="flex items-start">
                            <div className="flex items-center h-5">
                                <input
                                    id="wasm"
                                    type="checkbox"
                                    checked={formData.wasmEnabled}
                                    onChange={(e) => setFormData({ ...formData, wasmEnabled: e.target.checked })}
                                    className="h-4 w-4 text-indigo-600 border-slate-300 rounded focus:ring-indigo-500"
                                />
                            </div>
                            <div className="ml-3 text-sm">
                                <label htmlFor="wasm" className="font-medium text-slate-900">Enable WASM Compilation</label>
                                <p className="text-slate-500">Compiles logic to WebAssembly for high-performance evaluation.</p>
                            </div>
                        </div>

                        {formData.wasmEnabled && (
                            <div className="pl-7 animate-in fade-in slide-in-from-top-2">
                                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500 mb-1">Entrypoint Rule</label>
                                <div className="flex rounded-md shadow-sm ring-1 ring-inset ring-slate-300 bg-white">
                                    <span className="flex select-none items-center pl-3 text-slate-500 sm:text-sm">root/</span>
                                    <input
                                        type="text"
                                        value={formData.entrypoint}
                                        onChange={(e) => setFormData({ ...formData, entrypoint: e.target.value })}
                                        className="block flex-1 border-0 bg-transparent py-1.5 pl-1 text-slate-900 placeholder:text-slate-400 focus:ring-0 sm:text-sm sm:leading-6"
                                        placeholder="allow"
                                    />
                                </div>
                            </div>
                        )}
                    </div>

                    {/* Bindings Selection */}
                    <div>
                        <h4 className="text-sm font-medium text-slate-900 border-b border-slate-100 pb-2 mb-4">Include Policies</h4>
                        <div className="space-y-2 max-h-96 overflow-y-auto pr-2">
                            {bindings.map(binding => (
                                <div
                                    key={binding.id}
                                    onClick={() => toggleBindingSelection(binding.id)}
                                    className={`relative flex cursor-pointer rounded-lg border p-4 shadow-sm focus:outline-none transition-all ${formData.bindingIds.includes(binding.id)
                                        ? 'border-indigo-600 ring-2 ring-indigo-600 bg-indigo-50/50'
                                        : 'border-slate-300 hover:border-slate-400 bg-white'
                                        }`}
                                >
                                    <div className="flex w-full items-center justify-between">
                                        <div className="flex items-center">
                                            <div className="text-sm">
                                                <p className={`font-medium ${formData.bindingIds.includes(binding.id) ? 'text-indigo-900' : 'text-slate-900'}`}>
                                                    {binding.resourceType}
                                                </p>
                                                <p className={`text-slate-500 ${formData.bindingIds.includes(binding.id) ? 'text-indigo-700' : 'text-slate-500'}`}>
                                                    Context: {binding.context}
                                                </p>
                                            </div>
                                        </div>
                                        <div className={`shrink-0 text-indigo-600 ${formData.bindingIds.includes(binding.id) ? 'opacity-100' : 'opacity-0'}`}>
                                            <Box className="h-5 w-5" fill="currentColor" />
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Actions */}
                    <div className="pt-6 flex items-center justify-end gap-x-6 border-t border-slate-900/10">
                        <button type="button" className="text-sm font-semibold leading-6 text-slate-900" onClick={() => setShowCreate(false)}>
                            Cancel
                        </button>
                        <button
                            type="submit"
                            className="rounded-md bg-indigo-600 px-8 py-2 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
                        >
                            Create Bundle
                        </button>
                    </div>
                </form>
            </SlideOver>
        </div>
    );
};

export default PolicyBundles;
