import React, { useState, useEffect, useCallback } from 'react';
import { getAuditLogs } from '../services/audit';
import { ShieldCheck, Search, Filter, RefreshCw, X } from 'lucide-react';

const AuditLog = () => {
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [selectedLog, setSelectedLog] = useState(null);

    // Filter State
    const [filters, setFilters] = useState({
        action: '',
        resourceType: '',
        username: '',
        status: '',
        startDate: '',
        endDate: ''
    });

    const [appliedFilters, setAppliedFilters] = useState({});

    const fetchLogs = useCallback(async () => {
        setLoading(true);
        try {
            const params = {
                page,
                size: 20,
                sort: 'timestamp,desc',
                ...appliedFilters
            };

            // Handle date conversion if necessary for backend Instant (usually ISO string works)
            if (params.startDate) params.startDate = new Date(params.startDate).toISOString();
            if (params.endDate) params.endDate = new Date(params.endDate).toISOString();

            const data = await getAuditLogs(params);
            setLogs(data.content);
            setTotalPages(data.totalPages);
        } catch (e) {
            console.error("Failed to load logs", e);
        } finally {
            setLoading(false);
        }
    }, [page, appliedFilters]);

    useEffect(() => {
        fetchLogs();
    }, [fetchLogs]);

    const handleFilterChange = (e) => {
        const { name, value } = e.target;
        setFilters(prev => ({ ...prev, [name]: value }));
    };

    const applyFilters = (e) => {
        e.preventDefault();
        setPage(0); // Reset to first page

        // Remove empty filters
        const activeFilters = {};
        Object.keys(filters).forEach(key => {
            if (filters[key]) activeFilters[key] = filters[key];
        });

        setAppliedFilters(activeFilters);
    };

    const clearFilters = () => {
        setFilters({
            action: '',
            resourceType: '',
            username: '',
            status: '',
            startDate: '',
            endDate: ''
        });
        setAppliedFilters({});
        setPage(0);
    };

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h1 className="text-3xl font-bold text-gray-900 flex items-center gap-2">
                    <ShieldCheck className="h-8 w-8 text-indigo-600" />
                    Audit Logs
                </h1>
            </div>

            {/* Filter Bar */}
            <div className="bg-white p-4 rounded-lg shadow border border-gray-200">
                <form onSubmit={applyFilters} className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                    {/* Actor Filter */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700">Actor (Username)</label>
                        <input
                            type="text"
                            name="username"
                            value={filters.username}
                            onChange={handleFilterChange}
                            placeholder="e.g. admin"
                            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2"
                        />
                    </div>

                    {/* Action Filter */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700">Action</label>
                        <select
                            name="action"
                            value={filters.action}
                            onChange={handleFilterChange}
                            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2"
                        >
                            <option value="">All Actions</option>
                            <option value="CREATE">CREATE</option>
                            <option value="UPDATE">UPDATE</option>
                            <option value="DELETE">DELETE</option>
                            <option value="LOGIN">LOGIN</option>
                            <option value="LOGOUT">LOGOUT</option>
                            <option value="SYNC">SYNC</option>
                            <option value="PUSH_TO_GIT">PUSH_TO_GIT</option>
                        </select>
                    </div>

                    {/* Resource Type Filter */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700">Resource Type</label>
                        <select
                            name="resourceType"
                            value={filters.resourceType}
                            onChange={handleFilterChange}
                            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2"
                        >
                            <option value="">All Resources</option>
                            <option value="POLICY">POLICY</option>
                            <option value="ENTITLEMENT">ENTITLEMENT</option>
                            <option value="USER">USER</option>
                            <option value="SYSTEM">SYSTEM</option>
                        </select>
                    </div>

                    {/* Status Filter */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700">Status</label>
                        <select
                            name="status"
                            value={filters.status}
                            onChange={handleFilterChange}
                            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2"
                        >
                            <option value="">All Statuses</option>
                            <option value="SUCCESS">Success</option>
                            <option value="FAILURE">Failure</option>
                        </select>
                    </div>

                    {/* Date Range Start */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700">Start Date</label>
                        <input
                            type="datetime-local"
                            name="startDate"
                            value={filters.startDate}
                            onChange={handleFilterChange}
                            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2"
                        />
                    </div>

                    {/* Date Range End */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700">End Date</label>
                        <input
                            type="datetime-local"
                            name="endDate"
                            value={filters.endDate}
                            onChange={handleFilterChange}
                            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2"
                        />
                    </div>

                    <div className="lg:col-span-2 flex items-end gap-2">
                        <button
                            type="submit"
                            className="flex-1 bg-indigo-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 flex justify-center items-center gap-2"
                        >
                            <Filter size={16} /> Apply Filters
                        </button>
                        <button
                            type="button"
                            onClick={clearFilters}
                            className="bg-white text-gray-700 border border-gray-300 px-4 py-2 rounded-md text-sm font-medium hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 flex items-center gap-2"
                        >
                            <X size={16} /> Clear
                        </button>
                        <button
                            type="button"
                            onClick={fetchLogs}
                            className="bg-white text-gray-700 border border-gray-300 px-4 py-2 rounded-md text-sm font-medium hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
                            title="Refresh"
                        >
                            <RefreshCw size={16} />
                        </button>
                    </div>
                </form>
            </div>

            <div className="bg-white shadow rounded-lg overflow-hidden border border-gray-200">
                <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Timestamp</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actor</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Action</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Resource</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Details</th>
                            </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                            {loading ? (
                                <tr><td colSpan="6" className="text-center py-4">Loading...</td></tr>
                            ) : logs.length === 0 ? (
                                <tr><td colSpan="6" className="text-center py-4 text-gray-500">No logs found matching your criteria.</td></tr>
                            ) : logs.map((log) => (
                                <tr key={log.id} className="hover:bg-gray-50 cursor-pointer" onClick={() => setSelectedLog(log)}>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                        {new Date(log.timestamp).toLocaleString()}
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                                        {log.actorUsername}
                                        <div className="text-xs text-gray-400">{log.clientIp}</div>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-700">
                                        <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${log.action === 'DELETE' ? 'bg-red-100 text-red-800' :
                                            log.action === 'CREATE' ? 'bg-green-100 text-green-800' :
                                                'bg-blue-100 text-blue-800'
                                            }`}>
                                            {log.action}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                        {log.resourceType} <span className="text-xs text-gray-400">({log.resourceId})</span>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                                        {log.status === 'SUCCESS' ? (
                                            <span className="text-green-600 font-medium">Success</span>
                                        ) : (
                                            <span className="text-red-600 font-medium flex items-center gap-1">
                                                {/* Start Icon Code for AlertTriangle manually to avoid imports if desired, but we imported it */}
                                                <span className="text-red-500">Failed</span>
                                            </span>
                                        )}
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                                        <button className="text-indigo-600 hover:text-indigo-900">View</button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

                <div className="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6">
                    <div className="flex-1 flex justify-between sm:hidden">
                        <button disabled={page === 0} onClick={() => setPage(p => p - 1)} className="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:text-gray-500">Previous</button>
                        <button disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)} className="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:text-gray-500">Next</button>
                    </div>
                    <div className="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
                        <div>
                            <p className="text-sm text-gray-700">
                                Showing page <span className="font-medium">{page + 1}</span> of <span className="font-medium">{totalPages || 1}</span>
                            </p>
                        </div>
                        <div>
                            <nav className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px" aria-label="Pagination">
                                <button disabled={page === 0} onClick={() => setPage(p => p - 1)} className="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50">Previous</button>
                                <button disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)} className="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50">Next</button>
                            </nav>
                        </div>
                    </div>
                </div>
            </div>

            {/* Detail Modal */}
            {selectedLog && (
                <div className="fixed inset-0 bg-gray-500 bg-opacity-75 flex items-center justify-center p-4 z-50">
                    <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
                        <div className="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
                            <h3 className="text-lg font-medium text-gray-900">Log Details</h3>
                            <button onClick={() => setSelectedLog(null)} className="text-gray-400 hover:text-gray-500">
                                <span className="sr-only">Close</span>
                                <X className="h-6 w-6" />
                            </button>
                        </div>
                        <div className="px-6 py-4 space-y-4">
                            <div className="grid grid-cols-2 gap-4 text-sm">
                                <div><span className="font-semibold">ID:</span> {selectedLog.id}</div>
                                <div><span className="font-semibold">Request ID:</span> {selectedLog.requestId}</div>
                                <div><span className="font-semibold">Session ID:</span> {selectedLog.sessionId}</div>
                                <div><span className="font-semibold">User Agent:</span> <span className="break-all">{selectedLog.userAgent}</span></div>
                                <div className="col-span-2"><span className="font-semibold">Checksum:</span> <span className="font-mono text-xs break-all bg-gray-100 p-1 rounded">{selectedLog.checksum}</span></div>
                            </div>

                            {selectedLog.failureReason && (
                                <div className="bg-red-50 p-3 rounded-md border border-red-200">
                                    <h4 className="text-sm font-medium text-red-800">Failure Reason</h4>
                                    <p className="text-sm text-red-700 mt-1">{selectedLog.failureReason}</p>
                                </div>
                            )}

                            <div>
                                <h4 className="text-sm font-medium text-gray-900 mb-2">New Values (Active State)</h4>
                                <pre className="bg-gray-800 text-green-400 p-3 rounded-md text-xs overflow-auto max-h-60">
                                    {selectedLog.newValues ? JSON.stringify(JSON.parse(selectedLog.newValues), null, 2) : 'No data'}
                                </pre>
                            </div>
                        </div>
                        <div className="px-6 py-4 border-t border-gray-200 bg-gray-50 flex justify-end">
                            <button onClick={() => setSelectedLog(null)} className="px-4 py-2 bg-white border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50">
                                Close
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AuditLog;
