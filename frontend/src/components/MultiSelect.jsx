import React, { useState, useRef, useEffect } from 'react';
import { Check, ChevronDown, X } from 'lucide-react';

const MultiSelect = ({
    options = [],
    value = [],
    onChange,
    placeholder = "Select items...",
    label
}) => {
    const [isOpen, setIsOpen] = useState(false);
    const [searchTerm, setSearchTerm] = useState("");
    const containerRef = useRef(null);

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (containerRef.current && !containerRef.current.contains(event.target)) {
                setIsOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const toggleOption = (optionValue) => {
        const newValue = value.includes(optionValue)
            ? value.filter(v => v !== optionValue)
            : [...value, optionValue];
        onChange(newValue);
    };

    const removeOption = (e, optionValue) => {
        e.stopPropagation();
        onChange(value.filter(v => v !== optionValue));
    };

    const filteredOptions = options.filter(opt =>
        opt.label.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <div className="w-full relative" ref={containerRef}>
            {label && (
                <label className="block text-sm font-medium text-slate-700 mb-1">
                    {label}
                </label>
            )}
            <div
                className="min-h-[38px] w-full border border-slate-300 rounded-md bg-white px-3 py-1.5 text-sm shadow-sm focus-within:border-brand-500 focus-within:ring-1 focus-within:ring-brand-500 cursor-pointer flex flex-wrap gap-1.5 items-center"
                onClick={() => setIsOpen(!isOpen)}
            >
                {value.length === 0 && (
                    <span className="text-slate-500">{placeholder}</span>
                )}
                {value.map(val => {
                    const option = options.find(o => o.value === val);
                    return (
                        <span key={val} className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-brand-50 text-brand-700 border border-brand-200">
                            {option ? option.label : val}
                            <button
                                type="button"
                                onClick={(e) => removeOption(e, val)}
                                className="ml-1 text-brand-400 hover:text-brand-600 focus:outline-none"
                            >
                                <X className="h-3 w-3" />
                            </button>
                        </span>
                    );
                })}
                <div className="flex-grow flex justify-end">
                    <ChevronDown className="h-4 w-4 text-slate-400" />
                </div>
            </div>

            {isOpen && (
                <div className="absolute z-10 mt-1 w-full bg-white shadow-lg max-h-60 rounded-md py-1 text-base ring-1 ring-black ring-opacity-5 overflow-auto focus:outline-none sm:text-sm">
                    <div className="px-2 py-1 sticky top-0 bg-white border-b border-slate-100">
                        <input
                            type="text"
                            className="w-full border-none p-1 text-sm focus:ring-0 text-slate-700 placeholder-slate-400"
                            placeholder="Search..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            onClick={(e) => e.stopPropagation()}
                        />
                    </div>
                    {filteredOptions.length === 0 ? (
                        <div className="cursor-default select-none relative py-2 px-4 text-slate-500">
                            No results found
                        </div>
                    ) : (
                        filteredOptions.map((option) => (
                            <div
                                key={option.value}
                                className={`cursor-pointer select-none relative py-2 pl-3 pr-9 hover:bg-slate-50 ${value.includes(option.value) ? 'bg-brand-50 text-brand-900' : 'text-slate-900'}`}
                                onClick={() => toggleOption(option.value)}
                            >
                                <div className="flex items-center">
                                    <span className={`block truncate ${value.includes(option.value) ? 'font-semibold' : 'font-normal'}`}>
                                        {option.label}
                                    </span>
                                </div>
                                {value.includes(option.value) && (
                                    <span className="absolute inset-y-0 right-0 flex items-center pr-4 text-brand-600">
                                        <Check className="h-4 w-4" />
                                    </span>
                                )}
                            </div>
                        ))
                    )}
                </div>
            )}
        </div>
    );
};

export default MultiSelect;
