import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { LayoutDashboard, Shield, Users, Settings, LogOut, Server, Menu, Bell, Search, ChevronRight, Link as LinkIcon, Package, ShieldCheck } from 'lucide-react';

const Layout = ({ children }) => {
    const location = useLocation();

    const isActive = (path) => location.pathname === path;

    const navItems = [
        { path: '/', icon: LayoutDashboard, label: 'Dashboard' },
        { path: '/policies', icon: Shield, label: 'Policy Editor' },
        { path: '/entitlements', icon: Users, label: 'Entitlements' },
        { path: '/policy-bindings', icon: LinkIcon, label: 'Policy Bindings' },
        { path: '/policy-bundles', icon: Package, label: 'Policy Bundles' },
        { path: '/resource-types', icon: Server, label: 'Resource Types' },
        { path: '/audit', icon: ShieldCheck, label: 'Audit Logs' },
        { path: '/settings', icon: Settings, label: 'Settings' },
    ];

    const getPageTitle = () => {
        const item = navItems.find(i => i.path === location.pathname);
        return item ? item.label : 'Dashboard';
    };

    const handleLogout = () => {
        // Redirect to backend logout endpoint to clear session cookie
        // In dev, we are on localhost:5173, backend is on 8080.
        // In prod, we are on same origin.
        const logoutUrl = import.meta.env.DEV ? 'http://localhost:8080/logout' : '/logout';
        window.location.href = logoutUrl;
    };

    return (
        <div className="flex h-screen bg-slate-50 font-sans text-slate-900">
            {/* Sidebar */}
            <aside className="w-72 bg-slate-900 text-slate-300 flex flex-col flex-shrink-0 transition-all duration-300 ease-in-out border-r border-slate-800">
                <div className="h-16 flex items-center px-6 bg-slate-950 border-b border-slate-800">
                    <div className="flex items-center gap-3">
                        <div className="bg-brand-600 p-1.5 rounded-lg">
                            <Shield className="text-white" size={20} />
                        </div>
                        <span className="font-bold text-white tracking-wide text-sm uppercase">Policy Engine</span>
                    </div>
                </div>

                <nav className="flex-1 overflow-y-auto px-4 py-6 space-y-1">
                    <p className="px-2 text-xs font-bold text-slate-500 uppercase tracking-wider mb-4">Platform</p>
                    {navItems.map((item) => (
                        <Link
                            key={item.path}
                            to={item.path}
                            className={`flex items-center gap-3 px-3 py-2.5 rounded-lg transition-all duration-200 group ${isActive(item.path)
                                ? 'bg-brand-600/10 text-brand-400 border border-brand-600/20'
                                : 'hover:bg-slate-800 hover:text-white text-slate-400 border border-transparent'
                                }`}
                        >
                            <item.icon size={20} className={isActive(item.path) ? 'text-brand-400' : 'text-slate-500 group-hover:text-white transition-colors'} />
                            <span className="font-medium text-sm">{item.label}</span>
                        </Link>
                    ))}
                </nav>

                <div className="p-4 border-t border-slate-800 bg-slate-950">
                    <button
                        onClick={handleLogout}
                        className="flex items-center gap-3 w-full px-4 py-3 text-slate-400 hover:text-red-400 hover:bg-slate-900 rounded-lg transition-colors group"
                    >
                        <LogOut size={20} className="group-hover:text-red-400 transition-colors" />
                        <span className="font-medium text-sm">Sign Out</span>
                    </button>
                    <div className="mt-4 pt-4 border-t border-slate-900 text-center">
                        <p className="text-xs text-slate-600">v0.0.1 Enterprise Edition</p>
                    </div>
                </div>
            </aside>

            {/* Main Content Wrapper */}
            <div className="flex-1 flex flex-col overflow-hidden bg-slate-50">
                {/* Header */}
                <header className="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-8 shadow-sm z-10 flex-shrink-0">
                    <div className="flex items-center gap-4">
                        <nav className="flex items-center text-sm text-slate-500">
                            <span className="font-medium text-slate-900 text-lg tracking-tight">{getPageTitle()}</span>
                        </nav>
                    </div>

                    <div className="flex items-center gap-6">
                        <button className="text-slate-400 hover:text-slate-600 transition-colors relative">
                            <Bell size={20} />
                            <span className="absolute top-0 right-0 block h-2 w-2 transform -translate-y-1/2 translate-x-1/2 rounded-full bg-red-500 ring-2 ring-white" />
                        </button>
                        <div className="h-6 w-px bg-slate-200"></div>
                        <div className="flex items-center gap-3">
                            <div className="text-right hidden md:block leading-tight">
                                <p className="text-sm font-semibold text-slate-900">Admin User</p>
                                <p className="text-xs text-slate-500">Global Administrator</p>
                            </div>
                            <div className="w-9 h-9 bg-brand-50 text-brand-600 rounded-full flex items-center justify-center font-bold border border-brand-200 shadow-sm ring-2 ring-white">
                                AD
                            </div>
                        </div>
                    </div>
                </header>

                {/* Page Content - Centered & Constrained */}
                <main className="flex-1 overflow-auto p-8">
                    <div className="max-w-7xl mx-auto w-full h-full flex flex-col">
                        {children}
                    </div>
                </main>
            </div>
        </div>
    );
};

export default Layout;
