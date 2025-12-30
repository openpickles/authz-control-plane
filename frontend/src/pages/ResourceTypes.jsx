import React, { useState, useEffect, useCallback } from 'react';
import { Server, Plus, Trash2, Globe, RefreshCw, PenSquare, Eye } from 'lucide-react';
import { resourceTypeService } from '../services/api';
import DataGrid from '../components/DataGrid';
import SlideOver from '../components/SlideOver';

const ResourceTypes = () => {
    // List State
    const [types, setTypes] = useState([]);
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(0);
    const [size] = useState(10);
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [search, setSearch] = useState('');

    // Creation/Edit State
    const [showCreate, setShowCreate] = useState(false);
    const [isEditing, setIsEditing] = useState(false);
    const [editId, setEditId] = useState(null);

    // Form Data
    const [formData, setFormData] = useState({
        name: '',
        key: '',
        description: '',
        baseUrl: '',
        dataEndpoint: '',
        metadataEndpoint: '',
        schema: ''
    });

    const [definitionMode, setDefinitionMode] = useState('EXTERNAL'); // 'MANUAL' or 'EXTERNAL'
    const [fetchingSchema, setFetchingSchema] = useState(false);
    const [schemaError, setSchemaError] = useState(null);

    const loadTypes = useCallback(async () => {
        setLoading(true);
        try {
            const params = { page, size, search };
            const response = await resourceTypeService.getAll(params);
            const data = response.data.content || response.data || [];
            setTypes(data);
            setTotalElements(response.data.totalElements || data.length || 0);
            setTotalPages(response.data.totalPages || 1);
        } catch (error) {
            console.error('Error loading resource types:', error);
            setTypes([]);
        } finally {
            setLoading(false);
        }
    }, [page, size, search]);

    useEffect(() => {
        const timer = setTimeout(() => loadTypes(), 300);
        return () => clearTimeout(timer);
    }, [loadTypes]);

    const resetForm = () => {
        setFormData({
            name: '',
            key: '',
            description: '',
            baseUrl: '',
            dataEndpoint: '',
            metadataEndpoint: '',
            schema: ''
        });
        setIsEditing(false);
        setEditId(null);
        setSchemaError(null);
        setDefinitionMode('EXTERNAL');
    };

    const handleEdit = (row) => {
        setFormData({
            name: row.name,
            key: row.key,
            description: row.description || '',
            baseUrl: row.baseUrl || '',
            dataEndpoint: row.dataEndpoint || '',
            metadataEndpoint: row.metadataEndpoint || '',
            schema: row.schema || ''
        });
        setDefinitionMode(row.baseUrl ? 'EXTERNAL' : 'MANUAL');
        setEditId(row.id);
        setIsEditing(true);
        setShowCreate(true);
    };

    const handleFetchSchema = async () => {
        if (!editId) {
            // In create mode, we can't fetch via "refresh" API because ID doesn't exist.
            // But Wait, if it's external, we usually save first then refresh, OR we strictly require ID.
            // Actually, the requirement said "fetch actual data... fetch meta data... while adding". 
            // Ideally we need an endpoint to fetch ad-hoc or we save first. 
            // For simplicity: Save First is better, BUT user might want to see it properly.
            // Let's rely on standard "refresh" after save OR if we have the URL we could proxy.
            // Let's implement a simple "Save & Fetch" flow or just let them save.
            // Re-reading plan: The Backend has `refreshSchema(id)`. 
            // So we must save the entity first. 
            alert("Please save the Resource Type first to enable fetching.");
            return;
        }

        setFetchingSchema(true);
        setSchemaError(null);
        try {
            const response = await resourceTypeService.refreshSchema(editId);
            // response.data could be string or object
            const schemaStr = typeof response.data === 'string' ? response.data : JSON.stringify(response.data, null, 2);
            setFormData(prev => ({ ...prev, schema: schemaStr }));
        } catch (err) {
            console.error(err);
            setSchemaError("Failed to fetch schema. Check URL and Endpoint.");
        } finally {
            setFetchingSchema(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            if (isEditing) {
                await resourceTypeService.update(editId, formData);
            } else {
                const response = await resourceTypeService.create(formData);
                // If External mode and we just created, we might want to prompt fetch?
            }
            setShowCreate(false);
            resetForm();
            loadTypes();
        } catch (error) {
            console.error('Error saving resource type:', error);
            alert('Failed to save resource type');
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure? This may break existing entitlements.')) {
            try {
                await resourceTypeService.delete(id);
                loadTypes();
            } catch (error) {
                console.error('Error deleting resource type:', error);
            }
        }
    };

    const columns = [
        {
            header: 'Name',
            accessor: 'name',
            className: 'font-medium text-slate-900'
        },
        {
            header: 'Key',
            accessor: 'key',
            render: (row) => (
                <span className="font-mono text-xs px-2 py-1 rounded bg-slate-100 text-slate-600">
                    {row.key}
                </span>
            )
        },
        {
            header: 'Source',
            accessor: 'baseUrl',
            render: (row) => row.baseUrl
                ? <span className="flex items-center gap-1 text-xs text-blue-600"><Globe size={12} /> {row.baseUrl}</span>
                : <span className="flex items-center gap-1 text-xs text-orange-600"><PenSquare size={12} /> Manual</span>
        },
        {
            header: 'Actions',
            accessor: 'actions',
            className: 'text-right',
            render: (row) => (
                <div className="flex justify-end gap-2">
                    <button
                        onClick={() => handleEdit(row)}
                        className="text-slate-400 hover:text-blue-500 transition-colors p-1"
                        title="Edit Configuration"
                    >
                        <PenSquare size={18} />
                    </button>
                    <button
                        onClick={() => handleDelete(row.id)}
                        className="text-slate-400 hover:text-red-500 transition-colors p-1"
                        title="Delete Resource Type"
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
                    <h1 className="text-2xl font-bold tracking-tight text-slate-900">Resource Types</h1>
                    <p className="mt-2 text-sm text-slate-500">
                        Define domain data types and their metadata filters.
                    </p>
                </div>
                <button
                    onClick={() => { resetForm(); setShowCreate(true); }}
                    className="btn-primary flex items-center gap-2"
                >
                    <Plus size={16} />
                    Register Type
                </button>
            </div>

            <DataGrid
                columns={columns}
                data={types}
                loading={loading}
                page={page}
                size={size}
                totalElements={totalElements}
                totalPages={totalPages}
                onPageChange={setPage}
                onSearch={setSearch}
                emptyMessage="No Resource Types defined."
            />

            <SlideOver
                isOpen={showCreate}
                onClose={() => setShowCreate(false)}
                title={isEditing ? "Edit Resource Type" : "Register Resource Type"}
            >
                <form onSubmit={handleSubmit} className="space-y-6">
                    {/* Identity Section */}
                    <div className="space-y-4">
                        <h3 className="text-sm font-medium text-slate-900 border-b pb-2">Identity</h3>
                        <div>
                            <label className="block text-sm font-medium text-slate-700">Display Name</label>
                            <input
                                type="text"
                                required
                                className="input-field mt-1"
                                placeholder="e.g. Loan Account"
                                value={formData.name}
                                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-slate-700">Unique Key</label>
                            <div className="mt-1 flex rounded-md shadow-sm">
                                <span className="inline-flex items-center px-3 rounded-l-md border border-r-0 border-slate-300 bg-slate-50 text-slate-500 text-sm">
                                    Type
                                </span>
                                <input
                                    type="text"
                                    required
                                    className="input-field rounded-l-none"
                                    placeholder="e.g. loan-service:loan"
                                    value={formData.key}
                                    onChange={(e) => setFormData({ ...formData, key: e.target.value })}
                                />
                            </div>
                            <p className="mt-1 text-xs text-slate-500">Used in policies and entitlements.</p>
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-slate-700">Description</label>
                            <textarea
                                className="input-field mt-1"
                                rows="2"
                                value={formData.description}
                                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                            />
                        </div>
                    </div>

                    {/* Configuration Mode Toggle */}
                    <div className="space-y-4">
                        <h3 className="text-sm font-medium text-slate-900 border-b pb-2 flex justify-between items-center">
                            Schema Configuration
                            <div className="flex bg-slate-100 rounded-lg p-1">
                                <button
                                    type="button"
                                    onClick={() => setDefinitionMode('EXTERNAL')}
                                    className={`px-3 py-1 text-xs font-medium rounded-md transition-all ${definitionMode === 'EXTERNAL' ? 'bg-white shadow text-blue-600' : 'text-slate-500 hover:text-slate-900'}`}
                                >
                                    External Provider
                                </button>
                                <button
                                    type="button"
                                    onClick={() => setDefinitionMode('MANUAL')}
                                    className={`px-3 py-1 text-xs font-medium rounded-md transition-all ${definitionMode === 'MANUAL' ? 'bg-white shadow text-blue-600' : 'text-slate-500 hover:text-slate-900'}`}
                                >
                                    Manual Definition
                                </button>
                            </div>
                        </h3>

                        {definitionMode === 'EXTERNAL' && (
                            <div className="space-y-4 bg-slate-50 p-4 rounded-lg border border-slate-200">
                                <div>
                                    <label className="block text-sm font-medium text-slate-700">Base URL</label>
                                    <input
                                        type="url"
                                        required={definitionMode === 'EXTERNAL'}
                                        className="input-field mt-1"
                                        placeholder="http://loan-service:8080"
                                        value={formData.baseUrl}
                                        onChange={(e) => setFormData({ ...formData, baseUrl: e.target.value })}
                                    />
                                </div>
                                <div className="grid grid-cols-2 gap-4">
                                    <div>
                                        <label className="block text-sm font-medium text-slate-700">Metadata Endpoint</label>
                                        <input
                                            type="text"
                                            className="input-field mt-1"
                                            placeholder="/api/metadata"
                                            value={formData.metadataEndpoint}
                                            onChange={(e) => setFormData({ ...formData, metadataEndpoint: e.target.value })}
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-slate-700">Data Endpoint</label>
                                        <input
                                            type="text"
                                            className="input-field mt-1"
                                            placeholder="/api/loans"
                                            value={formData.dataEndpoint}
                                            onChange={(e) => setFormData({ ...formData, dataEndpoint: e.target.value })}
                                        />
                                    </div>
                                </div>

                                {isEditing && (
                                    <div className="pt-2">
                                        <button
                                            type="button"
                                            onClick={handleFetchSchema}
                                            disabled={fetchingSchema}
                                            className="w-full flex justify-center items-center gap-2 py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-blue-700 bg-blue-100 hover:bg-blue-200"
                                        >
                                            {fetchingSchema ? <RefreshCw className="animate-spin" size={16} /> : <RefreshCw size={16} />}
                                            {fetchingSchema ? 'Fetching...' : 'Fetch Schema Now'}
                                        </button>
                                        {schemaError && <p className="mt-2 text-xs text-red-600">{schemaError}</p>}
                                    </div>
                                )}
                            </div>
                        )}

                        <div>
                            <label className="block text-sm font-medium text-slate-700 mb-2">
                                JSON Schema
                                {definitionMode === 'EXTERNAL' && <span className="ml-2 text-xs text-slate-400 font-normal">(Read-only when External)</span>}
                            </label>
                            <textarea
                                className="input-field font-mono text-xs bg-slate-900 text-slate-50"
                                rows="10"
                                readOnly={definitionMode === 'EXTERNAL'}
                                placeholder='{ "filters": [...] }'
                                value={formData.schema}
                                onChange={(e) => setFormData({ ...formData, schema: e.target.value })}
                            />
                            <p className="mt-1 text-xs text-slate-500">Defines the filters available for this resource type.</p>
                        </div>
                    </div>

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
                            disabled={loading || fetchingSchema}
                            className="btn-primary"
                        >
                            {loading ? 'Saving...' : (isEditing ? 'Update Type' : 'Register Type')}
                        </button>
                    </div>
                </form>
            </SlideOver>
        </div>
    );
};

export default ResourceTypes;
