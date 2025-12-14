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
        { path: '/users', icon: Users, label: 'User Management' },
        { path: '/settings', icon: Settings, label: 'Settings' },
    ];

    const getPageTitle = () => {
        const item = navItems.find(i => i.path === location.pathname);
        return item ? item.label : 'Dashboard';
    };

    return (
        <div className="flex h-screen bg-slate-50 font-sans text-slate-900">
            {/* Sidebar */}
            <aside className="w-64 bg-slate-900 text-white flex flex-col shadow-xl z-20">
                <div className="p-6 flex items-center gap-3 border-b border-slate-800">
                    <div className="w-8 h-8 bg-brand-600 rounded-lg flex items-center justify-center">
                        <Shield className="text-white" size={20} />
                    </div>
                    <div>
                        <h1 className="text-lg font-bold tracking-tight">Policy Engine Control Plane</h1>
                    </div>
                </div>

                <nav className="flex-1 p-4 space-y-1">
                    <p className="px-4 text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2 mt-2">Main Menu</p>
                    {navItems.map((item) => (
                        <Link
                            key={item.path}
                            to={item.path}
                            className={`flex items-center gap-3 px-4 py-2.5 rounded-md transition-all duration-200 group ${isActive(item.path)
                                ? 'bg-brand-600 text-white shadow-md'
                                : 'text-slate-300 hover:bg-slate-800 hover:text-white'
                                }`}
                        >
                            <item.icon size={18} className={isActive(item.path) ? 'text-white' : 'text-slate-400 group-hover:text-white'} />
                            <span className="font-medium text-sm">{item.label}</span>
                        </Link>
                    ))}
                </nav>

                <div className="p-4 border-t border-slate-800">
                    <button className="flex items-center gap-3 w-full px-4 py-2.5 text-slate-300 hover:text-white hover:bg-slate-800 rounded-md transition-colors">
                        <LogOut size={18} />
                        <span className="font-medium text-sm">Sign Out</span>
                    </button>
                </div>
            </aside>

            {/* Main Content Wrapper */}
            <div className="flex-1 flex flex-col overflow-hidden">
                {/* Header */}
                <header className="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-8 shadow-sm z-10">
                    <div className="flex items-center gap-4">
                        <button className="p-2 text-slate-500 hover:bg-slate-100 rounded-md lg:hidden">
                            <Menu size={20} />
                        </button>
                        <nav className="flex items-center text-sm text-slate-500">
                            <span className="hover:text-slate-900 cursor-pointer">Home</span>
                            <ChevronRight size={14} className="mx-2" />
                            <span className="font-medium text-slate-900">{getPageTitle()}</span>
                        </nav>
                    </div>

                    <div className="flex items-center gap-6">
                        <div className="relative hidden md:block">
                            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-slate-400" size={16} />
                            <input
                                type="text"
                                placeholder="Global search..."
                                className="pl-9 pr-4 py-1.5 bg-slate-100 border-transparent focus:bg-white focus:border-brand-500 focus:ring-0 rounded-md text-sm w-64 transition-all"
                            />
                        </div>
                        <button className="relative p-2 text-slate-500 hover:bg-slate-100 rounded-full transition-colors">
                            <Bell size={20} />
                            <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-red-500 rounded-full border-2 border-white"></span>
                        </button>
                        <div className="flex items-center gap-3 pl-6 border-l border-slate-200">
                            <div className="text-right hidden md:block">
                                <p className="text-sm font-medium text-slate-900">Admin User</p>
                                <p className="text-xs text-slate-500">System Administrator</p>
                            </div>
                            <div className="w-9 h-9 bg-brand-100 text-brand-700 rounded-full flex items-center justify-center font-bold border border-brand-200">
                                AD
                            </div>
                        </div>
                    </div>
                </header>

                {/* Page Content */}
                <main className="flex-1 overflow-auto bg-slate-50 p-8">
                    <div className="max-w-7xl mx-auto">
                        {children}
                    </div>
                </main>
            </div>
        </div>
    );
};

export default Layout;
