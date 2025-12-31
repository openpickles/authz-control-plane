import React from 'react';
import { useLocation, Link } from 'react-router-dom';
import { ChevronRight, Home } from 'lucide-react';

const Breadcrumbs = () => {
    const location = useLocation();
    const pathnames = location.pathname.split('/').filter(Boolean);

    const capitalize = (s) => s.charAt(0).toUpperCase() + s.slice(1).replaceAll(/-/g, ' ');

    return (
        <nav aria-label="Breadcrumb" className="flex items-center text-sm text-slate-500">
            <Link
                to="/"
                className="hover:text-slate-900 transition-colors flex items-center gap-1 focus-ring rounded-sm px-1"
                aria-label="Home"
            >
                <Home size={14} />
            </Link>

            {pathnames.map((value, index) => {
                const to = `/${pathnames.slice(0, index + 1).join('/')}`;
                const isLast = index === pathnames.length - 1;

                return (
                    <React.Fragment key={to}>
                        <ChevronRight size={14} className="mx-1 text-slate-400" />
                        {isLast ? (
                            <span
                                className="font-medium text-slate-900 px-1"
                                aria-current="page"
                            >
                                {capitalize(value)}
                            </span>
                        ) : (
                            <Link
                                to={to}
                                className="hover:text-slate-900 transition-colors focus-ring rounded-sm px-1"
                            >
                                {capitalize(value)}
                            </Link>
                        )}
                    </React.Fragment>
                );
            })}
        </nav>
    );
};

export default Breadcrumbs;
