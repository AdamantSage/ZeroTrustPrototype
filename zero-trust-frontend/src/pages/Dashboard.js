// src/pages/Dashboard.js
import React, { useEffect, useState } from 'react';
import Sidebar from '../components/common/Sidebar';
import Header from '../components/common/Header';
import { getDevices, quarantineDevice } from '../services/deviceService';
import analyticsService from '../services/analyticsService';
import EnhancedPanel from '../components/EnhancedComponent/EnhancedPanel';
import {
  DeviceRiskPanel,
  TimelineChart,
  RecentChangesPanel
} from '../components/Analytics';

export default function Dashboard() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedDevice, setSelectedDevice] = useState('');
  
  // Analytics data for selected device
  const [deviceRisk, setDeviceRisk] = useState(null);
  const [timeline, setTimeline] = useState([]);
  const [recentChanges, setRecentChanges] = useState(null);
  const [analyticsLoading, setAnalyticsLoading] = useState(false);

  const toggleSidebar = () => setSidebarOpen(prev => !prev);

  useEffect(() => {
    fetchDevices();
  }, []);

  useEffect(() => {
    if (selectedDevice) {
      fetchDeviceAnalytics(selectedDevice);
    } else {
      // Clear analytics data when no device selected
      setDeviceRisk(null);
      setTimeline([]);
      setRecentChanges(null);
    }
  }, [selectedDevice]);

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

  const fetchDeviceAnalytics = async (deviceId) => {
    setAnalyticsLoading(true);
    try {
      const [risk, timelineData, changes] = await Promise.all([
        analyticsService.getDeviceRiskAssessment(deviceId),
        analyticsService.getTrustScoreTimeline(deviceId, 7),
        analyticsService.getRecentChanges(deviceId, 24)
      ]);

      setDeviceRisk(risk);
      setTimeline(timelineData);
      setRecentChanges(changes);
    } catch (err) {
      console.error('Failed to load device analytics:', err);
    } finally {
      setAnalyticsLoading(false);
    }
  };

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
      // Refresh analytics if this device is selected
      if (selectedDevice === deviceId) {
        await fetchDeviceAnalytics(deviceId);
      }
    } catch (err) {
      console.error('Quarantine failed', err);
      alert('Failed to quarantine device');
    }
  };

  const handleRefresh = async () => {
    await fetchDevices();
    if (selectedDevice) {
      await fetchDeviceAnalytics(selectedDevice);
    }
  };

  const handleResetTrustScore = async (deviceId, baselineScore) => {
    try {
      await analyticsService.resetDeviceTrustScore(deviceId, baselineScore);
      await fetchDeviceAnalytics(deviceId);
      await fetchDevices(); // Refresh device list to update trust scores
    } catch (err) {
      console.error('Failed to reset trust score:', err);
      alert('Failed to reset trust score.');
    }
  };

  const handleDeviceSelect = (deviceId) => {
    setSelectedDevice(deviceId);
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
          {/* Dashboard Header with Refresh */}
          <div className="mb-6 flex flex-col lg:flex-row lg:items-center lg:justify-between">
            <div>
              <h1 className="text-3xl font-bold mb-2">📊 Device Dashboard</h1>
              <p className="text-gray-600">Real-time device monitoring and analytics</p>
            </div>
            <div className="mt-4 lg:mt-0 flex items-center space-x-4">
              {/* Device selector for analytics */}
              <select
                value={selectedDevice}
                onChange={(e) => handleDeviceSelect(e.target.value)}
                className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">Select device for analytics</option>
                {devices.map(device => 
                  <option key={device.deviceId} value={device.deviceId}>
                    {device.deviceId} ({device.deviceType || 'Unknown'})
                  </option>
                )}
              </select>
              {/* Refresh button */}
              <button
                onClick={handleRefresh}
                disabled={loading}
                className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50"
              >
                {loading ? '⏳' : '🔄 Refresh'}
              </button>
            </div>
          </div>

          {/* EnhancedPanel - Device Overview */}
          <div className="mb-6">
            <EnhancedPanel
              initialDevices={devices}
              loading={loading}
              onQuarantine={handleQuarantine}
            />
          </div>

          {/* Device Analytics Section (shown when device is selected) */}
          {selectedDevice && (
            <div className="space-y-6">
              <div className="border-t pt-6">
                <h2 className="text-2xl font-bold mb-4">
                  📱 Device Analytics: {selectedDevice}
                </h2>
              </div>

              {analyticsLoading ? (
                <div className="text-center py-12">
                  <div className="text-lg text-gray-600">📡 Loading analytics data...</div>
                </div>
              ) : (
                <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
                  {/* Left column */}
                  <div className="space-y-6">
                    {/* Risk Assessment Panel */}
                    {deviceRisk && (
                      <DeviceRiskPanel 
                        data={deviceRisk} 
                        onResetTrustScore={handleResetTrustScore}
                      />
                    )}
                    
                    {/* Recent Changes Panel */}
                    {recentChanges && (
                      <RecentChangesPanel data={recentChanges} />
                    )}
                  </div>

                  {/* Right column */}
                  <div className="space-y-6">
                    {/* Trust Score Timeline (replacing System Health) */}
                    {timeline.length > 0 && (
                      <TimelineChart 
                        data={timeline} 
                        deviceId={selectedDevice}
                      />
                    )}
                  </div>
                </div>
              )}
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