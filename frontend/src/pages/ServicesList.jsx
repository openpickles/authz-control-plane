import React, { useState, useEffect } from 'react';
import { serviceRegistryService } from '../services/api';
import { Link } from 'react-router-dom';
import SlideOver from '../components/SlideOver';
import { Plus, Server } from 'lucide-react';

const ServicesList = () => {
    const [services, setServices] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showRegister, setShowRegister] = useState(false);
    const [formData, setFormData] = useState({ name: '', description: '', publicKey: '' });
    const [registering, setRegistering] = useState(false);

    useEffect(() => {
        loadServices();
    }, []);

    const loadServices = () => {
        serviceRegistryService.getAll()
            .then(response => {
                setServices(response.data);
                setLoading(false);
            })
            .catch(err => {
                setError(err.message);
                setLoading(false);
            });
    };

    const handleRegister = async (e) => {
        e.preventDefault();
        setRegistering(true);
        try {
            await serviceRegistryService.create(formData);
            setShowRegister(false);
            setFormData({ name: '', description: '', publicKey: '' });
            loadServices();
        } catch (err) {
            console.error(err);
            alert("Failed to register service: " + (err.response?.data || err.message));
        } finally {
            setRegistering(false);
        }
    };

    if (loading) return <div className="p-6 text-gray-500">Loading services...</div>;
    if (error) return <div className="p-6 text-red-500">Error: {error}</div>;

    return (
        <div className="p-6">
            <div className="flex justify-between items-center mb-6">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Domain Services</h1>
                    <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">Manage registered services and policies.</p>
                </div>
                <button
                    onClick={() => setShowRegister(true)}
                    className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-md transition-colors"
                >
                    <Plus size={16} />
                    Register Service
                </button>
            </div>

            <div className="bg-white dark:bg-gray-800 shadow rounded-lg overflow-hidden">
                <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                    <thead className="bg-gray-50 dark:bg-gray-900">
                        <tr>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Service Name</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Mode</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Version</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Bundles</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">C. Policies</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Status</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Description</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Last Sync</th>
                            <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Actions</th>
                        </tr>
                    </thead>
                    <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                        {services.map(service => (
                            <tr key={service.id} className="hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors">
                                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900 dark:text-gray-100 flex items-center gap-2">
                                    <Server size={14} className="text-gray-400" />
                                    {service.name}
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                    <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${service.registrationMode === 'MANUAL' ? 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200' : 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200'}`}>
                                        {service.registrationMode}
                                    </span>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{service.version || '-'}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400 font-semibold">{service.bundleCount || 0}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{service.customPolicyCount || 0}</td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${service.status === 'HEALTHY' ? 'bg-green-100 text-green-800' :
                                        service.status === 'OFFLINE' ? 'bg-red-100 text-red-800' :
                                            service.status === 'MODIFIED' ? 'bg-blue-100 text-blue-800' :
                                                service.status === 'REGISTERED' ? 'bg-yellow-100 text-yellow-800' :
                                                    'bg-gray-100 text-gray-800'
                                        }`}>
                                        {service.status || 'UNKNOWN'}
                                    </span>
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400 max-w-xs truncate" title={service.description}>
                                    {service.description}
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                                    {service.lastSyncTime ? new Date(service.lastSyncTime).toLocaleString() : '-'}
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                                    <Link to={`/services/${service.name}`} className="text-indigo-600 hover:text-indigo-900 dark:text-indigo-400 dark:hover:text-indigo-300">View Details</Link>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
                {services.length === 0 && <div className="p-6 text-center text-gray-500">No services registered yet.</div>}
            </div>

            <SlideOver
                isOpen={showRegister}
                onClose={() => setShowRegister(false)}
                title="Register New Service"
            >
                <form onSubmit={handleRegister} className="space-y-6 p-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">Service Name</label>
                        <input
                            type="text"
                            required
                            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                            value={formData.name}
                            onChange={e => setFormData({ ...formData, name: e.target.value })}
                            placeholder="e.g. payment-service"
                        />
                        <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">Unique identifier for the service.</p>
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">Description</label>
                        <textarea
                            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                            rows={3}
                            value={formData.description}
                            onChange={e => setFormData({ ...formData, description: e.target.value })}
                            placeholder="Service purpose..."
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">Public Key (PEM)</label>
                        <textarea
                            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm font-mono text-xs dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                            rows={5}
                            value={formData.publicKey}
                            onChange={e => setFormData({ ...formData, publicKey: e.target.value })}
                            placeholder="-----BEGIN PUBLIC KEY-----..."
                        />
                        <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">Optional. Used for signature verification.</p>
                    </div>
                    <div className="pt-4 flex justify-end gap-3">
                        <button
                            type="button"
                            onClick={() => setShowRegister(false)}
                            className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 dark:text-gray-300 dark:hover:bg-gray-700"
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            disabled={registering}
                            className="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50"
                        >
                            {registering ? 'Registering...' : 'Register'}
                        </button>
                    </div>
                </form>
            </SlideOver>
        </div>
    );
};

export default ServicesList;
