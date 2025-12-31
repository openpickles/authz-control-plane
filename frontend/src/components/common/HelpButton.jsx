import React, { useState, useRef, useEffect } from 'react';
import { HelpCircle, X } from 'lucide-react'; // Assuming Lucide is available

import { helpContent } from '../../config/help-content';

const HelpButton = ({ topic }) => {
    const [isOpen, setIsOpen] = useState(false);
    const popoverRef = useRef(null);
    const buttonRef = useRef(null);

    const content = helpContent[topic];

    // Close on click outside
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (isOpen &&
                popoverRef.current && !popoverRef.current.contains(event.target) &&
                buttonRef.current && !buttonRef.current.contains(event.target)) {
                setIsOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, [isOpen]);

    if (!content) return null;

    return (
        <div className="relative inline-block">
            <button
                ref={buttonRef}
                onClick={() => setIsOpen(!isOpen)}
                className={`p-1.5 rounded-md transition-colors ${isOpen ? 'bg-indigo-100 text-indigo-700' : 'text-slate-400 hover:text-indigo-600 hover:bg-slate-100'}`}
                title="Help"
                aria-label="Show Help"
            >
                <HelpCircle size={18} />
            </button>

            {isOpen && (
                <div
                    ref={popoverRef}
                    className="absolute right-0 top-full mt-2 w-80 bg-white border border-slate-200 rounded-lg shadow-xl z-50 animate-in fade-in zoom-in-95 duration-100 origin-top-right"
                >
                    <div className="flex justify-between items-center p-3 border-b border-slate-100 bg-slate-50/50 rounded-t-lg">
                        <h4 className="font-semibold text-slate-800 text-sm flex items-center gap-2">
                            <HelpCircle size={14} className="text-indigo-500" />
                            {content.title}
                        </h4>
                        <button onClick={() => setIsOpen(false)} className="text-slate-400 hover:text-slate-600">
                            <X size={14} />
                        </button>
                    </div>
                    <div className="p-4 text-sm text-slate-600 whitespace-pre-wrap leading-relaxed prose prose-sm prose-slate max-w-none">
                        {/* Simple markdown-like rendering if react-markdown isn't available */}
                        {content.content.split('\n').map((line, i) => {
                            if (line.startsWith('**')) return <p key={i} className="font-bold text-slate-800 mt-2 mb-1">{line.replace(/\*\*/g, '')}</p>;
                            if (line.startsWith('- ')) return <div key={i} className="flex gap-2 ml-1 mb-1"><span className="text-slate-400">•</span><span>{line.replace('- ', '')}</span></div>;
                            return <p key={i} className="mb-1">{line}</p>;
                        })}
                    </div>
                </div>
            )}
        </div>
    );
};

export default HelpButton;
