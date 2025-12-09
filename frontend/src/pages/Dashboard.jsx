import React from 'react';
import { Shield, Users, Activity, AlertTriangle, ArrowUpRight, ArrowDownRight, Clock } from 'lucide-react';

const StatCard = ({ title, value, trend, trendUp, icon: StatIcon, colorClass }) => (
    <div className="card p-6 hover:shadow-card-hover transition-shadow">
        <div className="flex items-start justify-between">
            <div>
                <p className="text-slate-500 text-sm font-medium">{title}</p>
                <h3 className="text-3xl font-bold text-slate-900 mt-2">{value}</h3>
            </div>
            <div className={`p-3 rounded-lg ${colorClass} bg-opacity-10`}>
                <StatIcon size={24} className={colorClass.replace('bg-', 'text-')} />
            </div>
        </div>
        <div className="mt-4 flex items-center text-sm">
            <span className={`flex items-center font-medium ${trendUp ? 'text-green-600' : 'text-red-600'} `}>
                {trendUp ? <ArrowUpRight size={16} className="mr-1" /> : <ArrowDownRight size={16} className="mr-1" />}
                {trend}
            </span>
            <span className="text-slate-400 ml-2">vs last month</span>
        </div>
    </div>
);

const Dashboard = () => {
    const stats = [
        { title: 'Active Policies', value: '12', trend: '12%', trendUp: true, icon: Shield, colorClass: 'bg-brand-600 text-brand-600' },
        { title: 'Total Entitlements', value: '1,234', trend: '5%', trendUp: true, icon: Users, colorClass: 'bg-purple-600 text-purple-600' },
        { title: 'Requests (24h)', value: '45.2k', trend: '2%', trendUp: false, icon: Activity, colorClass: 'bg-emerald-600 text-emerald-600' },
        { title: 'Failed Checks', value: '23', trend: '0.5%', trendUp: true, icon: AlertTriangle, colorClass: 'bg-amber-500 text-amber-500' },
    ];

    const recentActivity = [
        { id: 1, action: 'Policy Updated', target: 'Data Access Policy', user: 'admin', time: '2 mins ago', status: 'Success' },
        { id: 2, action: 'New Entitlement', target: 'User: john.doe', user: 'manager', time: '15 mins ago', status: 'Success' },
        { id: 3, action: 'Policy Created', target: 'API Rate Limit', user: 'admin', time: '1 hour ago', status: 'Pending' },
        { id: 4, action: 'User Deleted', target: 'User: temp.user', user: 'admin', time: '3 hours ago', status: 'Success' },
        { id: 5, action: 'Provider Added', target: 'Loan Service', user: 'system', time: '5 hours ago', status: 'Failed' },
    ];

    return (
        <div className="space-y-8">
            <div>
                <h2 className="text-2xl font-bold text-slate-900">System Overview</h2>
                <p className="text-slate-500 mt-1">Welcome back, here's what's happening today.</p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {stats.map((stat, index) => (
                    <StatCard key={index} {...stat} />
                ))}
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                {/* Activity Log */}
                <div className="lg:col-span-2 card">
                    <div className="p-6 border-b border-slate-200 flex justify-between items-center">
                        <h3 className="text-lg font-bold text-slate-900">Recent Activity</h3>
                        <button className="text-sm text-brand-600 font-medium hover:text-brand-700">View All</button>
                    </div>
                    <div className="overflow-x-auto">
                        <table className="w-full text-left">
                            <thead className="bg-slate-50 text-xs uppercase text-slate-500 font-semibold">
                                <tr>
                                    <th className="px-6 py-4">Action</th>
                                    <th className="px-6 py-4">Target</th>
                                    <th className="px-6 py-4">User</th>
                                    <th className="px-6 py-4">Status</th>
                                    <th className="px-6 py-4 text-right">Time</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-100">
                                {recentActivity.map((activity) => (
                                    <tr key={activity.id} className="hover:bg-slate-50 transition-colors">
                                        <td className="px-6 py-4 font-medium text-slate-900">{activity.action}</td>
                                        <td className="px-6 py-4 text-slate-600">{activity.target}</td>
                                        <td className="px-6 py-4 text-slate-600">{activity.user}</td>
                                        <td className="px-6 py-4">
                                            <span className={`inline - flex items - center px - 2.5 py - 0.5 rounded - full text - xs font - medium ${activity.status === 'Success' ? 'bg-green-100 text-green-800' :
                                                activity.status === 'Pending' ? 'bg-blue-100 text-blue-800' :
                                                    'bg-red-100 text-red-800'
                                                } `}>
                                                {activity.status}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4 text-right text-slate-500 text-sm flex items-center justify-end gap-1">
                                            <Clock size={14} />
                                            {activity.time}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>

                {/* System Health */}
                <div className="card p-6">
                    <h3 className="text-lg font-bold text-slate-900 mb-6">System Health</h3>
                    <div className="space-y-6">
                        <div className="p-4 bg-green-50 border border-green-100 rounded-lg flex items-start gap-3">
                            <div className="p-3 bg-indigo-50 rounded-lg text-indigo-600">
                                <Activity size={24} />
                            </div>
                            <div>
                                <p className="text-green-900 font-bold">All Systems Operational</p>
                                <p className="text-sm text-green-700 mt-1">Policy Engine services are running normally with 99.9% uptime.</p>
                            </div>
                        </div>

                        <div>
                            <div className="flex justify-between mb-2 text-sm font-medium">
                                <span className="text-slate-600">CPU Usage</span>
                                <span className="text-slate-900">12%</span>
                            </div>
                            <div className="w-full bg-slate-100 rounded-full h-2">
                                <div className="bg-brand-500 h-2 rounded-full transition-all duration-500" style={{ width: '12%' }}></div>
                            </div>
                        </div>

                        <div>
                            <div className="flex justify-between mb-2 text-sm font-medium">
                                <span className="text-slate-600">Memory Usage</span>
                                <span className="text-slate-900">45%</span>
                            </div>
                            <div className="w-full bg-slate-100 rounded-full h-2">
                                <div className="bg-purple-500 h-2 rounded-full transition-all duration-500" style={{ width: '45%' }}></div>
                            </div>
                        </div>

                        <div>
                            <div className="flex justify-between mb-2 text-sm font-medium">
                                <span className="text-slate-600">Storage</span>
                                <span className="text-slate-900">28%</span>
                            </div>
                            <div className="w-full bg-slate-100 rounded-full h-2">
                                <div className="bg-amber-500 h-2 rounded-full transition-all duration-500" style={{ width: '28%' }}></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Dashboard;
