import React, { useState, useEffect } from 'react';
import { Shield, Users, Activity, Package, Server } from 'lucide-react';
import { policyService, resourceTypeService, policyBundleService, serviceRegistryService } from '../services/api';

const StatCard = ({ title, value, icon: Icon, colorClass }) => (
    <div className="card p-6 hover:shadow-card-hover transition-shadow">
        <div className="flex items-start justify-between">
            <div>
                <p className="text-slate-500 text-sm font-medium">{title}</p>
                <h3 className="text-3xl font-bold text-slate-900 mt-2">{value}</h3>
            </div>
            <div className={`p-3 rounded-lg ${colorClass} bg-opacity-10`}>
                <Icon size={24} className={colorClass.replace('bg-', 'text-')} />
            </div>
        </div>
    </div>
);

const Dashboard = () => {
    const [stats, setStats] = useState({
        policies: 0,
        resourceTypes: 0,
        bundles: 0,
        services: 0,
        activeClients: 0
    });
    const [services, setServices] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchStats = async () => {
            try {
                const [policiesRes, resourceTypesRes, bundlesRes, servicesRes, clientStatsRes] = await Promise.all([
                    policyService.getAll({ size: 1 }),
                    resourceTypeService.getAll({ size: 1 }),
                    policyBundleService.getAll({ size: 1 }), // Assuming getAll supports paging for count
                    serviceRegistryService.getAll(),
                    fetch('/api/v1/stats/clients').then(r => r.json())
                ]);

                // policyBundleService.getAll returns page object
                setStats({
                    policies: policiesRes.data.totalElements || 0,
                    resourceTypes: resourceTypesRes.data.totalElements || 0,
                    bundles: bundlesRes.data.totalElements || 0,
                    services: servicesRes.data.length || 0,
                    activeClients: clientStatsRes.activeConnections || 0
                });
                setServices(servicesRes.data);
            } catch (error) {
                console.error('Error fetching dashboard stats:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchStats();
    }, []);

    const statCards = [
        {
            title: 'Registered Services',
            value: loading ? '-' : stats.services,
            icon: Users, // Using Users icon as a proxy for Services for now, or maybe Server
            colorClass: 'bg-indigo-600 text-indigo-600'
        },
        {
            title: 'Active Policies',
            value: loading ? '-' : stats.policies,
            icon: Shield,
            colorClass: 'bg-brand-600 text-brand-600'
        },
        {
            title: 'Business Contexts',
            value: loading ? '-' : stats.resourceTypes,
            icon: Server,
            colorClass: 'bg-purple-600 text-purple-600'
        },
        {
            title: 'Policy Bundles',
            value: loading ? '-' : stats.bundles,
            icon: Package,
            colorClass: 'bg-emerald-600 text-emerald-600'
        }
    ];

    return (
        <div className="space-y-8">
            <div>
                <h2 className="text-2xl font-bold text-slate-900">System Overview</h2>
                <p className="text-slate-500 mt-1">Real-time metrics from your Policy Engine.</p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                {statCards.map((stat, index) => (
                    <StatCard key={index} {...stat} />
                ))}
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                {/* Services Table */}
                <div className="lg:col-span-2 bg-white border border-slate-200 shadow-sm rounded-lg overflow-hidden">
                    <div className="px-6 py-4 border-b border-slate-100">
                        <h3 className="font-bold text-slate-800">Connected Services</h3>
                    </div>
                    <div className="overflow-x-auto">
                        <table className="w-full text-sm text-left">
                            <thead className="bg-slate-50 text-slate-500 font-medium">
                                <tr>
                                    <th className="px-6 py-3">Name</th>
                                    <th className="px-6 py-3">Version</th>
                                    <th className="px-6 py-3">Bundles</th>
                                    <th className="px-6 py-3">Status</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-100">
                                {services.slice(0, 5).map(service => (
                                    <tr key={service.id} className="hover:bg-slate-50">
                                        <td className="px-6 py-3 font-medium text-slate-900">{service.name}</td>
                                        <td className="px-6 py-3 text-slate-500">{service.version}</td>
                                        <td className="px-6 py-3 text-slate-500">{service.bundleCount || 0}</td>
                                        <td className="px-6 py-3">
                                            <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${service.status === 'HEALTHY' ? 'bg-green-100 text-green-800' :
                                                service.status === 'OFFLINE' ? 'bg-red-100 text-red-800' :
                                                    'bg-gray-100 text-gray-800'
                                                }`}>
                                                {service.status}
                                            </span>
                                        </td>
                                    </tr>
                                ))}
                                {services.length === 0 && (
                                    <tr>
                                        <td colSpan="4" className="px-6 py-4 text-center text-slate-500 italic">No services registered</td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>

                {/* Client Connectivity Card */}
                <div className="card p-6 flex flex-col items-center justify-center text-center space-y-4 bg-white border border-slate-200 shadow-sm">
                    <div className="p-4 bg-blue-50 rounded-full">
                        <Activity size={32} className="text-blue-500" />
                    </div>
                    <div>
                        <h3 className="text-lg font-semibold text-slate-700">Client Connectivity</h3>
                        <p className="text-slate-500 text-sm mt-1 mb-2">
                            Active WebSocket Sessions
                        </p>
                        <div className="text-4xl font-bold text-slate-900">
                            {loading ? '-' : stats.activeClients}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Dashboard;
