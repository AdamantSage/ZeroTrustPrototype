// src/components/Enhanced/EnhancedPanel.jsx
import React, { useEffect, useMemo, useState } from 'react';
import {
  MapPin,
  Shield,
  AlertTriangle,
  Activity,
  Eye,
  Clock,
  Monitor,
  CheckCircle,
  XCircle,
  AlertCircle,
  Info,
  Lock,
  BarChart3,
  Target,
  Gauge,
  HardDrive,
  Network,
  FileCheck,
  UserCheck,
  MapIcon
} from 'lucide-react';

import { getDevices as fetchDevices, quarantineDevice } from '../services/deviceService';
import { getAuditSummary as fetchAuditSummary } from '../services/AuditService';

/**
 * EnhancedPanel
 * - Uses fetchDevices and fetchAuditSummary
 * - Keeps theme consistent with your existing layout
 */
export default function EnhancedPanel({ initialDevices = [], loading: parentLoading = false, onQuarantine }) {
  const [devices, setDevices] = useState(initialDevices || []);
  const [loading, setLoading] = useState(parentLoading);
  const [filter, setFilter] = useState('all');
  const [view, setView] = useState('current');
  const [selectedDevice, setSelectedDevice] = useState(null);
  const [showTrustBreakdown, setShowTrustBreakdown] = useState(false);
  const [auditSummary, setAuditSummary] = useState(null);

  useEffect(() => {
    // If parent passed devices, use them; otherwise fetch.
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
      setAuditSummary({ totalLocationChanges: 0, totalFirmwareChecks: 0, totalQuarantineActions: 0 });
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
    if (onQuarantine) await onQuarantine(deviceId, reason);
    else await quarantineDevice(deviceId, reason);

    setDevices(ds => ds.map(d => d.deviceId === deviceId ? { ...d, quarantined: true, trusted: false, status: 'QUARANTINED' } : d));
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
      <div className="p-6 border-b">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-xl font-bold text-gray-900 flex items-center"><Shield className="w-5 h-5 mr-2" /> Device Overview</h2>
            <p className="text-sm text-gray-600 mt-1">Real-time device monitoring — click a device for details</p>
          </div>

          <div className="flex items-center space-x-2">
            <button onClick={() => setView('current')} className={`px-3 py-1 rounded ${view === 'current' ? 'bg-blue-600 text-white' : 'bg-gray-50 text-gray-700'}`}><Eye className="w-4 h-4 inline mr-1" />Live</button>
            <button onClick={() => setView('historical')} className={`px-3 py-1 rounded ${view === 'historical' ? 'bg-blue-600 text-white' : 'bg-gray-50 text-gray-700'}`}><BarChart3 className="w-4 h-4 inline mr-1" />Analytics</button>
          </div>
        </div>

        <div className="grid grid-cols-2 md:grid-cols-6 gap-4 mt-6">
          <SmallStat icon={<CheckCircle className="w-5 h-5 text-green-600" />} title="Trusted" value={devices.filter(d => d.status === 'TRUSTED' || d.trusted).length} />
          <SmallStat icon={<AlertTriangle className="w-5 h-5 text-yellow-600" />} title="Suspicious" value={devices.filter(d => d.status === 'SUSPICIOUS' || !d.trusted).length} />
          <SmallStat icon={<Lock className="w-5 h-5 text-red-600" />} title="Quarantined" value={devices.filter(d => d.status === 'QUARANTINED' || d.quarantined).length} />
          <SmallStat icon={<Gauge className="w-5 h-5 text-blue-600" />} title="Avg Trust" value={avgTrust} />
          <SmallStat icon={<MapPin className="w-5 h-5 text-orange-600" />} title="Location Changes" value={auditSummary?.totalLocationChanges || 0} />
          <SmallStat icon={<HardDrive className="w-5 h-5 text-purple-600" />} title="Firmware Issues" value={devices.filter(d => !d.firmwareValid).length} />
        </div>
      </div>

      <div className="p-4 border-b">
        <div className="flex flex-wrap gap-2">
          {['all', 'trusted', 'suspicious', 'quarantined'].map(option => (
            <button key={option} onClick={() => setFilter(option)} className={`px-3 py-1 rounded text-sm ${filter === option ? 'bg-blue-600 text-white' : 'bg-white text-gray-700 border'}`}>
              {option === 'all' ? 'All Devices' : option.charAt(0).toUpperCase() + option.slice(1)}
            </button>
          ))}
        </div>
      </div>

      <div className="p-6">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2 space-y-4">
            <div className="border rounded-lg">
              <div className="p-4 border-b flex items-center justify-between">
                <div className="font-medium">Devices ({filteredDevices.length})</div>
                <div className="text-sm text-gray-500">Click a device to open details</div>
              </div>

              <div className="divide-y">
                {filteredDevices.map(device => (
                  <div key={device.deviceId} className="p-4 hover:bg-gray-50 cursor-pointer flex items-start justify-between" onClick={() => handleDeviceClick(device)}>
                    <div className="flex items-start space-x-4 min-w-0">
                      <div className="relative">
                        <Monitor className="w-10 h-10 text-gray-600" />
                        {(device.status === 'QUARANTINED' || device.quarantined) && <Lock className="w-4 h-4 text-red-600 absolute -top-1 -right-1 bg-white rounded-full" />}
                      </div>
                      <div className="min-w-0">
                        <div className="font-semibold text-gray-900 truncate">{device.name || device.deviceId}</div>
                        <div className="flex items-center space-x-2 mt-1 text-xs text-gray-600">
                          <span className={`px-2 py-0.5 rounded-full border ${getStatusColor(device.status || (device.trusted ? 'TRUSTED' : 'SUSPICIOUS'))}`}>{device.status || (device.trusted ? 'TRUSTED' : 'SUSPICIOUS')}</span>
                          {device.location && <span className={`px-2 py-0.5 rounded-full ${getLocationTypeColor(device.locationType || '')}`}>{(device.location || '').replace(/-/g, ' ')}</span>}
                        </div>

                        <div className="flex items-center space-x-4 mt-2 text-xs text-gray-600">
                          <div className="flex items-center"><MapPin className="w-4 h-4 mr-1" />{device.location || '—'}</div>
                          <div className="flex items-center"><Network className="w-4 h-4 mr-1" />{device.ipAddress || '—'}</div>
                          <div className="flex items-center"><Clock className="w-4 h-4 mr-1" />{device.lastSeen ? new Date(device.lastSeen).toLocaleTimeString() : '—'}</div>
                        </div>
                      </div>
                    </div>

                    <div className="text-right space-y-2">
                      <div onClick={(e) => { e.stopPropagation(); handleTrustScoreClick(device); }} className="p-2 rounded-lg hover:bg-gray-100 cursor-pointer">
                        <div className={`text-2xl font-bold ${device.trustScore >= 70 ? 'text-green-600' : device.trustScore >= 50 ? 'text-yellow-600' : 'text-red-600'}`}>{Math.round(device.trustScore || 0)}</div>
                        <div className="text-xs text-gray-500">Trust Score</div>
                      </div>
                      <div className="flex items-center justify-end space-x-2 text-xs">
                        <div className={`${getRiskColor(device.riskLevel)} flex items-center`}><Target className="w-3 h-3 mr-1" />{device.riskLevel || 'N/A'}</div>
                        <div className="text-gray-600">{Math.round((device.anomalyScore || 0) * 100)}%</div>
                      </div>
                    </div>
                  </div>
                ))}

                {filteredDevices.length === 0 && (
                  <div className="p-6 text-center text-sm text-gray-500">No devices match the filter</div>
                )}
              </div>
            </div>

            {showTrustBreakdown && selectedDevice && (
              <TrustPanel device={selectedDevice} onClose={() => setShowTrustBreakdown(false)} onQuarantine={handleQuarantineLocal} />
            )}

            {!showTrustBreakdown && selectedDevice && (
              <div className="border rounded-lg p-4">
                <div className="flex items-center justify-between">
                  <div>
                    <div className="font-semibold">{selectedDevice.name || selectedDevice.deviceId}</div>
                    <div className="text-xs text-gray-600">{selectedDevice.location || '—'} • {selectedDevice.ipAddress || '—'}</div>
                  </div>
                  <div className="space-x-2">
                    {!selectedDevice.quarantined && <button onClick={() => handleQuarantineLocal(selectedDevice.deviceId)} className="px-3 py-1 bg-red-500 text-white text-sm rounded">Quarantine</button>}
                    <button onClick={() => setSelectedDevice(null)} className="px-3 py-1 bg-gray-100 text-sm rounded">Close</button>
                  </div>
                </div>

                <div className="mt-4 grid grid-cols-3 gap-4 text-xs text-gray-600">
                  <div><div className="font-medium">Firmware</div><div className={`${selectedDevice.firmwareValid ? 'text-green-600' : 'text-red-600'}`}>{selectedDevice.firmwareVersion || '—'}</div></div>
                  <div><div className="font-medium">Suspicious</div><div className={`${(selectedDevice.suspiciousActivityScore || 0) > 5 ? 'text-red-600' : 'text-green-600'}`}>Level {selectedDevice.suspiciousActivityScore || 0}/10</div></div>
                  <div><div className="font-medium">Location Risk</div><div className={`${getRiskColor(selectedDevice.riskLevel)}`}>{selectedDevice.riskLevel || 'N/A'}</div></div>
                </div>
              </div>
            )}

          </div>

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

/* --------------------------- Small reusable pieces ------------------------- */
function SmallStat({ icon, title, value }) {
  return (
    <div className="bg-white p-3 rounded-md border flex items-center space-x-3">
      <div className="p-2 bg-gray-50 rounded">{icon}</div>
      <div>
        <div className="text-xs text-gray-500">{title}</div>
        <div className="text-lg font-bold text-gray-900">{value}</div>
      </div>
    </div>
  );
}

function TrustPanel({ device, onClose, onQuarantine }) {
  const mockTrustAnalysis = device.mockTrustAnalysis || {
    deviceId: device.deviceId,
    overallHealth: device.trustScore > 75 ? 'EXCELLENT' : device.trustScore > 50 ? 'GOOD' : 'CONCERNING',
    riskLevel: device.riskLevel || 'MEDIUM',
    trustFactors: [
      { factorName: 'Identity Verification', category: 'SECURITY', score: 75, impact: 'NEUTRAL', dataPoints: 12, description: 'Identity checks mostly pass', details: { successRate: '75%' } },
      { factorName: 'Location Context', category: 'BEHAVIORAL', score: 30, impact: 'CRITICAL', dataPoints: 6, description: 'Unusual location jumps', details: { recentLocationChanges: 4 } }
    ],
    recommendations: ['Run full security scan', 'Force firmware update']
  };

  const analysis = mockTrustAnalysis;

  return (
    <div className="border rounded-lg p-4">
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center space-x-2">
          <Shield className="w-5 h-5" />
          <div>
            <div className="font-semibold">Trust Analysis — {analysis.deviceId}</div>
            <div className="text-xs text-gray-500">Overall: {analysis.overallHealth} • {analysis.riskLevel} risk</div>
          </div>
        </div>
        <div className="space-x-2">
          <button onClick={() => onQuarantine(device.deviceId)} className="px-3 py-1 bg-red-500 text-white rounded text-sm">Quarantine</button>
          <button onClick={onClose} className="px-3 py-1 bg-gray-100 rounded text-sm">Close</button>
        </div>
      </div>

      <div className="space-y-3">
        {analysis.trustFactors.map((f, i) => (
          <div key={i} className="border rounded p-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <div className={`p-2 rounded ${f.category === 'SECURITY' ? 'bg-red-50' : 'bg-blue-50'}`}><Info className="w-4 h-4" /></div>
                <div>
                  <div className="font-medium">{f.factorName}</div>
                  <div className="text-xs text-gray-500">{f.description}</div>
                </div>
              </div>
              <div className="text-right">
                <div className={`font-bold ${f.score >= 80 ? 'text-green-600' : f.score >= 60 ? 'text-yellow-600' : 'text-red-600'}`}>{f.score}</div>
                <div className="text-xs text-gray-500">{f.dataPoints} checks</div>
              </div>
            </div>
            <div className="mt-2">
              <div className="w-full bg-gray-200 rounded-full h-2"><div className={`${f.score >= 80 ? 'bg-green-500' : f.score >= 60 ? 'bg-yellow-500' : 'bg-red-500'} h-2 rounded-full`} style={{ width: `${f.score}%` }} /></div>
            </div>
          </div>
        ))}

        <div className="border-t pt-3">
          <div className="font-medium mb-2 flex items-center"><Target className="w-4 h-4 mr-2" />Recommendations</div>
          <div className="space-y-1">
            {analysis.recommendations.map((r, idx) => (
              <div key={idx} className="text-sm text-gray-700">• {r}</div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

function CampusMap({ devices = [] }) {
  return (
    <div className="bg-white border rounded-lg p-4">
      <div className="flex items-center justify-between mb-3">
        <div className="font-medium"><MapIcon className="w-4 h-4 inline mr-2" />Campus Map</div>
        <div className="text-xs text-gray-500">Real-time</div>
      </div>

      <div className="relative bg-gradient-to-br from-green-50 to-blue-50 rounded-lg h-48 p-3 border overflow-hidden">
        <div className="absolute top-6 left-6 w-16 h-10 bg-blue-200 rounded shadow-sm border flex items-center justify-center text-xs">Library</div>
        <div className="absolute top-6 right-6 w-16 h-10 bg-purple-200 rounded shadow-sm border flex items-center justify-center text-xs">Lab</div>
        <div className="absolute bottom-6 left-6 w-16 h-10 bg-red-200 rounded shadow-sm border flex items-center justify-center text-xs">Admin</div>
        <div className="absolute bottom-6 right-6 w-16 h-10 bg-green-200 rounded shadow-sm border flex items-center justify-center text-xs">Student</div>

        {devices.slice(0, 6).map((d, i) => {
          const pos = [
            { left: '15%', top: '25%' },
            { left: '80%', top: '20%' },
            { left: '15%', top: '75%' },
            { left: '80%', top: '75%' },
            { left: '50%', top: '10%' },
            { left: '50%', top: '85%' }
          ][i] || { left: '50%', top: '50%' };
          const color = (d.riskLevel === 'CRITICAL') ? 'bg-red-500 animate-pulse' : (d.riskLevel === 'HIGH') ? 'bg-orange-500' : (d.riskLevel === 'MEDIUM') ? 'bg-yellow-500' : 'bg-green-500';
          return (
            <div key={d.deviceId} className="absolute group" style={{ left: pos.left, top: pos.top }}>
              <div className={`w-4 h-4 rounded-full border-2 border-white shadow ${color}`} title={`${d.deviceId} • Trust ${Math.round(d.trustScore || 0)}`}></div>
              <div className="absolute -bottom-6 -left-6 opacity-0 group-hover:opacity-100 transition-opacity bg-black text-white text-xs rounded px-2 py-1">{d.deviceId}</div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

function AlertsPanel({ devices = [] }) {
  const alerts = [];
  devices.forEach(d => {
    if (d.alerts && d.alerts.length) {
      d.alerts.slice(0, 3).forEach(a => alerts.push({ deviceId: d.deviceId, text: a, severity: 'HIGH' }));
    }
    if (d.firmwareValid === false) alerts.push({ deviceId: d.deviceId, text: 'Firmware Outdated', severity: 'MEDIUM' });
  });

  return (
    <div className="bg-white border rounded-lg p-4">
      <div className="flex items-center justify-between mb-3">
        <div className="font-medium"><AlertTriangle className="w-4 h-4 inline mr-2" />Security Alerts</div>
        <div className="text-xs text-gray-500">Live</div>
      </div>

      <div className="space-y-2 max-h-56 overflow-y-auto">
        {alerts.length === 0 && <div className="text-sm text-gray-500">No active alerts</div>}
        {alerts.map((a, i) => (
          <div key={i} className={`p-3 rounded border ${a.severity === 'HIGH' ? 'bg-red-50 border-red-200' : 'bg-yellow-50 border-yellow-200'}`}>
            <div className="flex items-center justify-between">
              <div className="text-sm font-medium">{a.deviceId} — {a.text}</div>
              <div className="text-xs text-gray-500">{a.severity}</div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function SystemHealth() {
  return (
    <div className="bg-white border rounded-lg p-4">
      <div className="flex items-center justify-between mb-3"><div className="font-medium"><Activity className="w-4 h-4 inline mr-2" />System Health</div><div className="text-xs text-gray-500">Summary</div></div>
      <div className="space-y-3 text-sm text-gray-700">
        <div className="flex items-center justify-between"><div>Overall Security Score</div><div className="font-medium">65%</div></div>
        <div className="flex items-center justify-between"><div>Network Compliance</div><div className="font-medium">82%</div></div>
        <div className="flex items-center justify-between"><div>Firmware Compliance</div><div className="font-medium">45%</div></div>
        <div className="flex items-center justify-between"><div>Identity Verification</div><div className="font-medium">88%</div></div>
      </div>

      <div className="mt-3 border-t pt-3">
        <button className="w-full px-3 py-2 bg-blue-50 rounded text-sm">Force Firmware Updates</button>
        <button className="w-full mt-2 px-3 py-2 bg-red-50 rounded text-sm">Quarantine Suspicious Devices</button>
      </div>
    </div>
  );
}
