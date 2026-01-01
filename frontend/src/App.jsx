import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import PolicyEditor from './pages/PolicyEditor';
import Entitlements from './pages/Entitlements';

import UserManagement from './pages/UserManagement';

import ResourceTypes from './pages/ResourceTypes';
import PolicyBindings from './pages/PolicyBindings';
import PolicyBundles from './pages/PolicyBundles';
import AuditLog from './pages/AuditLog';

// Placeholder for missing pages
const Settings = () => <div className="text-2xl font-bold text-white">Settings Page (Coming Soon)</div>;

function App() {
  return (
    <Router>
      <Layout>
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/policies" element={<PolicyEditor />} />
          <Route path="/entitlements" element={<Entitlements />} />
          <Route path="/resource-types" element={<ResourceTypes />} />
          <Route path="/users" element={<UserManagement />} />
          <Route path="/policy-bindings" element={<PolicyBindings />} />
          <Route path="/policy-bundles" element={<PolicyBundles />} />
          <Route path="/audit" element={<AuditLog />} />
          <Route path="/settings" element={<Settings />} />
        </Routes>
      </Layout>
    </Router>
  );
}

export default App;
