import React, { useState, useEffect } from 'react';
import { Plus, Save, Trash2, Code, FileText, CheckCircle, AlertCircle, Upload, RefreshCw, GitBranch } from 'lucide-react';
import Editor from '@monaco-editor/react';

import { policyService } from '../services/api';

const PolicyEditor = () => {
    const [policies, setPolicies] = useState([]);
    const [selectedPolicy, setSelectedPolicy] = useState(null);
    const [isEditing, setIsEditing] = useState(false);
    const [formData, setFormData] = useState({
        name: '',
        description: '',
        filename: '',
        content: '',
        version: '1.0',
        status: 'DRAFT',
        sourceType: 'MANUAL',
        gitRepositoryUrl: '',
        gitBranch: 'main',
        gitPath: '',
        lastSyncTime: null,
        syncStatus: null
    });


    const loadPolicies = React.useCallback(async () => {
        try {
            const response = await policyService.getAll();
            setPolicies(response.data);
        } catch (error) {
            console.error('Error loading policies:', error);
        }
    }, []);

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        loadPolicies();
    }, [loadPolicies]);


    const handleSelectPolicy = (policy) => {
        setSelectedPolicy(policy);
        setFormData({
            name: policy.name,
            description: policy.description || '',
            filename: policy.filename || '',
            content: policy.content,
            version: policy.version,
            status: policy.status,
            sourceType: policy.sourceType || 'MANUAL',
            gitRepositoryUrl: policy.gitRepositoryUrl,
            gitBranch: policy.gitBranch,
            gitPath: policy.gitPath,
            lastSyncTime: policy.lastSyncTime,
            syncStatus: policy.syncStatus

        });
        setIsEditing(true);
    };

    const handleCreateNew = () => {
        setSelectedPolicy(null);
        setFormData({
            name: '',
            description: '',
            filename: '',
            content: 'package policy\n\ndefault allow = false\n\n',
            version: '1.0',
            status: 'DRAFT',
            sourceType: 'MANUAL',
            gitRepositoryUrl: '',
            gitBranch: 'main',
            gitPath: '',
            lastSyncTime: null,
            syncStatus: null
        });

        setIsEditing(true);
    };

    const handleSave = async () => {
        try {
            if (selectedPolicy) {
                await policyService.update(selectedPolicy.id, formData);
            } else {
                await policyService.create(formData);
            }
            await loadPolicies();
            // setIsEditing(false); // Keep editing after save
            if (!selectedPolicy) {
                // If it was new, we might want to select it, but for now just reload
                setIsEditing(false);
            }
        } catch (error) {
            console.error('Error saving policy:', error);
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this policy?')) {
            try {
                await policyService.delete(id);
                await loadPolicies();
                if (selectedPolicy?.id === id) {
                    setSelectedPolicy(null);
                    setIsEditing(false);
                }
            } catch (error) {
                console.error('Error deleting policy:', error);
            }
        }
    };

    const handleFileUpload = (event) => {
        const file = event.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = (e) => {
                setFormData({
                    ...formData,
                    content: e.target.result,
                    filename: file.name
                });
            };
            reader.readAsText(file);
        }
    };

    const handleSyncGit = async () => {
        if (!selectedPolicy?.id) return;
        try {
            const response = await policyService.sync(selectedPolicy.id);
            const updatedPolicy = response.data;
            setFormData(prev => ({
                ...prev,
                content: updatedPolicy.content, // Update content from git
                lastSyncTime: updatedPolicy.lastSyncTime,
                syncStatus: updatedPolicy.syncStatus
            }));
            await loadPolicies(); // Refresh list to update any status indicators there
            alert("Policy synced successfully from Git!");
        } catch (error) {
            console.error('Error syncing policy:', error);
            alert("Failed to sync policy. Check console for details.");
        }
    };

    return (
        <div className="flex h-[calc(100vh-8rem)] gap-6">
            {/* Policy List Sidebar */}
            <div className="w-80 card flex flex-col overflow-hidden">
                <div className="p-4 border-b border-slate-200 flex justify-between items-center bg-slate-50">
                    <h3 className="font-bold text-slate-700 flex items-center gap-2">
                        <FileText size={18} className="text-brand-600" />
                        Policies
                    </h3>
                    <button
                        onClick={handleCreateNew}
                        className="p-1.5 bg-brand-600 hover:bg-brand-700 text-white rounded-md transition-colors shadow-sm"
                        title="New Policy"
                    >
                        <Plus size={18} />
                    </button>
                </div>
                <div className="flex-1 overflow-y-auto p-2 space-y-1 bg-white">
                    {policies.map((policy) => (
                        <div
                            key={policy.id}
                            onClick={() => handleSelectPolicy(policy)}
                            className={`p-3 rounded-md cursor-pointer transition-all border ${selectedPolicy?.id === policy.id
                                ? 'bg-brand-50 border-brand-200 shadow-sm'
                                : 'hover:bg-slate-50 border-transparent hover:border-slate-200'
                                }`}
                        >
                            <div className="flex justify-between items-start">
                                <div>
                                    <h4 className={`font-medium text-sm ${selectedPolicy?.id === policy.id ? 'text-brand-900' : 'text-slate-700'}`}>
                                        {policy.name}
                                    </h4>
                                    <p className="text-xs text-slate-500 mt-0.5">v{policy.version}</p>
                                </div>
                                <span className={`text-[10px] px-1.5 py-0.5 rounded-full font-medium ${policy.status === 'ACTIVE' ? 'bg-green-100 text-green-700' :
                                    policy.status === 'ARCHIVED' ? 'bg-slate-100 text-slate-600' :
                                        'bg-amber-100 text-amber-700'
                                    }`}>
                                    {policy.status}
                                </span>
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            {/* Editor Area */}
            <div className="flex-1 card flex flex-col overflow-hidden">
                {isEditing ? (
                    <>
                        <div className="p-4 border-b border-slate-200 flex flex-col gap-4 bg-white">
                            <div className="flex justify-between items-center">
                                <div className="flex gap-4 items-center">
                                    <div className="flex flex-col gap-2">
                                        <input
                                            type="text"
                                            value={formData.name}
                                            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                            placeholder="Policy Name (ID)"
                                            className="text-lg font-bold text-slate-900 border-none focus:ring-0 p-0 placeholder-slate-400 w-64"
                                        />
                                        <input
                                            type="text"
                                            value={formData.filename}
                                            onChange={(e) => setFormData({ ...formData, filename: e.target.value })}
                                            placeholder="filename.rego (unique)"
                                            className="text-xs text-slate-600 border-none focus:ring-0 p-0 placeholder-slate-400 w-64"
                                        />
                                        <input
                                            type="text"
                                            value={formData.description}
                                            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                                            placeholder="Short description..."
                                            className="text-xs text-slate-500 border-none focus:ring-0 p-0 placeholder-slate-400 w-96 italic"
                                        />
                                    </div>
                                    <div className="h-6 w-px bg-slate-200"></div>
                                    <select
                                        value={formData.status}
                                        onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                                        className="text-sm border-slate-300 rounded-md shadow-sm focus:border-brand-500 focus:ring-brand-500 py-1"
                                    >
                                        <option value="DRAFT">Draft</option>
                                        <option value="ACTIVE">Active</option>
                                        <option value="ARCHIVED">Archived</option>
                                    </select>
                                </div>
                                <div className="flex gap-2">
                                    {selectedPolicy && (
                                        <button
                                            onClick={() => handleDelete(selectedPolicy.id)}
                                            className="btn-danger flex items-center gap-2 py-1.5 text-sm"
                                        >
                                            <Trash2 size={16} />
                                            Delete
                                        </button>
                                    )}
                                    <button
                                        onClick={handleSave}
                                        className="btn-primary flex items-center gap-2 py-1.5 text-sm"
                                    >
                                        <Save size={16} />
                                        Save Changes
                                    </button>
                                </div>
                            </div>

                            {/* Source Configuration */}
                            <div className="flex items-center gap-4 text-sm">
                                <label className="font-medium text-slate-700">Source:</label>
                                <div className="flex rounded-md shadow-sm">
                                    <button
                                        onClick={() => setFormData({ ...formData, sourceType: 'MANUAL' })}
                                        className={`px-3 py-1.5 text-xs font-medium border rounded-l-md ${formData.sourceType === 'MANUAL'
                                            ? 'bg-brand-50 text-brand-700 border-brand-200 z-10'
                                            : 'bg-white text-slate-600 border-slate-300 hover:bg-slate-50'
                                            }`}
                                    >
                                        Manual / Upload
                                    </button>
                                    <button
                                        onClick={() => setFormData({ ...formData, sourceType: 'GIT' })}
                                        className={`px-3 py-1.5 text-xs font-medium border -ml-px rounded-r-md ${formData.sourceType === 'GIT'
                                            ? 'bg-brand-50 text-brand-700 border-brand-200 z-10'
                                            : 'bg-white text-slate-600 border-slate-300 hover:bg-slate-50'
                                            }`}
                                    >
                                        Git Repository
                                    </button>
                                </div>

                                {formData.sourceType === 'MANUAL' && (
                                    <div className="flex items-center gap-2 ml-4">
                                        <input
                                            type="file"
                                            accept=".rego"
                                            onChange={handleFileUpload}
                                            className="hidden"
                                            id="rego-upload"
                                        />
                                        <label
                                            htmlFor="rego-upload"
                                            className="flex items-center gap-1.5 px-3 py-1.5 bg-white border border-slate-300 rounded-md text-xs font-medium text-slate-700 hover:bg-slate-50 cursor-pointer transition-colors"
                                        >
                                            <Upload size={14} />
                                            Upload .rego
                                        </label>
                                    </div>
                                )}
                            </div>

                            {formData.sourceType === 'GIT' && (
                                <div className="grid grid-cols-3 gap-4 bg-slate-50 p-3 rounded-md border border-slate-200">
                                    <div className="col-span-1">
                                        <label className="block text-xs font-medium text-slate-500 mb-1">Repository URL</label>
                                        <input
                                            type="text"
                                            value={formData.gitRepositoryUrl || ''}
                                            onChange={(e) => setFormData({ ...formData, gitRepositoryUrl: e.target.value })}
                                            placeholder="https://github.com/org/repo.git"
                                            className="w-full text-xs border-slate-300 rounded-md"
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-xs font-medium text-slate-500 mb-1">Branch</label>
                                        <input
                                            type="text"
                                            value={formData.gitBranch || ''}
                                            onChange={(e) => setFormData({ ...formData, gitBranch: e.target.value })}
                                            placeholder="main"
                                            className="w-full text-xs border-slate-300 rounded-md"
                                        />
                                    </div>
                                    <div className="flex items-end gap-2">
                                        <div className="flex-1">
                                            <label className="block text-xs font-medium text-slate-500 mb-1">Path to File</label>
                                            <input
                                                type="text"
                                                value={formData.gitPath || ''}
                                                onChange={(e) => setFormData({ ...formData, gitPath: e.target.value })}
                                                placeholder="policies/auth.rego"
                                                className="w-full text-xs border-slate-300 rounded-md"
                                            />
                                        </div>
                                        {selectedPolicy && (
                                            <div className="flex flex-col items-end gap-1">
                                                <button
                                                    onClick={handleSyncGit}
                                                    className="px-3 py-2 bg-slate-800 text-white rounded-md text-xs font-medium hover:bg-slate-700 flex items-center gap-1"
                                                >
                                                    <RefreshCw size={14} />
                                                    Sync Now
                                                </button>
                                            </div>
                                        )}
                                    </div>
                                    <div className="col-span-3 flex justify-between items-center text-xs text-slate-500 border-t border-slate-200 pt-2 mt-1">
                                        <div className="flex gap-2">
                                            <span>Last Sync: </span>
                                            <span className="font-mono text-slate-700">
                                                {formData.lastSyncTime ? new Date(formData.lastSyncTime).toLocaleString() : 'Never'}
                                            </span>
                                        </div>
                                        <div className="flex gap-2 items-center">
                                            <span>Status: </span>
                                            <span className={`font-medium ${(formData.syncStatus || '').startsWith('FAILED') ? 'text-red-600' :
                                                formData.syncStatus === 'SUCCESS' ? 'text-green-600' : 'text-slate-600'
                                                }`}>
                                                {formData.syncStatus || 'Unknown'}
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            )}
                        </div>

                        <div className="flex-1 relative bg-[#1e1e1e]">
                            <Editor
                                height="100%"
                                defaultLanguage="rego"
                                language="rego" // You might need to register the language if not standard, or use 'python'/'ruby' for similar colouring
                                theme="vs-dark"
                                value={formData.content}
                                onChange={(value) => setFormData({ ...formData, content: value })}
                                options={{
                                    minimap: { enabled: false },
                                    fontSize: 14,
                                    scrollBeyondLastLine: false,
                                    readOnly: formData.sourceType === 'GIT',
                                    automaticLayout: true,
                                }}
                            />
                        </div>
                    </>
                ) : (
                    <div className="flex-1 flex flex-col items-center justify-center text-slate-400 bg-slate-50">
                        <div className="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center mb-4">
                            <Code size={32} className="text-slate-300" />
                        </div>
                        <p className="text-lg font-medium text-slate-600">No Policy Selected</p>
                        <p className="text-sm mt-2">Select a policy from the sidebar to edit or create a new one.</p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default PolicyEditor;
