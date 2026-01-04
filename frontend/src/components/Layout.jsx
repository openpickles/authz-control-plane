import React from 'react';
import { useLocation } from 'react-router-dom';
import { Bell, Search } from 'lucide-react';
import DualRailSidebar from './DualRailSidebar';
import Breadcrumbs from './Breadcrumbs';

const Layout = ({ children }) => {
    return (
        <div className="flex h-screen bg-slate-50 font-sans text-slate-900 overflow-hidden">
            {/* Accessibility: Skip to Content */}
            <a
                href="#main-content"
                className="sr-only focus:not-sr-only focus:absolute focus:top-4 focus:left-20 focus:z-50 focus:px-4 focus:py-2 focus:bg-white focus:text-brand-600 focus:shadow-lg focus:rounded-md focus:font-bold focus:outline-none focus:ring-2 focus:ring-brand-500"
            >
                Skip to content
            </a>

            {/* New Dual-Rail Sidebar */}
            <DualRailSidebar />

            {/* Main Content Wrapper */}
            <div className="flex-1 flex flex-col min-w-0 transition-all duration-300">
                {/* Header */}
                <header className="h-16 bg-white/80 backdrop-blur-md border-b border-slate-200/60 flex items-center justify-between px-6 z-10 flex-shrink-0 sticky top-0">
                    <div className="flex items-center gap-4 flex-1">
                        <Breadcrumbs />

                        {/* Global Search Bar (Command K) */}
                        <div className="hidden md:flex items-center max-w-md w-full ml-8 relative group">
                            <Search className="absolute left-3 text-slate-400 group-focus-within:text-brand-500 transition-colors" size={16} />
                            <input
                                type="text"
                                placeholder="Search policies, resources... (Cmd+K)"
                                className="w-full pl-9 pr-4 py-1.5 bg-slate-100 border-transparent focus:bg-white focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 rounded-md text-sm transition-all outline-none"
                                aria-label="Global Search"
                            />
                        </div>
                    </div>

                    <div className="flex items-center gap-5">
                        <button className="text-slate-400 hover:text-slate-600 transition-colors relative focus-ring rounded-full p-1">
                            <Bell size={20} />
                            <span className="absolute top-1 right-1 block h-2 w-2 transform translate-x-1/4 -translate-y-1/4 rounded-full bg-red-500 ring-2 ring-white" />
                        </button>

                        <div className="h-6 w-px bg-slate-200"></div>

                        <div className="flex items-center gap-3">
                            <div className="text-right hidden md:block leading-tight">
                                <p className="text-sm font-semibold text-slate-900">Admin User</p>
                                <p className="text-xs text-slate-500">Global Administrator</p>
                            </div>
                            <div className="w-9 h-9 bg-gradient-to-br from-brand-50 to-brand-100 text-brand-600 rounded-full flex items-center justify-center font-bold border border-brand-200 shadow-sm ring-2 ring-white">
                                AD
                            </div>
                            <button onClick={() => window.location.href = '/login'} className="text-xs text-red-500 hover:text-red-700 font-medium ml-2">
                                Logout
                            </button>
                        </div>
                    </div>
                </header>

                {/* Page Content - Bento Grid Styled Container */}
                <main id="main-content" className="flex-1 overflow-auto p-4 md:p-6 lg:p-8 scroll-smooth relative" tabIndex="-1">
                    {/* 
                        We wrap children in a constrained max-width container 
                        to ensure content doesn't stretch too wide on huge screens 
                     */}
                    <div className="max-w-[1600px] mx-auto w-full h-full flex flex-col">
                        {children}
                    </div>
                </main>
            </div>
        </div>
    );
};

export default Layout;
