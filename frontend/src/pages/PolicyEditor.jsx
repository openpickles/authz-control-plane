import React, { useState, useEffect } from 'react';
import { Plus, Save, Trash2, Code, FileText, CheckCircle, AlertCircle, Upload, RefreshCw, GitBranch, Play, UploadCloud, Search, Settings, Shield } from 'lucide-react';
import Editor from '@monaco-editor/react';

import { policyService, evaluationService } from '../services/api';
import TestPanel from '../components/TestPanel';

const PolicyEditor = () => {
    const [policies, setPolicies] = useState([]);
    const [selectedPolicy, setSelectedPolicy] = useState(null);
    const [isEditing, setIsEditing] = useState(false);
    const [showTestPanel, setShowTestPanel] = useState(false);
    const [pushModalOpen, setPushModalOpen] = useState(false);
    const [commitMessage, setCommitMessage] = useState('');
    const [validationStatus, setValidationStatus] = useState(null); // 'valid', 'invalid', null
    const [showSettings, setShowSettings] = useState(false);

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

    const [search, setSearch] = useState('');

    const loadPolicies = React.useCallback(async () => {
        try {
            const response = await policyService.getAll({ size: 100, search });
            const data = response.data.content || (Array.isArray(response.data) ? response.data : []);
            setPolicies(data);
        } catch (error) {
            console.error('Error loading policies:', error);
        }
    }, [search]);

    useEffect(() => {
        const timer = setTimeout(() => loadPolicies(), 300); // Debounce search
        return () => clearTimeout(timer);
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
            if (!selectedPolicy) {
                setIsEditing(false);
            }
        } catch (error) {
            console.error('Error saving policy:', error);
            const message = error.response?.data?.errorMessage || 'Failed to save policy. Check console for details.';
            alert(message);
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
                const rawFilename = file.name;
                const nameWithoutExt = rawFilename.replace(/\.rego$/, '');
                const timestamp = new Date().getTime();
                const extIndex = rawFilename.lastIndexOf('.');
                const baseName = extIndex !== -1 ? rawFilename.substring(0, extIndex) : rawFilename;
                const extension = extIndex !== -1 ? rawFilename.substring(extIndex) : '';
                const uniqueName = `${baseName}_${timestamp}${extension}`;

                setFormData(prev => ({
                    ...prev,
                    content: e.target.result,
                    filename: uniqueName,
                    name: prev.name ? prev.name : nameWithoutExt
                }));
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
                content: updatedPolicy.content,
                lastSyncTime: updatedPolicy.lastSyncTime,
                syncStatus: updatedPolicy.syncStatus
            }));
            await loadPolicies();
            alert("Policy synced successfully from Git!");
        } catch (error) {
            console.error('Error syncing policy:', error);
            alert("Failed to sync policy. Check console for details.");
        }
    };

    const handleValidate = async () => {
        setValidationStatus('loading');
        try {
            await evaluationService.validate(formData.content);
            setValidationStatus('valid');
            setTimeout(() => setValidationStatus(null), 3000);
        } catch (error) {
            console.error('Validation error:', error);
            setValidationStatus('invalid');
            alert("Validation Failed: " + (error.response?.data?.error || error.message));
        }
    };

    const handlePush = async () => {
        if (!selectedPolicy?.id) return;
        if (!commitMessage.trim()) {
            alert("Please enter a commit message");
            return;
        }

        try {
            await policyService.push(selectedPolicy.id, commitMessage);
            setPushModalOpen(false);
            setCommitMessage('');
            alert("Successfully pushed to Git!");
            await loadPolicies();
        } catch (error) {
            console.error('Push error:', error);
            alert("Failed to push to Git: " + (error.response?.data?.error || error.message));
        }
    };

    return (
        <div className="flex h-full gap-4">
            {/* 1. File Explorer / Policy List */}
            <div className="w-72 bento-card flex flex-col overflow-hidden flex-shrink-0">
                <div className="p-4 border-b border-slate-200 bg-slate-50/50 space-y-3">
                    <div className="flex justify-between items-center">
                        <h3 className="font-bold text-slate-700 flex items-center gap-2 text-sm uppercase tracking-wide">
                            <FileText size={16} className="text-brand-600" />
                            Explorer
                        </h3>
                        <button
                            onClick={handleCreateNew}
                            className="p-1.5 bg-brand-600 hover:bg-brand-700 text-white rounded-md transition-colors shadow-sm focus-ring"
                            title="New Policy"
                            aria-label="Create New Policy"
                        >
                            <Plus size={16} />
                        </button>
                    </div>
                    <div className="relative group">
                        <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                            <Search size={14} className="text-slate-400 group-focus-within:text-brand-500" />
                        </div>
                        <input
                            type="text"
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                            placeholder="Filter policies..."
                            className="block w-full rounded-md border-0 py-1.5 pl-9 text-slate-900 shadow-sm ring-1 ring-inset ring-slate-300 placeholder:text-slate-400 focus:ring-2 focus:ring-inset focus:ring-brand-600 sm:text-xs sm:leading-6 transition-shadow"
                        />
                    </div>
                </div>
                <div className="flex-1 overflow-y-auto p-2 space-y-1 bg-white">
                    {policies.map((policy) => (
                        <div
                            key={policy.id}
                            onClick={() => handleSelectPolicy(policy)}
                            onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') handleSelectPolicy(policy); }}
                            tabIndex={0}
                            role="button"
                            className={`p-3 rounded-lg cursor-pointer transition-all border outline-none focus-ring ${selectedPolicy?.id === policy.id
                                ? 'bg-brand-50 border-brand-200 shadow-sm ring-1 ring-brand-200'
                                : 'hover:bg-slate-50 border-transparent hover:border-slate-200'
                                }`}
                        >
                            <div className="flex justify-between items-start">
                                <div className="min-w-0">
                                    <h4 className={`font-medium text-sm truncate ${selectedPolicy?.id === policy.id ? 'text-brand-900' : 'text-slate-700'}`}>
                                        {policy.name}
                                    </h4>
                                    <p className="text-xs text-slate-500 mt-0.5 truncate">{policy.filename || 'No filename'}</p>
                                </div>
                                <span className={`flex-shrink-0 text-[10px] px-1.5 py-0.5 rounded-full font-medium ${policy.status === 'ACTIVE' ? 'bg-green-100 text-green-700' :
                                    policy.status === 'ARCHIVED' ? 'bg-slate-100 text-slate-600' : 'bg-amber-100 text-amber-700'}`}>
                                    {policy.status && policy.status[0]}
                                </span>
                            </div>
                        </div>
                    ))}
                    {policies.length === 0 && (
                        <div className="text-center p-4 text-xs text-slate-400">
                            No policies found.
                        </div>
                    )}
                </div>
            </div>

            {/* 2. Main Editor Area (IDE) */}
            <div className="flex-1 bento-card flex flex-col overflow-hidden relative">
                {isEditing ? (
                    <>
                        {/* IDE Toolbar */}
                        <div className="h-14 border-b border-slate-200 bg-white flex items-center justify-between px-4 flex-shrink-0">
                            <div className="flex items-center gap-4 flex-1 min-w-0">
                                <FileText size={18} className="text-slate-400 flex-shrink-0" />
                                <div className="flex flex-col min-w-0">
                                    <div className="flex items-center gap-2">
                                        <input
                                            type="text"
                                            value={formData.name}
                                            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                            placeholder="Policy Name"
                                            className="text-sm font-bold text-slate-900 border-none focus:ring-0 p-0 placeholder-slate-400 bg-transparent truncate"
                                            aria-label="Policy Name"
                                        />
                                        <button onClick={() => setShowSettings(true)} className="text-slate-400 hover:text-brand-600 transition-colors focus-ring rounded p-0.5">
                                            <Settings size={14} />
                                        </button>
                                    </div>
                                    <div className="flex items-center gap-2 text-xs text-slate-500">
                                        <span className="font-mono">{formData.filename || 'untitled.rego'}</span>
                                        {formData.sourceType === 'GIT' && (
                                            <span className="flex items-center gap-1 text-slate-400 bg-slate-100 px-1.5 rounded">
                                                <GitBranch size={10} /> {formData.gitBranch || 'main'}
                                            </span>
                                        )}
                                    </div>
                                </div>
                            </div>

                            <div className="flex items-center gap-2">
                                <div className="h-6 w-px bg-slate-200 mx-2"></div>

                                <button
                                    onClick={() => setShowTestPanel(!showTestPanel)}
                                    className={`btn-secondary flex items-center gap-2 py-1.5 px-3 text-xs focus-ring ${showTestPanel ? 'bg-slate-100 text-brand-700 border-brand-200' : ''}`}
                                    title="Toggle Test Panel"
                                >
                                    <Play size={14} />
                                    {showTestPanel ? 'Hide Tests' : 'Run Tests'}
                                </button>

                                <button
                                    onClick={handleValidate}
                                    className={`flex items-center gap-2 py-1.5 px-3 rounded-md text-xs font-medium transition-colors focus-ring ${validationStatus === 'valid' ? 'bg-green-50 text-green-700 border border-green-200' :
                                        validationStatus === 'invalid' ? 'bg-red-50 text-red-700 border border-red-200' :
                                            'bg-white border border-slate-300 text-slate-700 hover:bg-slate-50'
                                        }`}
                                >
                                    {validationStatus === 'valid' ? <CheckCircle size={14} /> :
                                        validationStatus === 'invalid' ? <AlertCircle size={14} /> :
                                            <CheckCircle size={14} className="text-slate-400" />}
                                    {validationStatus === 'valid' ? 'Valid' : 'Validate'}
                                </button>

                                <button onClick={handleSave} className="btn-primary flex items-center gap-2 py-1.5 px-3 text-xs ml-2 focus-ring">
                                    <Save size={14} /> Save
                                </button>

                                {/* More Actions Menu could go here */}
                                {selectedPolicy && (
                                    <button
                                        onClick={() => handleDelete(selectedPolicy.id)}
                                        className="text-slate-400 hover:text-red-500 p-2 rounded-md hover:bg-red-50 transition-colors focus-ring"
                                        title="Delete Policy"
                                    >
                                        <Trash2 size={16} />
                                    </button>
                                )}
                            </div>
                        </div>

                        {/* Split Pane: Editor | Test Panel */}
                        <div className="flex-1 flex overflow-hidden">
                            <div className={`flex-1 relative bg-[#1e1e1e] transition-all duration-300 flex flex-col`}>
                                {/* Editor Header/Tabs */}
                                <div className="h-8 bg-[#252526] flex items-center px-4 border-b border-[#3e3e42]">
                                    <span className="text-[11px] text-[#ccccc7] flex items-center gap-2 bg-[#1e1e1e] h-full px-3 border-r border-[#3e3e42] border-t-2 border-t-brand-500">
                                        <Code size={12} className="text-blue-400" />
                                        {formData.filename || 'code.rego'}
                                    </span>
                                </div>
                                <div className="flex-1 relative">
                                    <Editor
                                        height="100%"
                                        defaultLanguage="rego"
                                        language="rego"
                                        theme="vs-dark"
                                        value={formData.content}
                                        onChange={(value) => setFormData({ ...formData, content: value })}
                                        options={{
                                            minimap: { enabled: false },
                                            fontSize: 13,
                                            fontFamily: "'JetBrains Mono', 'Fira Code', Consolas, monospace",
                                            scrollBeyondLastLine: false,
                                            readOnly: formData.sourceType === 'GIT',
                                            automaticLayout: true,
                                            renderLineHighlight: 'all',
                                            lineHeight: 1.5,
                                        }}
                                    />
                                </div>
                            </div>

                            {/* Test Panel - Right Pane */}
                            {showTestPanel && (
                                <div className="w-1/3 min-w-[320px] border-l border-slate-200 bg-white flex flex-col shadow-xl z-10">
                                    <div className="h-full overflow-hidden">
                                        <TestPanel
                                            policyContent={formData.content}
                                            policyId={selectedPolicy?.id}
                                            onClose={() => setShowTestPanel(false)}
                                        />
                                    </div>
                                </div>
                            )}
                        </div>
                    </>
                ) : (
                    <div className="flex-1 flex flex-col items-center justify-center text-slate-400 bg-slate-50/50">
                        <div className="w-20 h-20 bg-white rounded-full flex items-center justify-center mb-6 shadow-sm border border-slate-100">
                            <Shield size={40} className="text-slate-300" strokeWidth={1.5} />
                        </div>
                        <h2 className="text-xl font-semibold text-slate-700">Policy Editor</h2>
                        <p className="text-sm mt-2 text-slate-500 max-w-sm text-center">Select a policy from the explorer to view the code, or create a new one to get started.</p>
                        <button onClick={handleCreateNew} className="mt-8 btn-primary">
                            Create New Policy
                        </button>
                    </div>
                )}
            </div>

            {/* Push Modal */}
            <PushModal
                isOpen={pushModalOpen}
                onClose={() => setPushModalOpen(false)}
                onPush={handlePush}
                message={commitMessage}
                setMessage={setCommitMessage}
            />

            {/* Policy Settings Modal */}
            {isEditing && (
                <PolicySettingsModal
                    isOpen={showSettings}
                    onClose={() => setShowSettings(false)}
                    formData={formData}
                    setFormData={setFormData}
                    handleFileUpload={handleFileUpload}
                    handleSyncGit={handleSyncGit}
                    selectedPolicy={selectedPolicy}
                />
            )}
        </div>
    );
};

