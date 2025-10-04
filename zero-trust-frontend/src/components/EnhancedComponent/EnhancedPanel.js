// src/components/Enhanced/EnhancedPanel.js
import React, { useEffect, useMemo, useState } from 'react';
import {
  MapPin,
  Shield,
  AlertTriangle,
  Eye,
  CheckCircle,
  Lock,
  BarChart3,
  Gauge,
  HardDrive
} from 'lucide-react';

// Import the broken-down components
import SmallStat from './SmallStat';
import TrustPanel from './TrustPanel';
import CampusMap from './CampusMap';
import AlertsPanel from './AlertPanel';
import SystemHealth from './SystemHealth';
import DeviceList from '../DeviceList';
import DeviceDetails from '../DeviceDetails';

// Update these relative imports to match your project layout.
// This file lives at: src/components/Enhanced/EnhancedPanel.js
// Services live at: src/services/*.js
import { getDevices as fetchDevices, quarantineDevice } from '../../services/deviceService';
import { getAuditSummary as fetchAuditSummary } from '../../services/AuditService';

export default function EnhancedPanel({ 
  initialDevices = [], 
  loading: parentLoading = false, 
  onQuarantine 
}) {
  const [devices, setDevices] = useState(initialDevices || []);
  const [loading, setLoading] = useState(parentLoading);
  const [filter, setFilter] = useState('all');
  const [view, setView] = useState('current');
  const [selectedDevice, setSelectedDevice] = useState(null);
  const [showTrustBreakdown, setShowTrustBreakdown] = useState(false);
  const [auditSummary, setAuditSummary] = useState(null);

  useEffect(() => {
    if (!initialDevices || initialDevices.length === 0) {
      loadDevices();
    } else {
      setDevices(initialDevices);
    }
    loadAuditSummary();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [initialDevices]);

  async function loadDevices() {
    setLoading(true);
    try {
      const list = await fetchDevices();
      setDevices(list);
    } catch (e) {
      console.error('loadDevices failed', e);
    } finally {
      setLoading(false);
    }
  }

  async function loadAuditSummary() {
    try {
      const s = await fetchAuditSummary();
      setAuditSummary(s);
    } catch (e) {
      console.warn('audit summary failed, using fallback', e);
      setAuditSummary({ 
        totalLocationChanges: 0, 
        totalFirmwareChecks: 0, 
        totalQuarantineActions: 0 
      });
    }
  }

  const handleDeviceClick = (device) => {
    setSelectedDevice(device);
    setShowTrustBreakdown(false);
  };

  const handleTrustScoreClick = (device) => {
    setSelectedDevice(device);
    setShowTrustBreakdown(true);
  };

  const handleQuarantineLocal = async (deviceId) => {
    const reason = prompt('Quarantine reason?');
    if (!reason) return;
    
    try {
      if (onQuarantine) {
        await onQuarantine(deviceId, reason);
      } else {
        await quarantineDevice(deviceId, reason);
      }

      setDevices(ds => ds.map(d => 
        d.deviceId === deviceId 
          ? { ...d, quarantined: true, trusted: false, status: 'QUARANTINED' } 
          : d
      ));
    } catch (err) {
      console.error('Quarantine failed', err);
      alert('Failed to quarantine device');
    }
  };

  const filteredDevices = devices.filter(device => {
    if (filter === 'all') return true;
    if (filter === 'trusted') return device.status === 'TRUSTED' || device.trusted === true;
    if (filter === 'suspicious') return device.status === 'SUSPICIOUS' || (device.trusted === false && !device.quarantined);
    if (filter === 'quarantined') return device.status === 'QUARANTINED' || device.quarantined === true;
    return true;
  });

  const avgTrust = useMemo(() => {
    if (!devices || devices.length === 0) return 0;
    const sum = devices.reduce((s, d) => s + (d.trustScore || 0), 0);
    return Math.round(sum / devices.length);
  }, [devices]);

  const getStatusColor = (status) => {
    switch (status) {
      case 'TRUSTED': return 'text-green-600 bg-green-50 border-green-200';
      case 'SUSPICIOUS': return 'text-yellow-600 bg-yellow-50 border-yellow-200';
      case 'QUARANTINED': return 'text-red-600 bg-red-50 border-red-200';
      default: return 'text-gray-600 bg-gray-50 border-gray-200';
    }
  };

  const getRiskColor = (level) => {
    switch (level) {
      case 'LOW': return 'text-green-600';
      case 'MEDIUM': return 'text-yellow-600';
      case 'HIGH': return 'text-orange-600';
      case 'CRITICAL': return 'text-red-600';
      default: return 'text-gray-600';
    }
  };

  const getLocationTypeColor = (type) => {
    switch (type) {
      case 'ACADEMIC': return 'bg-blue-100 text-blue-800';
      case 'LAB': return 'bg-purple-100 text-purple-800';
      case 'SOCIAL': return 'bg-green-100 text-green-800';
      case 'RESTRICTED': return 'bg-red-100 text-red-800';
      case 'EXTERNAL': return 'bg-orange-100 text-orange-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  if (loading || parentLoading) {
    return (
      <div className="p-8 bg-gray-50 min-h-56 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600 mx-auto mb-3"></div>
          <p className="text-gray-600">Loading devices...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="w-full overflow-hidden rounded-lg shadow bg-white">
      {/* Header Section */}
      <div className="p-6 border-b">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-xl font-bold text-gray-900 flex items-center">
              <Shield className="w-5 h-5 mr-2" /> 
              Device Overview
            </h2>
            <p className="text-sm text-gray-600 mt-1">
              Real-time device monitoring – click a device for details
            </p>
          </div>
        </div>

        {/* Statistics Grid */}
        <div className="grid grid-cols-2 md:grid-cols-6 gap-4 mt-6">
          <SmallStat 
            icon={<CheckCircle className="w-5 h-5 text-green-600" />} 
            title="Trusted" 
            value={devices.filter(d => d.status === 'TRUSTED' || d.trusted).length} 
          />
          <SmallStat 
            icon={<AlertTriangle className="w-5 h-5 text-yellow-600" />} 
            title="Suspicious" 
            value={devices.filter(d => d.status === 'SUSPICIOUS' || !d.trusted).length} 
          />
          <SmallStat 
            icon={<Lock className="w-5 h-5 text-red-600" />} 
            title="Quarantined" 
            value={devices.filter(d => d.status === 'QUARANTINED' || d.quarantined).length} 
          />
          <SmallStat 
            icon={<Gauge className="w-5 h-5 text-blue-600" />} 
            title="Avg Trust" 
            value={avgTrust} 
          />
          <SmallStat 
            icon={<MapPin className="w-5 h-5 text-orange-600" />} 
            title="Location Changes" 
            value={auditSummary?.totalLocationChanges || 0} 
          />
          <SmallStat 
            icon={<HardDrive className="w-5 h-5 text-purple-600" />} 
            title="Firmware Issues" 
            value={devices.filter(d => !d.firmwareValid).length} 
          />
        </div>
      </div>

      {/* Filter Section */}
      <div className="p-4 border-b">
        <div className="flex flex-wrap gap-2">
          {['all', 'trusted', 'suspicious', 'quarantined'].map(option => (
            <button 
              key={option} 
              type="button" 
              onClick={() => setFilter(option)} 
              className={`px-3 py-1 rounded text-sm ${
                filter === option 
                  ? 'bg-blue-600 text-white' 
                  : 'bg-white text-gray-700 border'
              }`}
            >
              {option === 'all' 
                ? 'All Devices' 
                : option.charAt(0).toUpperCase() + option.slice(1)
              }
            </button>
          ))}
        </div>
      </div>

      {/* Main Content */}
      <div className="p-6">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Left Column - Device List and Details */}
          <div className="lg:col-span-2 space-y-4">
            <DeviceList
              devices={filteredDevices}
              onDeviceClick={handleDeviceClick}
              onTrustScoreClick={handleTrustScoreClick}
              getStatusColor={getStatusColor}
              getRiskColor={getRiskColor}
              getLocationTypeColor={getLocationTypeColor}
            />

            {showTrustBreakdown && selectedDevice && (
              <TrustPanel 
                device={selectedDevice} 
                onClose={() => setShowTrustBreakdown(false)} 
                onQuarantine={handleQuarantineLocal} 
              />
            )}

            {!showTrustBreakdown && selectedDevice && (
              <DeviceDetails
                device={selectedDevice}
                onQuarantine={handleQuarantineLocal}
                onClose={() => setSelectedDevice(null)}
                getRiskColor={getRiskColor}
              />
            )}
          </div>

          {/* Right Column - Map, Alerts, System Health */}
          <div className="space-y-4">
            <CampusMap devices={devices} />
            <AlertsPanel devices={devices} />
            <SystemHealth />
          </div>
        </div>
      </div>
    </div>
  );
}