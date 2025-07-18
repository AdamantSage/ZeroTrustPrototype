// src/pages/Dashboard.js
import React, { useEffect, useState } from 'react';
import Sidebar from '../components/common/Sidebar';
import Header from '../components/common/Header';
import {
  getDevices,
  quarantineDevice,
  getTrustScore
} from '../services/deviceService';

export default function Dashboard() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);

  const toggleSidebar = () => setSidebarOpen((prev) => !prev);

  useEffect(() => {
    const fetchDevicesWithScores = async () => {
      setLoading(true);
      try {
        const list = await getDevices();
        const withScores = await Promise.all(
          list.map(async (d) => ({
            ...d,
            trustScore: await getTrustScore(d.deviceId),
          }))
        );
        setDevices(withScores);
      } catch (err) {
        console.error('Failed to load devices', err);
      } finally {
        setLoading(false);
      }
    };

    fetchDevicesWithScores();
  }, []);

  const handleQuarantine = async (deviceId) => {
    const reason = prompt('Quarantine reason?');
    if (!reason) return;
    await quarantineDevice(deviceId, reason);
    setDevices((prev) =>
      prev.map((d) =>
        d.deviceId === deviceId ? { ...d, quarantined: true, trusted: false } : d
      )
    );
  };

  return (
    <div className="flex h-screen bg-gray-100 text-gray-800">
      <Sidebar isOpen={sidebarOpen} onToggle={toggleSidebar} />

      <div className={`flex flex-col flex-1 transition-all duration-300 ${sidebarOpen ? 'lg:ml-0' : 'lg:ml-0'}`}>
        <Header onMenuToggle={toggleSidebar} />

        <main className="flex-1 p-6 overflow-y-auto w-full">
          <h1 className="text-3xl font-bold mb-6">📊 Device Dashboard</h1>

          {loading ? (
            <div className="text-center text-lg">🔄 Loading devices…</div>
          ) : (
            <div className="w-full overflow-x-auto rounded-lg shadow bg-white">
              <table className="w-full text-sm table-auto">
                <thead className="bg-gray-200 text-gray-700 text-left">
                  <tr>
                    <th className="px-6 py-3">Device ID</th>
                    <th className="px-6 py-3">Trusted</th>
                    <th className="px-6 py-3">Trust Score</th>
                    <th className="px-6 py-3">Quarantined</th>
                    <th className="px-6 py-3">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {devices.map((d) => (
                    <tr key={d.deviceId} className="hover:bg-gray-50">
                      <td className="px-6 py-4 font-medium">{d.deviceId}</td>
                      <td className="px-6 py-4">
                        {d.trusted ? (
                          <span className="text-green-600 font-bold">✅ Yes</span>
                        ) : (
                          <span className="text-red-500 font-bold">❌ No</span>
                        )}
                      </td>
                      <td className="px-6 py-4">{d.trustScore.toFixed(1)}</td>
                      <td className="px-6 py-4">
                        {d.quarantined ? 'Yes' : 'No'}
                      </td>
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

      {/* Mobile overlay */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-black bg-opacity-50 z-20 lg:hidden"
          onClick={toggleSidebar}
        />
      )}
    </div>
  );
}