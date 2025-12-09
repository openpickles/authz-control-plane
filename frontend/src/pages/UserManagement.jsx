import React, { useState, useEffect } from 'react';
import { Users, Shield, Layers, Plus, Trash2, Search } from 'lucide-react';
import { userService } from '../services/api';

const UserManagement = () => {
    const [activeTab, setActiveTab] = useState('users');
    const [users, setUsers] = useState([]);
    const [roles, setRoles] = useState([]);
    const [groups, setGroups] = useState([]);
    const [showModal, setShowModal] = useState(false);
    const [newItemName, setNewItemName] = useState('');

    const loadData = React.useCallback(async () => {
        try {
            const [usersRes, rolesRes, groupsRes] = await Promise.all([
                userService.getAll(),
                userService.getAllRoles(),
                userService.getAllGroups()
            ]);
            setUsers(usersRes.data);
            setRoles(rolesRes.data);
            setGroups(groupsRes.data);
        } catch (error) {
            console.error('Error loading user data:', error);
        }
    }, []);

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        loadData();
    }, [loadData]);

    const handleCreate = async (e) => {
        e.preventDefault();
        try {
            if (activeTab === 'users') {
                await userService.createUser({ username: newItemName, roles: [], groups: [] });
            } else if (activeTab === 'roles') {
                await userService.createRole({ name: newItemName });
            } else {
                await userService.createGroup({ name: newItemName });
            }
            setNewItemName('');
            setShowModal(false);
            loadData();
        } catch (error) {
            console.error('Error creating item:', error);
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Delete this item?')) {
            try {
                await userService.deleteUser(id);
                loadData();
            } catch (error) {
                console.error('Error deleting:', error);
            }
        }
    };

    const tabs = [
        { id: 'users', label: 'Users', icon: Users },
        { id: 'roles', label: 'Roles', icon: Shield },
        { id: 'groups', label: 'Groups', icon: Layers },
    ];

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h2 className="text-2xl font-bold text-slate-900">User Management</h2>
                    <p className="text-slate-500 mt-1">Manage users, roles, and groups.</p>
                </div>
                <button
                    onClick={() => setShowModal(true)}
                    className="btn-primary flex items-center gap-2"
                >
                    <Plus size={18} />
                    Add {activeTab.slice(0, -1)}
                </button>
            </div>

            {/* Tabs */}
            <div className="border-b border-slate-200">
                <nav className="-mb-px flex space-x-8">
                    {tabs.map((tab) => (
                        <button
                            key={tab.id}
                            onClick={() => setActiveTab(tab.id)}
                            className={`
                flex items-center gap-2 py-4 px-1 border-b-2 font-medium text-sm transition-colors
                ${activeTab === tab.id
                                    ? 'border-brand-600 text-brand-600'
                                    : 'border-transparent text-slate-500 hover:text-slate-700 hover:border-slate-300'}
              `}
                        >
                            <tab.icon size={18} />
                            {tab.label}
                        </button>
                    ))}
                </nav>
            </div>

            {/* Content */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {activeTab === 'users' && users.map((user) => (
                    <div key={user.id} className="card p-4 flex justify-between items-center hover:shadow-md transition-shadow">
                        <div className="flex items-center gap-3">
                            <div className="w-10 h-10 rounded-full bg-brand-100 flex items-center justify-center text-brand-700 font-bold text-sm">
                                {user.username.charAt(0).toUpperCase()}
                            </div>
                            <div>
                                <h4 className="text-slate-900 font-medium">{user.username}</h4>
                                <p className="text-xs text-slate-500">ID: {user.id}</p>
                            </div>
                        </div>
                        <button onClick={() => handleDelete(user.id)} className="text-slate-400 hover:text-red-500 p-2">
                            <Trash2 size={18} />
                        </button>
                    </div>
                ))}

                {activeTab === 'roles' && roles.map((role) => (
                    <div key={role.id} className="card p-4 flex justify-between items-center hover:shadow-md transition-shadow">
                        <div className="flex items-center gap-3">
                            <div className="p-2 rounded-lg bg-purple-100 text-purple-700">
                                <Shield size={20} />
                            </div>
                            <h4 className="text-slate-900 font-medium">{role.name}</h4>
                        </div>
                    </div>
                ))}

                {activeTab === 'groups' && groups.map((group) => (
                    <div key={group.id} className="card p-4 flex justify-between items-center hover:shadow-md transition-shadow">
                        <div className="flex items-center gap-3">
                            <div className="p-2 rounded-lg bg-orange-100 text-orange-700">
                                <Layers size={20} />
                            </div>
                            <h4 className="text-slate-900 font-medium">{group.name}</h4>
                        </div>
                    </div>
                ))}
            </div>

            {/* Modal */}
            {showModal && (
                <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-xl shadow-2xl w-full max-w-sm overflow-hidden animate-in fade-in zoom-in duration-200">
                        <div className="px-6 py-4 border-b border-slate-100 bg-slate-50">
                            <h3 className="text-lg font-bold text-slate-900">Add New {activeTab.slice(0, -1)}</h3>
                        </div>
                        <form onSubmit={handleCreate} className="p-6 space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-slate-700 mb-1">Name</label>
                                <input
                                    type="text"
                                    value={newItemName}
                                    onChange={(e) => setNewItemName(e.target.value)}
                                    className="input-field"
                                    autoFocus
                                    required
                                />
                            </div>
                            <div className="flex gap-3 pt-2">
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
                                    Create
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default UserManagement;
