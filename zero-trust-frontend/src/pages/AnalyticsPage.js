// src/pages/AnalyticsPage.js
import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import Sidebar from '../components/common/Sidebar';
import Header from '../components/common/Header';
import analyticsService from '../services/analyticsService';
import { getDevices } from '../services/deviceService';
import {
  SystemOverview,
  DeviceRiskPanel,
  TrustAnalysisPanel,
  TimelineChart,
  RecentChangesPanel,
  AttentionDevices,
  EnhancedLocationMap
} from '../components/Analytics';

export default function AnalyticsPage() {
  const h = React.createElement;
  const [searchParams, setSearchParams] = useSearchParams();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [selectedDevice, setSelectedDevice] = useState(searchParams.get('device') || '');
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // System-wide data
  const [systemOverview, setSystemOverview] = useState(null);
  const [attentionDevices, setAttentionDevices] = useState([]);
  const [mapData, setMapData] = useState(null);

  // Device-specific data
  const [deviceRisk, setDeviceRisk] = useState(null);
  const [trustAnalysis, setTrustAnalysis] = useState(null);
  const [timeline, setTimeline] = useState([]);
  const [recentChanges, setRecentChanges] = useState(null);

  const toggleSidebar = () => setSidebarOpen(prev => !prev);

  useEffect(() => {
    fetchInitialData();
  }, []);

  useEffect(() => {
    const deviceFromUrl = searchParams.get('device');
    if (deviceFromUrl && deviceFromUrl !== selectedDevice) {
      setSelectedDevice(deviceFromUrl);
    }
  }, [searchParams]);

  useEffect(() => {
    if (selectedDevice) {
      fetchDeviceData(selectedDevice);
    }
  }, [selectedDevice]);

  const fetchInitialData = async () => {
    setLoading(true);
    setError(null);

    try {
      // Fetch devices and system overview in parallel
      const [deviceList, overview, attention, enhancedMap] = await Promise.all([
        getDevices(),
        analyticsService.getSystemRiskOverview(),
        analyticsService.getDevicesRequiringAttention(),
        analyticsService.getEnhancedLocationMapData()
      ]);

      setDevices(deviceList);
      setSystemOverview(overview);
      setAttentionDevices(attention);
      setMapData(enhancedMap);

    } catch (err) {
      console.error('Failed to load initial analytics data:', err);
      setError('Failed to load analytics data. Please try refreshing the page.');
    } finally {
      setLoading(false);
    }
  };

  const fetchDeviceData = async (deviceId) => {
    try {
      const [risk, analysis, timelineData, changes] = await Promise.all([
        analyticsService.getDeviceRiskAssessment(deviceId),
        analyticsService.getDeviceTrustAnalysis(deviceId),
        analyticsService.getTrustScoreTimeline(deviceId, 7),
        analyticsService.getRecentChanges(deviceId, 24)
      ]);

      setDeviceRisk(risk);
      setTrustAnalysis(analysis);
      setTimeline(timelineData);
      setRecentChanges(changes);

    } catch (err) {
      console.error('Failed to load device analytics:', err);
      setError('Failed to load device analytics data.');
    }
  };

  const handleDeviceSelect = (deviceId) => {
    setSelectedDevice(deviceId);
    setSearchParams(deviceId ? { device: deviceId } : {});
  };

  const handleRefresh = async () => {
    await fetchInitialData();
    if (selectedDevice) {
      await fetchDeviceData(selectedDevice);
    }
  };

  const handleResetTrustScore = async (deviceId, baselineScore) => {
    try {
      await analyticsService.resetDeviceTrustScore(deviceId, baselineScore);
      await fetchDeviceData(deviceId);
      await fetchInitialData(); // Refresh system overview
    } catch (err) {
      console.error('Failed to reset trust score:', err);
      setError('Failed to reset trust score.');
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
              h('span', { className: 'text-red-400 mr-2' }, '⚠️'),
              h('div', null,
                h('h3', { className: 'text-sm font-medium text-red-800' }, 'Error loading analytics'),
                h('p', { className: 'text-sm text-red-700 mt-1' }, error),
                h('button', {
                  onClick: () => { setError(null); fetchInitialData(); },
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
        // Header
        h('div', { className: 'mb-6 flex flex-col lg:flex-row lg:items-center lg:justify-between' },
          h('div', null,
            h('h1', { className: 'text-3xl font-bold mb-2' }, '📊 Advanced Analytics'),
            h('p', { className: 'text-gray-600' }, 'Comprehensive device risk assessment and trust analysis')
          ),
          h('div', { className: 'mt-4 lg:mt-0 flex items-center space-x-4' },
            // Device selector
            h('select', {
              value: selectedDevice,
              onChange: (e) => handleDeviceSelect(e.target.value),
              className: 'px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500'
            },
              h('option', { value: '' }, 'All Devices'),
              ...devices.map(device => 
                h('option', { key: device.deviceId, value: device.deviceId }, 
                  `${device.deviceId} (${device.deviceType || 'Unknown'})`
                )
              )
            ),
            // Refresh button
            h('button', {
              onClick: handleRefresh,
              disabled: loading,
              className: 'px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50'
            }, loading ? '⏳' : '🔄 Refresh')
          )
        ),

        loading ? h('div', { className: 'text-center py-12' },
          h('div', { className: 'text-lg text-gray-600' }, '📡 Loading analytics data...')
        ) : h('div', { className: 'space-y-6' },
          
          // System Overview (always shown)
          systemOverview && h(SystemOverview, { 
            data: systemOverview, 
            onDeviceSelect: handleDeviceSelect 
          }),

          // Enhanced Location Map
          mapData && h(EnhancedLocationMap, { 
            data: mapData, 
            onDeviceSelect: handleDeviceSelect,
            selectedDevice 
          }),

          // Devices Requiring Attention
          attentionDevices.length > 0 && h(AttentionDevices, { 
            devices: attentionDevices, 
            onDeviceSelect: handleDeviceSelect 
          }),

          // Device-Specific Analytics (shown when device is selected)
          selectedDevice && h('div', { className: 'space-y-6' },
            h('div', { className: 'border-t pt-6' },
              h('h2', { className: 'text-2xl font-bold mb-4' }, 
                `📱 Device Analytics: ${selectedDevice}`
              )
            ),

            // Two-column layout for device details
            h('div', { className: 'grid grid-cols-1 xl:grid-cols-2 gap-6' },
              // Left column
              h('div', { className: 'space-y-6' },
                deviceRisk && h(DeviceRiskPanel, { 
                  data: deviceRisk, 
                  onResetTrustScore: handleResetTrustScore 
                }),
                recentChanges && h(RecentChangesPanel, { data: recentChanges })
              ),

              // Right column  
              h('div', { className: 'space-y-6' },
                trustAnalysis && h(TrustAnalysisPanel, { data: trustAnalysis }),
                timeline.length > 0 && h(TimelineChart, { 
                  data: timeline, 
                  deviceId: selectedDevice 
                })
              )
            )
          )
        )
      )
    ),

    sidebarOpen && h('div', {
      className: 'fixed inset-0 bg-black bg-opacity-50 z-20 lg:hidden',
      onClick: toggleSidebar
    })
  );
}