const PolicySettingsModal = ({ isOpen, onClose, formData, setFormData, handleFileUpload, handleSyncGit, selectedPolicy }) => {
    if (!isOpen) return null;
    return (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg w-[32rem] shadow-xl overflow-hidden bento-card p-0">
                <div className="p-4 border-b border-slate-200 flex justify-between items-center bg-slate-50">
                    <h3 className="font-bold text-slate-700 flex items-center gap-2">
                        <Settings size={18} /> Policy Settings
                    </h3>
                    <button onClick={onClose} className="p-1 hover:bg-slate-200 rounded-md transition-colors">
                        <AlertCircle size={18} className="rotate-45 text-slate-500" />
                    </button>
                </div>

                <div className="p-6 space-y-4">
                    {/* Metadata */}
                    <div className="space-y-3">
                        <div>
                            <label className="block text-xs font-bold text-slate-500 uppercase mb-1">Display Name</label>
                            <input type="text" value={formData.name} onChange={(e) => setFormData({ ...formData, name: e.target.value })} className="input-field" placeholder="My Policy" />
                        </div>
                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-xs font-bold text-slate-500 uppercase mb-1">Filename (.rego)</label>
                                <input type="text" value={formData.filename} onChange={(e) => setFormData({ ...formData, filename: e.target.value })} className="input-field font-mono text-xs" placeholder="policy.rego" />
                            </div>
                            <div>
                                <label className="block text-xs font-bold text-slate-500 uppercase mb-1">Status</label>
                                <select value={formData.status} onChange={(e) => setFormData({ ...formData, status: e.target.value })} className="input-field">
                                    <option value="DRAFT">Draft</option>
                                    <option value="ACTIVE">Active</option>
                                    <option value="ARCHIVED">Archived</option>
                                </select>
                            </div>
                        </div>
                        <div>
                            <label className="block text-xs font-bold text-slate-500 uppercase mb-1">Description</label>
                            <textarea value={formData.description} onChange={(e) => setFormData({ ...formData, description: e.target.value })} className="input-field text-xs" rows={2} />
                        </div>
                    </div>

                    <hr className="border-slate-100" />

                    {/* Source Config */}
                    <div>
                        <label className="block text-xs font-bold text-slate-500 uppercase mb-2">Source Configuration</label>
                        <div className="flex rounded-md shadow-sm mb-4">
                            <button onClick={() => setFormData({ ...formData, sourceType: 'MANUAL' })} className={`flex-1 py-1.5 text-xs font-medium border rounded-l-md transition-colors ${formData.sourceType === 'MANUAL' ? 'bg-brand-50 text-brand-700 border-brand-200 z-10' : 'bg-white text-slate-600 border-slate-300 hover:bg-slate-50'}`}>Manual / Upload</button>
                            <button onClick={() => setFormData({ ...formData, sourceType: 'GIT' })} className={`flex-1 py-1.5 text-xs font-medium border -ml-px rounded-r-md transition-colors ${formData.sourceType === 'GIT' ? 'bg-brand-50 text-brand-700 border-brand-200 z-10' : 'bg-white text-slate-600 border-slate-300 hover:bg-slate-50'}`}>Git Repository</button>
                        </div>

                        {formData.sourceType === 'MANUAL' && (
                            <div className="flex items-center gap-3 p-3 bg-slate-50 rounded-md border border-slate-200 border-dashed">
                                <div className="p-2 bg-white rounded-md border border-slate-200 text-slate-400">
                                    <FileText size={20} />
                                </div>
                                <div className="flex-1">
                                    <p className="text-xs text-slate-500">Upload an existing .rego file to replace content.</p>
                                </div>
                                <input type="file" accept=".rego" onChange={handleFileUpload} className="hidden" id="rego-upload-modal" />
                                <label htmlFor="rego-upload-modal" className="btn-secondary text-xs py-1.5 px-3 cursor-pointer">
                                    <Upload size={14} className="mr-1 inline" /> Upload
                                </label>
                            </div>
                        )}

                        {formData.sourceType === 'GIT' && (
                            <div className="space-y-3 p-3 bg-slate-50 rounded-md border border-slate-200">
                                <div>
                                    <label className="block text-[10px] font-bold text-slate-400 uppercase mb-1">Repo URL</label>
                                    <input type="text" value={formData.gitRepositoryUrl || ''} onChange={(e) => setFormData({ ...formData, gitRepositoryUrl: e.target.value })} className="input-field text-xs" placeholder="https://github.com/..." />
                                </div>
                                <div className="grid grid-cols-2 gap-3">
                                    <div>
                                        <label className="block text-[10px] font-bold text-slate-400 uppercase mb-1">Branch</label>
                                        <input type="text" value={formData.gitBranch || ''} onChange={(e) => setFormData({ ...formData, gitBranch: e.target.value })} className="input-field text-xs" />
                                    </div>
                                    <div>
                                        <label className="block text-[10px] font-bold text-slate-400 uppercase mb-1">Path</label>
                                        <input type="text" value={formData.gitPath || ''} onChange={(e) => setFormData({ ...formData, gitPath: e.target.value })} className="input-field text-xs" placeholder="policies/auth.rego" />
                                    </div>
                                </div>
                                {selectedPolicy && (
                                    <div className="flex justify-between items-center pt-2">
                                        <span className="text-xs text-slate-500">Last Sync: {formData.lastSyncTime ? new Date(formData.lastSyncTime).toLocaleString() : 'Never'}</span>
                                        <button onClick={handleSyncGit} className="btn-secondary text-xs py-1 px-3 flex items-center gap-1">
                                            <RefreshCw size={12} /> Sync Now
                                        </button>
                                    </div>
                                )}
                            </div>
                        )}
                    </div>
                </div>
                <div className="p-4 bg-slate-50 border-t border-slate-200 flex justify-end">
                    <button onClick={onClose} className="btn-primary">Done</button>
                </div>
            </div>
        </div>
    );
};

const PushModal = ({ isOpen, onClose, onPush, message, setMessage }) => {
    if (!isOpen) return null;
    return (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
            <div className="bg-white p-6 rounded-lg w-96 shadow-xl">
                <h3 className="text-lg font-bold mb-4">Push to Git</h3>
                <textarea
                    className="w-full border rounded p-2 mb-4 text-sm"
                    rows={3}
                    placeholder="Commit message..."
                    value={message}
                    onChange={(e) => setMessage(e.target.value)}
                />
                <div className="flex justify-end gap-2">
                    <button onClick={onClose} className="btn-secondary">Cancel</button>
                    <button onClick={onPush} className="btn-primary">Push</button>
                </div>
            </div>
        </div>
    );
};

export default PolicyEditor;
