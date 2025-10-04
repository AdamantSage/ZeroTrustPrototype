// src/App.js
import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import './App.css';
import Dashboard from './pages/Dashboard';
import AuditPage from './pages/AuditPage';
import DeviceRegistryPage from './pages/DeviceRegistryPage';

function App() {
  return (
    <div className="App">
      <Router>
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/audit" element={<AuditPage />} />
          <Route path="/audit/location" element={<AuditPage />} />
          <Route path="/audit/quarantine" element={<AuditPage />} />
          <Route path="/audit/firmware" element={<AuditPage />} />
          <Route path="/devices" element={<DeviceRegistryPage />} />

        </Routes>
      </Router>
    </div>
  );
}

export default App;