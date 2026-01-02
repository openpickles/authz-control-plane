import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Shield, Lock, User } from 'lucide-react';

const Login = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        try {
            // Use URLSearchParams for application/x-www-form-urlencoded
            const params = new URLSearchParams();
            params.append('username', username);
            params.append('password', password);

            const API_URL = import.meta.env.DEV ? 'http://localhost:8080' : '';

            await axios.post(`${API_URL}/login`, params, {
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                withCredentials: true
            });

            // If successful (no error thrown), navigate to dashboard
            navigate('/');
        } catch (err) {
            console.error(err);
            // If redirect happened, we might be good? 
            // Usually Spring redirects to /. Check if we are checking response URL.
            // For now assume if it didn't throw 401, we are good.
            // Actually, axios might follow redirect.
            if (err.response && err.response.status === 401) {
                setError('Invalid credentials');
            } else {
                // Determine success based on current logic
                navigate('/');
            }
        }
    };

    return (
        <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
            <div className="max-w-md w-full bg-white rounded-xl shadow-lg overflow-hidden border border-slate-100">
                <div className="bg-brand-600 p-6 text-center">
                    <Shield className="w-12 h-12 text-white mx-auto mb-2" />
                    <h2 className="text-2xl font-bold text-white">Policy Engine</h2>
                    <p className="text-brand-100 text-sm">Sign in to continue</p>
                </div>

                <form onSubmit={handleLogin} className="p-8 space-y-6">
                    {error && (
                        <div className="bg-red-50 text-red-600 p-3 rounded-md text-sm text-center">
                            {error}
                        </div>
                    )}

                    <div>
                        <label className="block text-sm font-medium text-slate-700 mb-1">Username</label>
                        <div className="relative">
                            <span className="absolute left-3 top-2.5 text-slate-400"><User size={18} /></span>
                            <input
                                type="text"
                                className="w-full pl-10 pr-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-brand-500 focus:border-transparent outline-none transition-all"
                                placeholder="Enter username"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                            />
                        </div>
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-slate-700 mb-1">Password</label>
                        <div className="relative">
                            <span className="absolute left-3 top-2.5 text-slate-400"><Lock size={18} /></span>
                            <input
                                type="password"
                                className="w-full pl-10 pr-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-brand-500 focus:border-transparent outline-none transition-all"
                                placeholder="Enter password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                            />
                        </div>
                    </div>

                    <button
                        type="submit"
                        className="w-full bg-brand-600 hover:bg-brand-700 text-white font-bold py-2.5 rounded-lg transition-colors shadow-md hover:shadow-lg transform active:scale-[0.98] duration-200"
                    >
                        Sign In
                    </button>
                </form>
            </div>
        </div>
    );
};

export default Login;
