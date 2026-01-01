import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import {
    LayoutDashboard,
    Shield,
    Users,
    Settings,
    LogOut,
    Server,
    Link as LinkIcon,
    Package,
    ShieldCheck,
    Menu,
    ChevronRight
} from 'lucide-react';

const DualRailSidebar = () => {
    const [expanded, setExpanded] = useState(true);

    const handleLogout = () => {
        const logoutUrl = import.meta.env.DEV ? 'http://localhost:8080/logout' : '/logout';
        window.location.href = logoutUrl;
    };

    // Primary Rail Items (Top Level Contexts)
    const primaryItems = [
        { id: 'platform', icon: LayoutDashboard, label: 'Platform', path: '/' },
        { id: 'policy', icon: Shield, label: 'Policy Engine', path: '/policies' },
        { id: 'admin', icon: Settings, label: 'Administration', path: '/users' },
    ];

    // Secondary Rail Items (Context Specific)
    // Logic: Show different items based on the active primary context.
    // For simplicity in this implementation, we will show a flattened list categorized by headers
    // effectively acting as a "Mega Menu" for the active "Policy Engine" platform.



    return (
        <div className="flex h-screen flex-shrink-0 z-20 shadow-xl relative">
            {/* Primary Rail - Fixed Width 64px (w-16) - Darkest Theme */}
            <div className="w-16 flex flex-col items-center py-4 bg-slate-950 border-r border-slate-800 text-slate-400 z-30">
                <div className="mb-6">
                    <div className="bg-brand-600 p-2 rounded-lg text-white">
                        <Shield size={24} strokeWidth={2.5} />
                    </div>
                </div>

                <nav className="flex-1 space-y-4 w-full flex flex-col items-center">
                    {primaryItems.map((item) => (
                        <NavLink
                            key={item.id}
                            to={item.path}
                            className={({ isActive }) =>
                                `p-3 rounded-xl transition-all duration-200 group relative focus-ring-dark ${isActive
                                    ? 'bg-brand-600 text-white shadow-lg shadow-brand-900/50'
                                    : 'hover:bg-slate-800 hover:text-slate-100'
                                }`
                            }
                            aria-label={item.label}
                        >
                            <item.icon size={24} strokeWidth={1.5} />
                            {/* Tooltip on hover */}
                            <span className="absolute left-14 bg-slate-900 text-white text-xs px-2 py-1 rounded opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap pointer-events-none z-50 shadow-md">
                                {item.label}
                            </span>
                        </NavLink>
                    ))}
                </nav>

                <div className="mt-auto flex flex-col items-center gap-4">
                    <button
                        onClick={handleLogout}
                        className="p-3 text-slate-500 hover:text-red-400 hover:bg-slate-900 rounded-xl transition-colors focus-ring-dark"
                        aria-label="Logout"
                    >
                        <LogOut size={22} />
                    </button>
                    <div className="w-8 h-8 rounded-full bg-slate-800 flex items-center justify-center text-xs font-bold text-slate-300 border border-slate-700">
                        AD
                    </div>
                </div>
            </div>

            {/* Secondary Rail - Expandable 240px (w-64) - Dark Theme */}
            {expanded && (
                <div className="w-64 bg-slate-900 border-r border-slate-800 flex flex-col z-20">
                    {/* Header / Context Label */}
                    <div className="h-16 flex items-center px-5 border-b border-slate-800/50">
                        <span className="text-slate-100 font-semibold tracking-wide">
                            Platform Navigation
                        </span>
                    </div>

                    <nav className="flex-1 overflow-y-auto py-6 px-3 space-y-6">

                        {/* Section: Definition */}
                        <div>
                            <h3 className="px-3 text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">Definition</h3>
                            <div className="space-y-1">
                                <NavLink to="/resource-types" className={({ isActive }) => `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors focus-ring-dark ${isActive ? 'bg-slate-800 text-indigo-400' : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'}`}>
                                    <Server size={18} /> Resource Types
                                </NavLink>
                            </div>
                        </div>

                        {/* Section: Policies */}
                        <div>
                            <h3 className="px-3 text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">Policies</h3>
                            <div className="space-y-1">
                                <NavLink to="/policies" className={({ isActive }) => `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors focus-ring-dark ${isActive ? 'bg-slate-800 text-indigo-400' : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'}`}>
                                    <Shield size={18} /> Studio
                                </NavLink>
                                <NavLink to="/policy-bindings" className={({ isActive }) => `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors focus-ring-dark ${isActive ? 'bg-slate-800 text-indigo-400' : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'}`}>
                                    <LinkIcon size={18} /> Bindings
                                </NavLink>
                                <NavLink to="/policy-bundles" className={({ isActive }) => `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors focus-ring-dark ${isActive ? 'bg-slate-800 text-indigo-400' : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'}`}>
                                    <Package size={18} /> Bundles
                                </NavLink>
                                <NavLink to="/entitlements" className={({ isActive }) => `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors focus-ring-dark ${isActive ? 'bg-slate-800 text-indigo-400' : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'}`}>
                                    <Users size={18} /> Entitlements
                                </NavLink>
                            </div>
                        </div>

                        {/* Section: System */}
                        <div>
                            <h3 className="px-3 text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">System</h3>
                            <div className="space-y-1">
                                <NavLink to="/audit" className={({ isActive }) => `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors focus-ring-dark ${isActive ? 'bg-slate-800 text-indigo-400' : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'}`}>
                                    <ShieldCheck size={18} /> Audit Logs
                                </NavLink>
                                <NavLink to="/users" className={({ isActive }) => `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors focus-ring-dark ${isActive ? 'bg-slate-800 text-indigo-400' : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'}`}>
                                    <Users size={18} /> Users
                                </NavLink>
                                <NavLink to="/settings" className={({ isActive }) => `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors focus-ring-dark ${isActive ? 'bg-slate-800 text-indigo-400' : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'}`}>
                                    <Settings size={18} /> Settings
                                </NavLink>
                            </div>
                        </div>

                    </nav>

                    {/* Collapse Toggle */}
                    <div className="p-4 border-t border-slate-800">
                        <button
                            onClick={() => setExpanded(false)}
                            className="flex items-center w-full px-3 py-2 text-slate-500 hover:text-slate-300 transition-colors rounded-lg focus-ring-dark"
                        >
                            <ChevronRight className="rotate-180" size={16} />
                            <span className="ml-2 text-xs font-bold uppercase">Collapse Rail</span>
                        </button>
                    </div>
                </div>
            )}

            {/* Collapsed State Toggle (Floating or attached) */}
            {!expanded && (
                <div className="absolute top-4 left-20 z-50">
                    <button
                        onClick={() => setExpanded(true)}
                        className="p-2 bg-slate-900 border border-slate-700 text-slate-400 rounded-lg hover:text-white shadow-lg focus-ring-dark"
                    >
                        <Menu size={20} />
                    </button>
                </div>
            )}
        </div>
    );
};

export default DualRailSidebar;
