import React, { useState } from 'react';
import Editor from '@monaco-editor/react';
import { Play } from 'lucide-react';
import { evaluationService } from '../services/api';

const TestPanel = ({ policyContent, policyId }) => {
    const [inputJson, setInputJson] = useState('{\n    "user": "alice",\n    "action": "read"\n}');
    const [dataJson, setDataJson] = useState('{\n    "roles": {\n        "alice": ["admin"]\n    }\n}');
    const [result, setResult] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const handleRunTest = async () => {
        setLoading(true);
        setError(null);
        setResult(null);
        try {
            let inputParsed, dataParsed;
            try {
                inputParsed = JSON.parse(inputJson);
            } catch (e) {
                throw new Error("Invalid Input JSON: " + e.message);
            }
            try {
                dataParsed = JSON.parse(dataJson);
            } catch (e) {
                throw new Error("Invalid Data JSON: " + e.message);
            }

            const response = await evaluationService.test({
                policyContent,
                policyId,
                input: inputParsed,
                data: dataParsed
            });
            setResult(response.data);
        } catch (err) {
            console.error(err);
            setError(err.message || "Evaluation Failed");
            if (err.response?.data?.error) {
                setError(err.response.data.error);
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="flex flex-col h-full bg-slate-50 border-l border-slate-200 w-96 flex-shrink-0 animate-in slide-in-from-right duration-200">
            <div className="p-3 border-b border-slate-200 bg-white flex justify-between items-center">
                <h3 className="font-bold text-slate-700">Test Policy</h3>
                <button
                    onClick={handleRunTest}
                    disabled={loading}
                    className="btn-primary flex items-center gap-2 py-1 px-3 text-xs"
                >
                    <Play size={14} />
                    {loading ? 'Running...' : 'Run Test'}
                </button>
            </div>

            <div className="flex-1 overflow-y-auto p-4 space-y-4">
                {/* Input Editor */}
                <div>
                    <label className="block text-xs font-medium text-slate-500 mb-1">Input (JSON)</label>
                    <div className="h-40 border border-slate-300 rounded-md overflow-hidden">
                        <Editor
                            height="100%"
                            defaultLanguage="json"
                            value={inputJson}
                            onChange={(val) => setInputJson(val)}
                            options={{
                                minimap: { enabled: false },
                                lineNumbers: 'off',
                                fontSize: 12,
                                scrollBeyondLastLine: false,
                            }}
                        />
                    </div>
                </div>

                {/* Data Editor */}
                <div>
                    <label className="block text-xs font-medium text-slate-500 mb-1">Data / Context (JSON)</label>
                    <div className="h-40 border border-slate-300 rounded-md overflow-hidden">
                        <Editor
                            height="100%"
                            defaultLanguage="json"
                            value={dataJson}
                            onChange={(val) => setDataJson(val)}
                            options={{
                                minimap: { enabled: false },
                                lineNumbers: 'off',
                                fontSize: 12,
                                scrollBeyondLastLine: false,
                            }}
                        />
                    </div>
                </div>

                {/* Results */}
                <div>
                    <label className="block text-xs font-medium text-slate-500 mb-1">Result</label>
                    <div className={`min-h-[100px] p-3 rounded-md text-xs font-mono whitespace-pre-wrap border ${error ? 'bg-red-50 border-red-200 text-red-700' : 'bg-white border-slate-200 text-slate-700'
                        }`}>
                        {error ? error : result ? JSON.stringify(result, null, 2) : <span className="text-slate-400 italic">Run test to see results...</span>}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default TestPanel;
