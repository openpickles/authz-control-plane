import React from 'react';

const DynamicFilter = ({ schema, filters, onChange, onSearch }) => {
    if (!schema || !schema.filters) return null;

    const handleChange = (key, value) => {
        onChange({ ...filters, [key]: value });
    };

    return (
        <div className="bg-slate-50 p-4 rounded-lg border border-slate-200 mb-4 space-y-4">
            <h4 className="text-sm font-semibold text-slate-700">Filter Resources</h4>
            <div className="grid grid-cols-2 gap-4">
                {schema.filters.map((field) => (
                    <div key={field.key}>
                        <label className="block text-xs font-medium text-slate-500 mb-1">
                            {field.label}
                        </label>
                        {field.type === 'select' ? (
                            <select
                                value={filters[field.key] || ''}
                                onChange={(e) => handleChange(field.key, e.target.value)}
                                className="input-field text-sm py-1"
                            >
                                <option value="">All</option>
                                {field.options.map((opt) => (
                                    <option key={opt.value} value={opt.value}>
                                        {opt.label}
                                    </option>
                                ))}
                            </select>
                        ) : (
                            <input
                                type={field.type}
                                value={filters[field.key] || ''}
                                onChange={(e) => handleChange(field.key, e.target.value)}
                                className="input-field text-sm py-1"
                                placeholder={field.label}
                            />
                        )}
                    </div>
                ))}
            </div>
            <div className="flex justify-end">
                <button
                    type="button"
                    onClick={onSearch}
                    className="btn-secondary text-xs py-1 px-3"
                >
                    Apply Filters
                </button>
            </div>
        </div>
    );
};

export default DynamicFilter;
