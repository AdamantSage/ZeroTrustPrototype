// src/pages/Dashboard.js

import React, { useEffect, useState } from 'react';
import Sidebar from '../components/common/Sidebar';
import Header from '../components/common/Header';
import { getDevices, quarantineDevice } from '../services/deviceService';

export default function Dashboard() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);

  const toggleSidebar = () => setSidebarOpen(prev => !prev);

  useEffect(() => {
    const fetchDevices = async () => {
      setLoading(true);
      try {
        const list = await getDevices();
        setDevices(list);
      } catch (err) {
        console.error('Failed to load devices', err);
      } finally {
        setLoading(false);
      }
    };
    fetchDevices();
  }, []);

  const handleQuarantine = async (deviceId) => {
    const reason = prompt('Quarantine reason?');
    if (!reason) return;
    await quarantineDevice(deviceId, reason);
    setDevices(devs =>
      devs.map(d =>
        d.deviceId === deviceId
          ? { ...d, quarantined: true, trusted: false }
          : d
      )
    );
  };

  return (
    <div className="flex h-screen w-screen overflow-hidden bg-gray-100 text-gray-800">
      {/* Sidebar */}
      <Sidebar isOpen={sidebarOpen} onToggle={toggleSidebar} />

      {/* Main content wrapper */}
      <div className="flex flex-col flex-1 w-full transition-all duration-300 lg:ml-64">

        {/* Header */}
        <Header onMenuToggle={toggleSidebar} />

        {/* Main section */}
        <main className="flex-1 w-full max-w-full p-6 overflow-y-auto">
          <h1 className="text-3xl font-bold mb-6">📊 Device Dashboard</h1>

          {loading ? (
            <div className="text-center text-lg">🔄 Loading devices…</div>
          ) : (
            <div className="w-full overflow-x-auto rounded-lg shadow bg-white">
              <table className="w-full text-sm table-auto">
                <thead className="bg-gray-200 text-gray-700 text-left">
                  <tr>
                    <th className="px-6 py-3">Device ID</th>
                    <th className="px-6 py-3">Last Seen</th>
                    <th className="px-6 py-3">Location</th>
                    <th className="px-6 py-3">IP Address</th>
                    <th className="px-6 py-3">Trusted</th>
                    <th className="px-6 py-3">Trust Score</th>
                    <th className="px-6 py-3">Quarantined</th>
                    <th className="px-6 py-3">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {devices.map(d => (
                    <tr key={d.deviceId} className="hover:bg-gray-50">
                      <td className="px-6 py-4 font-medium">{d.deviceId}</td>
                      <td className="px-6 py-4">
                        {d.lastSeen ? new Date(d.lastSeen).toLocaleString() : '—'}
                      </td>
                      <td className="px-6 py-4">{d.location}</td>
                      <td className="px-6 py-4">{d.ipAddress}</td>
                      <td className="px-6 py-4">
                        {d.trusted
                          ? <span className="text-green-600 font-bold">✅</span>
                          : <span className="text-red-500 font-bold">❌</span>}
                      </td>
                      <td className="px-6 py-4">{d.trustScore.toFixed(1)}</td>
                      <td className="px-6 py-4">{d.quarantined ? 'Yes' : 'No'}</td>
                      <td className="px-6 py-4">
                        {!d.quarantined && (
                          <button
                            onClick={() => handleQuarantine(d.deviceId)}
                            className="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded-md text-xs"
                          >
                            Quarantine
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </main>
      </div>

      {/* Mobile backdrop */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-black bg-opacity-50 z-20 lg:hidden"
          onClick={toggleSidebar}
        />
      )}
    </div>
  );
}
