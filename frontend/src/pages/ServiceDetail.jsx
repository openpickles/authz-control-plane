import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { policyService, policyBundleService, policyBindingService } from '../services/api';
import SlideOver from '../components/SlideOver';
import MultiSelect from '../components/MultiSelect'; // Reusing existing component if suitable? Or basic select.
// Assuming PolicyEditor exists or we use a simple textarea for Custom override.

const ServiceDetail = () => {
    const { name } = useParams();
    const serviceName = name;

    const [policies, setPolicies] = useState([]);
    const [bundles, setBundles] = useState([]);
    const [bindings, setBindings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isSlideOpen, setIsSlideOpen] = useState(false);
    const [selectedPolicy, setSelectedPolicy] = useState(null);
    const [customContent, setCustomContent] = useState('');
    const [saving, setSaving] = useState(false);

    useEffect(() => {
        fetchData();
    }, [serviceName]);

    const fetchData = async () => {
        setLoading(true);
        try {
            const [pRes, bRes, bindRes] = await Promise.all([
                policyService.getAll({ serviceOwner: serviceName, size: 100 }),
                policyBundleService.getAll({ service: serviceName, size: 100 }),
                policyBindingService.getAll({ service: serviceName, size: 100 })
            ]);
            setPolicies(pRes.data.content);
            setBundles(bRes.data.content);
            setBindings(bindRes.data.content);
        } catch (error) {
            console.error("Error fetching service details", error);
        } finally {
            setLoading(false);
        }
    };

    const handleCustomize = (policy) => {
        setSelectedPolicy(policy);
        setCustomContent(policy.content); // Pre-fill with existing (Product or Custom) content
        setIsSlideOpen(true);
    };

    const handleSaveCustom = async () => {
        if (!selectedPolicy) return;
        setSaving(true);
        try {
            const customPolicy = {
                ...selectedPolicy,
                id: undefined, // Create new entry if it doesn't exist? Wait, createCustom endpoint handles it?
                // Actually AdminController createCustom creates a new entry with origin=CUSTOM.
                // We should pass name, serviceOwner, origin=CUSTOM, content.
                name: selectedPolicy.name,
                serviceOwner: serviceName,
                origin: 'CUSTOM', // AdminController enforces this anyway
                content: customContent,
                filename: selectedPolicy.filename,
                version: selectedPolicy.version
            };

            await policyService.createCustom(customPolicy);
            setIsSlideOpen(false);
            fetchData(); // Refresh to show updated state
        } catch (e) {
            console.error("Failed to save custom policy", e);
            alert("Failed to save: " + e.message);
        } finally {
            setSaving(false);
        }
    };

    if (loading) return <div className="p-6">Loading...</div>;

    return (
        <div className="p-6">
            <div className="flex items-center justify-between mb-6">
                <div>
                    <div className="flex items-center gap-3">
                        <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Service: {serviceName}</h1>
                        {/* Status/Mode Badges would go here if we had service object details loaded. 
                            Currently we only load policies/bundles. 
                            Ideally, we should fetch service details separately or pass them.
                            For now, skipping explicit service metadata display until new endpoint used.
                        */}
                    </div>
                    <Link to="/services" className="text-sm text-indigo-600 hover:text-indigo-500">← Back to Services</Link>
                </div>
                <button onClick={fetchData} className="px-4 py-2 bg-gray-200 dark:bg-gray-700 rounded hover:bg-gray-300 dark:hover:bg-gray-600">Refresh</button>
            </div>

            {/* Service Metadata Section (New) */}
            <div className="bg-white dark:bg-gray-800 shadow rounded-lg p-6 mb-8 border border-gray-200 dark:border-gray-700">
                <h2 className="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">Service Information</h2>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div>
                        <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Service Name</p>
                        <p className="text-base text-gray-900 dark:text-gray-100">{serviceName}</p>
                    </div>
                    <div>
                        {/* We need to fetch service details to show mode/key. 
                             Currently ServiceDetail only fetches sub-resources.
                             TODO: Fetch full service details. 
                         */}
                        <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Configuration</p>
                        <p className="text-sm text-gray-500 italic">Metadata loading not implemented in this view yet.</p>
                    </div>
                </div>
            </div>


            {/* Bundles Section */}
            <div className="mb-8">
                <h2 className="text-xl font-semibold mb-4 text-gray-800 dark:text-gray-200">Bundles</h2>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    {bundles.map(bundle => (
                        <div key={bundle.id} className="bg-white dark:bg-gray-800 p-4 rounded shadow border border-gray-200 dark:border-gray-700">
                            <h3 className="font-bold text-lg">{bundle.name}</h3>
                            <p className="text-sm text-gray-500">Origin: {bundle.origin}</p>
                            <div className="mt-2 text-xs">
                                <span className="font-semibold">Contexts: </span> N/A (Bundle definition logic)
                            </div>
                            <div className="mt-4">
                                <a href={`/api/v1/bundles/${bundle.name}/download?service=${serviceName}`} target="_blank" className="text-blue-600 hover:underline text-sm">Download Effective Bundle</a>
                            </div>
                        </div>
                    ))}
                    {bundles.length === 0 && <p className="text-gray-500 italic">No bundles found.</p>}
                </div>
            </div>

            {/* Policies Section */}
            <div>
                <h2 className="text-xl font-semibold mb-4 text-gray-800 dark:text-gray-200">Policies</h2>
                <div className="bg-white dark:bg-gray-800 shadow rounded-lg overflow-hidden">
                    <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                        <thead className="bg-gray-50 dark:bg-gray-900">
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Policy Name</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Origin</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
                                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">Actions</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
                            {policies.map(policy => (
                                <tr key={policy.id} className="hover:bg-gray-50 dark:hover:bg-gray-700">
                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900 dark:text-gray-100">{policy.name}</td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                                        <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${policy.origin === 'PRODUCT' ? 'bg-blue-100 text-blue-800' : 'bg-green-100 text-green-800'
                                            }`}>
                                            {policy.origin}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{policy.status}</td>
                                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm">
                                        <button
                                            onClick={() => handleCustomize(policy)}
                                            className="text-indigo-600 hover:text-indigo-900 dark:text-indigo-400 font-medium"
                                        >
                                            {policy.origin === 'PRODUCT' ? 'Customize / Override' : 'Edit Customization'}
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>

            <SlideOver
                isOpen={isSlideOpen}
                onClose={() => setIsSlideOpen(false)}
                title={selectedPolicy ? `Customize: ${selectedPolicy.name}` : 'Customize Policy'}
            >
                <div className="p-4 space-y-4">
                    <p className="text-sm text-gray-600 dark:text-gray-400">
                        Edit the Rego policy content below. This will create a CUSTOM override for this service.
                    </p>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">Content</label>
                        <textarea
                            rows={20}
                            className="mt-1 block w-full rounded-md border-gray-300 dark:border-gray-600 dark:bg-gray-800 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm font-mono"
                            value={customContent}
                            onChange={(e) => setCustomContent(e.target.value)}
                        />
                    </div>
                    <div className="pt-4 flex justify-end space-x-3">
                        <button onClick={() => setIsSlideOpen(false)} className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 dark:text-gray-200 dark:hover:bg-gray-700">Cancel</button>
                        <button
                            onClick={handleSaveCustom}
                            disabled={saving}
                            className="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50"
                        >
                            {saving ? 'Saving...' : 'Save Override'}
                        </button>
                    </div>
                </div>
            </SlideOver>
        </div>
    );
};

export default ServiceDetail;
