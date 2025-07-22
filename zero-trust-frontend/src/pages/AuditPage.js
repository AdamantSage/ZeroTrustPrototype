import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import Sidebar from '../components/common/Sidebar';
import Header from '../components/common/Header';
import auditService from '../services/AuditService';

export default function AuditPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [activeTab, setActiveTab] = useState(searchParams.get('tab') || 'location');
  const [loading, setLoading] = useState(true);
  const [selectedDevice, setSelectedDevice] = useState('');
  const [devices, setDevices] = useState([]);
  const [error, setError] = useState(null);
  
  // Data states
  const [locationChanges, setLocationChanges] = useState([]);
  const [quarantineHistory, setQuarantineHistory] = useState([]);
  const [firmwareLogs, setFirmwareLogs] = useState([]);
  const [auditSummary, setAuditSummary] = useState(null);

  const toggleSidebar = () => setSidebarOpen(prev => !prev);

  useEffect(() => {
    const tabFromUrl = searchParams.get('tab');
    if (tabFromUrl && ['location', 'quarantine', 'firmware'].includes(tabFromUrl)) {
      setActiveTab(tabFromUrl);
    }
  }, [searchParams]);

  useEffect(() => {
    fetchInitialData();
  }, []);

  useEffect(() => {
    if (selectedDevice) {
      fetchDeviceSpecificData(selectedDevice);
    } else {
      fetchAllData();
    }
  }, [selectedDevice, activeTab]);

  const fetchInitialData = async () => {
    setLoading(true);
    setError(null);
    
    try {
      // Fetch devices list for filter dropdown
      const deviceList = await auditService.getDevices();
      setDevices(deviceList);
      
      // Fetch audit summary
      const summary = await auditService.getAuditSummary();
      setAuditSummary(summary);
      
      // Fetch all audit data initially
      await fetchAllData();
    } catch (err) {
      console.error('Failed to load initial data:', err);
      setError('Failed to load initial data. Please try refreshing the page.');
    } finally {
      setLoading(false);
    }
  };

  const fetchAllData = async () => {
    try {
      const [locationData, quarantineData, firmwareData] = await Promise.all([
        auditService.getLocationChanges(),
        auditService.getQuarantineHistory(),
        auditService.getFirmwareLogs()
      ]);

      setLocationChanges(locationData);
      setQuarantineHistory(quarantineData);
      setFirmwareLogs(firmwareData);
    } catch (err) {
      console.error('Failed to load audit data:', err);
      setError('Failed to load audit data.');
    }
  };

  const fetchDeviceSpecificData = async (deviceId) => {
    try {
      const [locationData, quarantineData, firmwareData] = await Promise.all([
        auditService.getLocationChanges(deviceId),
        auditService.getQuarantineHistory(deviceId),
        auditService.getFirmwareLogs(deviceId)
      ]);

      setLocationChanges(locationData);
      setQuarantineHistory(quarantineData);
      setFirmwareLogs(firmwareData);
    } catch (err) {
      console.error('Failed to load device-specific audit data:', err);
      setError('Failed to load device-specific audit data.');
    }
  };

  const handleExport = async (type) => {
    try {
      const blob = await auditService.exportToCsv(type, selectedDevice);
      const filename = `${type}-audit-${selectedDevice || 'all'}-${new Date().toISOString().split('T')[0]}.csv`;
      auditService.downloadCsv(blob, filename);
    } catch (err) {
      console.error(`Failed to export ${type} data:`, err);
      setError(`Failed to export ${type} data.`);
    }
  };

  const getStatusBadge = (status) => {
    const statusColors = {
      SUCCESS: 'bg-green-100 text-green-800',
      FAILED: 'bg-red-100 text-red-800',
      PENDING: 'bg-yellow-100 text-yellow-800',
      ALREADY_QUARANTINED: 'bg-blue-100 text-blue-800',
      RECREATED: 'bg-purple-100 text-purple-800'
    };
    
    return (
      <span className={`px-2 py-1 text-xs font-medium rounded-full ${statusColors[status] || 'bg-gray-100 text-gray-800'}`}>
        {status}
      </span>
    );
  };

  const tabs = [
    { id: 'location', name: 'Location/Network Changes', icon: '🌍' },
    { id: 'quarantine', name: 'Quarantine History', icon: '🚨' },
    { id: 'firmware', name: 'Firmware Management', icon: '💾' }
  ];

  const renderSummaryCards = () => {
    if (!auditSummary) return null;
    
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <span className="text-2xl">🚨</span>
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-500">Quarantine Actions</p>
              <p className="text-2xl font-semibold text-gray-900">{auditSummary.totalQuarantineActions}</p>
            </div>
          </div>
        </div>
        
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <span className="text-2xl">🌍</span>
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-500">Location Changes</p>
              <p className="text-2xl font-semibold text-gray-900">{auditSummary.totalLocationChanges}</p>
            </div>
          </div>
        </div>
        
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <span className="text-2xl">💾</span>
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-500">Firmware Checks</p>
              <p className="text-2xl font-semibold text-gray-900">{auditSummary.totalFirmwareChecks}</p>
            </div>
          </div>
        </div>
        
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <span className="text-2xl">⚠️</span>
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-500">Devices with Issues</p>
              <p className="text-2xl font-semibold text-red-600">{auditSummary.devicesWithIssues}</p>
            </div>
          </div>
        </div>
      </div>
    );
  };

  const renderLocationChanges = () => (
    <div className="bg-white rounded-lg shadow overflow-hidden">
      <div className="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
        <div>
          <h3 className="text-lg font-semibold text-gray-900">Location & Network Changes</h3>
          <p className="text-sm text-gray-600">Track device movement and IP address changes</p>
        </div>
        <button
          onClick={() => handleExport('location-changes')}
          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 text-sm"
        >
          Export CSV
        </button>
      </div>
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Device ID</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Old Location</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">New Location</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Old IP</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">New IP</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Timestamp</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {locationChanges.map((change, index) => (
              <tr key={change.id || index} className="hover:bg-gray-50">
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                  {change.deviceId}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {change.oldLocation || '—'}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {change.newLocation || '—'}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 font-mono">
                  {change.oldIpAddress || '—'}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 font-mono">
                  {change.newIpAddress || '—'}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {auditService.formatTimestamp(change.timestamp)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {locationChanges.length === 0 && (
          <div className="text-center py-8 text-gray-500">
            No location or network changes recorded
          </div>
        )}
      </div>
    </div>
  );

  const renderQuarantineHistory = () => (
    <div className="bg-white rounded-lg shadow overflow-hidden">
      <div className="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
        <div>
          <h3 className="text-lg font-semibold text-gray-900">Quarantine History</h3>
          <p className="text-sm text-gray-600">Complete audit trail of security actions</p>
        </div>
        <button
          onClick={() => handleExport('quarantine-history')}
          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 text-sm"
        >
          Export CSV
        </button>
      </div>
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Device ID</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Reason</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Error Message</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Timestamp</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {quarantineHistory.map((log, index) => (
              <tr key={log.id || index} className="hover:bg-gray-50">
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                  {log.deviceId}
                </td>
                <td className="px-6 py-4 text-sm text-gray-500">
                  {log.reason}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {getStatusBadge(log.status)}
                </td>
                <td className="px-6 py-4 text-sm text-gray-500">
                  {log.errorMessage || '—'}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {auditService.formatTimestamp(log.timestamp)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {quarantineHistory.length === 0 && (
          <div className="text-center py-8 text-gray-500">
            No quarantine actions recorded
          </div>
        )}
      </div>
    </div>
  );

  const renderFirmwareLogs = () => (
    <div className="bg-white rounded-lg shadow overflow-hidden">
      <div className="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
        <div>
          <h3 className="text-lg font-semibold text-gray-900">Firmware Management</h3>
          <p className="text-sm text-gray-600">Track firmware versions and patch status</p>
        </div>
        <button
          onClick={() => handleExport('firmware-logs')}
          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 text-sm"
        >
          Export CSV
        </button>
      </div>
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Device ID</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Firmware Version</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Expected Version</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Patch Status</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Valid</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Timestamp</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {firmwareLogs.map((log, index) => (
              <tr key={log.id || index} className="hover:bg-gray-50">
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                  {log.deviceId}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 font-mono">
                  {log.firmwareVersion}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 font-mono">
                  {log.expectedVersion || '—'}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {log.reportedPatchStatus || '—'}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {log.firmwareValid ? (
                    <span className="text-green-600 font-bold">✅ Valid</span>
                  ) : (
                    <span className="text-red-500 font-bold">❌ Invalid</span>
                  )}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {auditService.formatTimestamp(log.timestamp)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {firmwareLogs.length === 0 && (
          <div className="text-center py-8 text-gray-500">
            No firmware logs recorded
          </div>
        )}
      </div>
    </div>
  );

  const renderContent = () => {
    switch (activeTab) {
      case 'location':
        return renderLocationChanges();
      case 'quarantine':
        return renderQuarantineHistory();
      case 'firmware':
        return renderFirmwareLogs();
      default:
        return renderLocationChanges();
    }
  };

  if (error) {
    return (
      <div className="flex h-screen bg-gray-100 text-gray-800">
        <Sidebar isOpen={sidebarOpen} onToggle={toggleSidebar} />
        <div className="flex flex-col flex-1">
          <Header onMenuToggle={toggleSidebar} />
          <main className="flex-1 p-6">
            <div className="bg-red-50 border border-red-200 rounded-md p-4">
              <div className="flex">
                <span className="text-red-400 mr-2">❌</span>
                <div>
                  <h3 className="text-sm font-medium text-red-800">Error loading audit data</h3>
                  <p className="text-sm text-red-700 mt-1">{error}</p>
                  <button 
                    onClick={() => {
                      setError(null);
                      fetchInitialData();
                    }}
                    className="mt-3 text-sm bg-red-100 text-red-800 px-3 py-1 rounded hover:bg-red-200"
                  >
                    Try Again
                  </button>
                </div>
              </div>
            </div>
          </main>
        </div>
      </div>
    );
  }

  return (
    <div className="flex h-screen bg-gray-100 text-gray-800">
      <Sidebar isOpen={sidebarOpen} onToggle={toggleSidebar} />

      <div className={`flex flex-col flex-1 transition-all duration-300 ${sidebarOpen ? '' : ''}`}>
        <Header onMenuToggle={toggleSidebar} />

        <main className="flex-1 p-6 overflow-y-auto w-full">
          <div className="mb-6">
            <h1 className="text-3xl font-bold mb-2">📊 Audit & Analytics</h1>
            <p className="text-gray-600">Comprehensive audit trail and device analytics</p>
          </div>

          {/* Summary Cards */}
          {renderSummaryCards()}

          {/* Filters */}
          <div className="bg-white rounded-lg shadow p-4 mb-6">
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
              <div className="flex items-center gap-4">
                <label htmlFor="device-filter" className="text-sm font-medium text-gray-700">
                  Filter by Device:
                </label>
                <select
                  id="device-filter"
                  value={selectedDevice}
                  onChange={(e) => setSelectedDevice(e.target.value)}
                  className="border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="">All Devices</option>
                  {devices.map(device => (
                    <option key={device.deviceId} value={device.deviceId}>
                      {device.deviceId}
                    </option>
                  ))}
                </select>
              </div>
              <div className="text-sm text-gray-500">
                {selectedDevice ? `Showing data for: ${selectedDevice}` : 'Showing all devices'}
              </div>
            </div>
          </div>

          {/* Tab Navigation */}
          <div className="border-b border-gray-200 mb-6">
  <nav className="-mb-px flex space-x-8">
    {tabs.map(tab => (
      <button
        key={tab.id}
        onClick={() => {
          setActiveTab(tab.id);
          // Push the new tab into the URL query ?tab=<tab.id>
          setSearchParams({ tab: tab.id });
        }}
        className={`py-2 px-1 border-b-2 font-medium text-sm whitespace-nowrap ${
          activeTab === tab.id
            ? 'border-blue-500 text-blue-600'
            : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
        }`}
      >
        <span className="mr-2">{tab.icon}</span>
        {tab.name}
      </button>
    ))}
  </nav>
</div>

          {/* Content */}
          {loading ? (
            <div className="text-center py-12">
              <div className="text-lg text-gray-600">🔄 Loading audit data...</div>
            </div>
          ) : (
            renderContent()
          )}
        </main>
      </div>

      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-black bg-opacity-50 z-20 lg:hidden"
          onClick={toggleSidebar}
        />
      )}
    </div>
  );
}