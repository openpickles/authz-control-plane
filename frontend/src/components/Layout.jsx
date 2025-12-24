import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { LayoutDashboard, Shield, Users, Settings, LogOut, Server, Menu, Bell, Search, ChevronRight, Link as LinkIcon, Package } from 'lucide-react';

const Layout = ({ children }) => {
    const location = useLocation();

    const isActive = (path) => location.pathname === path;

    const navItems = [
        { path: '/', icon: LayoutDashboard, label: 'Dashboard' },
        { path: '/policies', icon: Shield, label: 'Policy Editor' },
        { path: '/entitlements', icon: Users, label: 'Entitlements' },
        { path: '/policy-bindings', icon: LinkIcon, label: 'Policy Bindings' },
        { path: '/policy-bundles', icon: Package, label: 'Policy Bundles' },
        { path: '/providers', icon: Server, label: 'Resource Providers' },
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
            <aside className="w-64 bg-slate-900 text-slate-300 flex flex-col shadow-xl z-20 flex-shrink-0 transition-all duration-300 ease-in-out">
                <div className="h-16 flex items-center px-6 bg-slate-950 border-b border-slate-800">
                    <Shield className="text-brand-500 mr-3" size={24} />
                    <span className="font-bold text-white tracking-wide text-sm uppercase">Policy Engine</span>
                </div>

                <nav className="flex-1 overflow-y-auto py-6 px-3 space-y-1">
                    <p className="px-3 text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">Main Menu</p>
                    {navItems.map((item) => (
                        <Link
                            key={item.path}
                            to={item.path}
                            className={`flex items-center gap-3 px-3 py-2 rounded-md transition-colors duration-200 group ${isActive(item.path)
                                ? 'bg-brand-600 text-white shadow-md'
                                : 'hover:bg-slate-800 hover:text-white text-slate-400'
                                }`}
                        >
                            <item.icon size={18} className={isActive(item.path) ? 'text-white' : 'text-slate-500 group-hover:text-white'} />
                            <span className="font-medium text-sm">{item.label}</span>
                        </Link>
                    ))}
                </nav>

                <div className="p-4 border-t border-slate-800 bg-slate-950">
                    <button
                        onClick={handleLogout}
                        className="flex items-center gap-3 w-full px-4 py-2 text-slate-400 hover:text-red-400 hover:bg-slate-900 rounded-md transition-colors"
                    >
                        <LogOut size={18} />
                        <span className="font-medium text-sm">Sign Out</span>
                    </button>
                </div>
            </aside>

            {/* Main Content Wrapper */}
            <div className="flex-1 flex flex-col overflow-hidden bg-slate-100">
                {/* Header */}
                <header className="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-6 shadow-sm z-10 flex-shrink-0">
                    <div className="flex items-center gap-4">
                        <nav className="flex items-center text-sm text-slate-500">
                            <Link to="/" className="hover:text-brand-600 transition-colors">Home</Link>
                            <ChevronRight size={14} className="mx-2 text-slate-400" />
                            <span className="font-medium text-slate-900 text-base">{getPageTitle()}</span>
                        </nav>
                    </div>

                    <div className="flex items-center gap-4">
                        <div className="flex items-center gap-3 pl-6 border-l border-slate-200">
                            <div className="text-right hidden md:block">
                                <p className="text-sm font-medium text-slate-900">Admin User</p>
                                <p className="text-xs text-slate-500">Administrator</p>
                            </div>
                            <div className="w-8 h-8 bg-brand-100 text-brand-700 rounded-full flex items-center justify-center font-bold border border-brand-200">
                                AD
                            </div>
                        </div>
                    </div>
                </header>

                {/* Page Content - Full Width/Height Fluid Container */}
                <main className="flex-1 overflow-auto p-6 relative">
                    <div className="h-full w-full flex flex-col">
                        {children}
                    </div>
                </main>
            </div>
        </div>
    );
};

export default Layout;
