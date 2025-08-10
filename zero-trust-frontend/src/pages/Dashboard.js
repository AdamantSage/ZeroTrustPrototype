// src/pages/Dashboard.js
import React, { useEffect, useState } from 'react';
import Sidebar from '../components/common/Sidebar';
import Header from '../components/common/Header';
import { getDevices, quarantineDevice } from '../services/deviceService';
import EnhancedPanel from '../components/EnhancedPanel';

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

  const handleQuarantine = async (deviceId, reason) => {
    if (!reason) return;
    try {
      await quarantineDevice(deviceId, reason);
      setDevices(devs =>
        devs.map(d =>
          d.deviceId === deviceId
            ? { ...d, quarantined: true, trusted: false, status: 'QUARANTINED' }
            : d
        )
      );
    } catch (err) {
      console.error('Quarantine failed', err);
      alert('Failed to quarantine device');
    }
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

          {/* EnhancedPanel will render the new layout but keep visual theme */}
          <EnhancedPanel
            initialDevices={devices}
            loading={loading}
            onQuarantine={handleQuarantine}
          />
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
