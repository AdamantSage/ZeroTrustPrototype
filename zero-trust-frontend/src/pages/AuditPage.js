import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import Sidebar from '../components/common/Sidebar';
import Header from '../components/common/Header';
import auditService from '../services/AuditService';
import {
  SummaryCards,
  Filters,
  TabsNav,
  LocationTable,
  QuarantineTable,
  FirmwareTable
} from './AuditComponents/index';

export default function AuditPage() {
  const h = React.createElement;
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
      const deviceList = await auditService.getDevices();
      setDevices(deviceList);

      const summary = await auditService.getAuditSummary();
      setAuditSummary(summary);

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

  const tabs = [
    { id: 'location', name: 'Location/Network Changes', icon: '🌍' },
    { id: 'quarantine', name: 'Quarantine History', icon: '🚨' },
    { id: 'firmware', name: 'Firmware Management', icon: '💾' }
  ];

  const renderContent = function() {
    switch (activeTab) {
      case 'location':
        return h(LocationTable, { locationChanges, handleExport, auditService });
      case 'quarantine':
        return h(QuarantineTable, { quarantineHistory, handleExport });
      case 'firmware':
        return h(FirmwareTable, { firmwareLogs, handleExport });
      default:
        return h(LocationTable, { locationChanges, handleExport, auditService });
    }
  };

  if (error) {
    return h('div', { className: 'flex h-screen w-screen overflow-hidden bg-gray-100 text-gray-800' },
      h(Sidebar, { isOpen: sidebarOpen, onToggle: toggleSidebar }),
      h('div', { className: 'flex flex-col flex-1' },
        h(Header, { onMenuToggle: toggleSidebar }),
        h('main', { className: 'flex-1 p-6' },
          h('div', { className: 'bg-red-50 border border-red-200 rounded-md p-4' },
            h('div', { className: 'flex' },
              h('span', { className: 'text-red-400 mr-2' }, '❌'),
              h('div', null,
                h('h3', { className: 'text-sm font-medium text-red-800' }, 'Error loading audit data'),
                h('p', { className: 'text-sm text-red-700 mt-1' }, error),
                h('button', {
                  onClick: function() { setError(null); fetchInitialData(); },
                  className: 'mt-3 text-sm bg-red-100 text-red-800 px-3 py-1 rounded hover:bg-red-200'
                }, 'Try Again')
              )
            )
          )
        )
      )
    );
  }

  return h('div', { className: 'flex h-screen w-screen overflow-hidden bg-gray-100 text-gray-800' },
    h(Sidebar, { isOpen: sidebarOpen, onToggle: toggleSidebar }),

    h('div', { className: 'flex flex-col flex-1 w-full transition-all duration-300 lg:ml-64' },
      h(Header, { onMenuToggle: toggleSidebar }),

      h('main', { className: 'flex-1 p-6 overflow-y-auto w-full' },
        h('div', { className: 'mb-6' },
          h('h1', { className: 'text-3xl font-bold mb-2' }, '📊 Audit & Analytics'),
          h('p', { className: 'text-gray-600' }, 'Comprehensive audit trail and device analytics')
        ),

        // Summary Cards
        h(SummaryCards, { auditSummary }),

        // Filters
        h(Filters, {
          devices,
          selectedDevice,
          onDeviceChange: (val) => setSelectedDevice(val),
          onExport: handleExport
        }),

        // Tab Navigation
        h('div', { className: 'border-b border-gray-200 mb-6' },
          h(TabsNav, {
            tabs,
            activeTab,
            onTabChange: function(tabId) {
              setActiveTab(tabId);
              setSearchParams({ tab: tabId });
            }
          })
        ),

        // Content / Loading
        loading ? h('div', { className: 'text-center py-12' },
          h('div', { className: 'text-lg text-gray-600' }, '🔄 Loading audit data...')
        ) : renderContent()
      )
    ),

    sidebarOpen && h('div', {
      className: 'fixed inset-0 bg-black bg-opacity-50 z-20 lg:hidden',
      onClick: toggleSidebar
    })
  );
}