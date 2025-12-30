import React from 'react';
import { ChevronLeft, ChevronRight, Search, Download } from 'lucide-react';

const Pagination = ({ page, size, totalElements, totalPages, onPageChange }) => {
    const start = page * size + 1;
    const end = Math.min((page + 1) * size, totalElements);

    return (
        <div className="flex items-center justify-between border-t border-slate-200 bg-white px-4 py-3 sm:px-6">
            <div className="flex flex-1 justify-between sm:hidden">
                <button
                    onClick={() => onPageChange(page - 1)}
                    disabled={page === 0}
                    className="relative inline-flex items-center rounded-md border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-50"
                >
                    Previous
                </button>
                <button
                    onClick={() => onPageChange(page + 1)}
                    disabled={page >= totalPages - 1}
                    className="relative ml-3 inline-flex items-center rounded-md border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-50"
                >
                    Next
                </button>
            </div>
            <div className="hidden sm:flex sm:flex-1 sm:items-center sm:justify-between">
                <div>
                    <p className="text-sm text-slate-700">
                        Showing <span className="font-medium">{start}</span> to <span className="font-medium">{end}</span> of{' '}
                        <span className="font-medium">{totalElements}</span> results
                    </p>
                </div>
                <div>
                    <nav className="isolate inline-flex -space-x-px rounded-md shadow-sm" aria-label="Pagination">
                        <button
                            onClick={() => onPageChange(page - 1)}
                            disabled={page === 0}
                            className="relative inline-flex items-center rounded-l-md px-2 py-2 text-slate-400 ring-1 ring-inset ring-slate-300 hover:bg-slate-50 focus:z-20 focus:outline-offset-0 disabled:opacity-50"
                        >
                            <span className="sr-only">Previous</span>
                            <ChevronLeft className="h-5 w-5" aria-hidden="true" />
                        </button>

                        {/* Simple generic page indicator */}
                        <div className="relative inline-flex items-center px-4 py-2 text-sm font-semibold text-slate-900 ring-1 ring-inset ring-slate-300 focus:outline-offset-0">
                            Page {page + 1} of {totalPages}
                        </div>

                        <button
                            onClick={() => onPageChange(page + 1)}
                            disabled={page >= totalPages - 1}
                            className="relative inline-flex items-center rounded-r-md px-2 py-2 text-slate-400 ring-1 ring-inset ring-slate-300 hover:bg-slate-50 focus:z-20 focus:outline-offset-0 disabled:opacity-50"
                        >
                            <span className="sr-only">Next</span>
                            <ChevronRight className="h-5 w-5" aria-hidden="true" />
                        </button>
                    </nav>
                </div>
            </div>
        </div>
    );
};

export default function DataGrid({
    columns,
    data,
    loading,
    page,
    size,
    totalElements,
    totalPages,
    onPageChange,
    onSearch,
    actionButton
}) {
    return (
        <div className="bg-white shadow-sm ring-1 ring-slate-900/5 sm:rounded-xl overflow-hidden">
            {/* Toolbar */}
            <div className="border-b border-slate-200 bg-slate-50 px-4 py-4 sm:flex sm:items-center sm:justify-between sm:px-6">
                <div className="flex flex-1 items-center gap-4">
                    <div className="relative max-w-sm flex-1">
                        <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                            <Search className="h-4 w-4 text-slate-400" aria-hidden="true" />
                        </div>
                        <input
                            type="text"
                            placeholder="Search..."
                            className="block w-full rounded-md border-0 py-1.5 pl-10 text-slate-900 ring-1 ring-inset ring-slate-300 placeholder:text-slate-400 focus:ring-2 focus:ring-inset focus:ring-brand-600 sm:text-sm sm:leading-6 bg-white"
                            onChange={(e) => onSearch && onSearch(e.target.value)}
                        />
                    </div>
                </div>
                <div className="mt-4 sm:ml-4 sm:mt-0 flex gap-2">
                    {actionButton}
                </div>
            </div>

            {/* Table */}
            <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-slate-300">
                    <thead className="bg-slate-50">
                        <tr>
                            {columns.map((col, idx) => (
                                <th
                                    key={idx}
                                    scope="col"
                                    className={`py-3.5 pl-4 pr-3 text-left text-xs font-semibold text-slate-900 uppercase tracking-wide sm:pl-6 ${col.className || ''}`}
                                >
                                    {col.header}
                                </th>
                            ))}
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-200 bg-white">
                        {loading && (
                            <tr>
                                <td colSpan={columns.length} className="px-6 py-12 text-center text-slate-500">
                                    Loading...
                                </td>
                            </tr>
                        )}
                        {!loading && data.length === 0 && (
                            <tr>
                                <td colSpan={columns.length} className="px-6 py-12 text-center text-slate-500">
                                    No data found.
                                </td>
                            </tr>
                        )}
                        {!loading && data.map((row, rowIdx) => (
                            <tr key={rowIdx} className="hover:bg-slate-50 transition-colors">
                                {columns.map((col, colIdx) => (
                                    <td
                                        key={colIdx}
                                        className={`whitespace-nowrap py-4 pl-4 pr-3 text-sm font-medium text-slate-900 sm:pl-6 ${col.cellClassName || ''}`}
                                    >
                                        {col.render ? col.render(row) : row[col.accessor]}
                                    </td>
                                ))}
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {/* Pagination */}
            <Pagination
                page={page}
                size={size}
                totalElements={totalElements}
                totalPages={totalPages}
                onPageChange={onPageChange}
            />
        </div>
    );
}
