import React, { useState, useEffect } from 'react';
import { Shield, Users, Activity, Package, Server } from 'lucide-react';
import { policyService, resourceTypeService, policyBundleService } from '../services/api';

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
        bundles: 0
    });
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchStats = async () => {
            try {
                const [policiesRes, resourceTypesRes, bundlesRes, clientStatsRes] = await Promise.all([
                    policyService.getAll({ size: 1 }),
                    resourceTypeService.getAll({ size: 1 }),
                    policyBundleService.getAll({ size: 1 }),
                    fetch('/api/v1/stats/clients').then(r => r.json())
                ]);

                setStats({
                    policies: policiesRes.data.totalElements || 0,
                    resourceTypes: resourceTypesRes.data.totalElements || 0,
                    bundles: bundlesRes.data.totalElements || 0,
                    activeClients: clientStatsRes.activeConnections || 0
                });
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

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                {statCards.map((stat, index) => (
                    <StatCard key={index} {...stat} />
                ))}
            </div>

            <div className="grid grid-cols-1 gap-8">
                <div className="card p-12 flex flex-col items-center justify-center text-center space-y-4 bg-white border border-slate-200 shadow-sm">
                    <div className="p-4 bg-blue-50 rounded-full">
                        <Activity size={32} className="text-blue-500" />
                    </div>
                    <div>
                        <h3 className="text-lg font-semibold text-slate-700">Client Connectivity</h3>
                        <p className="text-slate-500 max-w-md mx-auto mt-2 mb-4">
                            Real-time connection status of policy enforcement points.
                        </p>
                        <div className="text-4xl font-bold text-slate-900">
                            {loading ? '-' : stats.activeClients}
                        </div>
                        <p className="text-sm text-slate-400 mt-1">Active WebSocket Sessions</p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Dashboard;
