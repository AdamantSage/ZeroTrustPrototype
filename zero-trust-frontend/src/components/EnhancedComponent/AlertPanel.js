// src/components/Enhanced/AlertPanel.js
import React from 'react';
import { AlertTriangle, Shield, CheckCircle, Info, TrendingUp } from 'lucide-react';

export default function AlertsPanel({ devices = [] }) {
  const devs = Array.isArray(devices) ? devices : [];
  const alerts = [];

  devs.forEach((d) => {
    const deviceId = d.deviceId || d.id || d.device_id || 'unknown-device';

    // Handle alerts that are strings OR objects
    if (Array.isArray(d.alerts) && d.alerts.length > 0) {
      d.alerts.slice(0, 3).forEach((a, index) => {
        let text = '';
        let sev = 'HIGH';
        let type = 'SYSTEM';
        let timestamp = new Date();

        if (typeof a === 'string') {
          text = a;
        } else if (a && typeof a === 'object') {
          text = a.message || a.text || JSON.stringify(a);
          if (a.severity) sev = String(a.severity).toUpperCase();
          if (a.type) type = String(a.type).toUpperCase();
          if (a.timestamp) timestamp = new Date(a.timestamp);
        } else {
          text = String(a);
        }

        alerts.push({ 
          deviceId, 
          text, 
          severity: sev, 
          type,
          timestamp,
          id: `${deviceId}-alert-${index}`
        });
      });
    }

    // Generate POSITIVE alerts for healthy behavior
    const trustScore = d.trustScore || 0;
    
    // Excellent health alert
    if (trustScore >= 90) {
      alerts.push({
        deviceId,
        text: `Excellent health maintained: ${trustScore.toFixed(1)}%`,
        severity: 'POSITIVE',
        type: 'EXCELLENT_HEALTH',
        timestamp: new Date(),
        id: `${deviceId}-excellent-health`
      });
    }
    
    // Trusted status achieved
    if (trustScore >= 70 && trustScore < 90 && d.trusted) {
      alerts.push({
        deviceId,
        text: `Device trusted: ${trustScore.toFixed(1)}%`,
        severity: 'POSITIVE',
        type: 'TRUSTED_STATUS',
        timestamp: new Date(),
        id: `${deviceId}-trusted`
      });
    }
    
    // Recovery from low trust
    if (trustScore >= 70 && d.previousTrustScore && d.previousTrustScore < 70) {
      alerts.push({
        deviceId,
        text: `Recovered from untrusted status`,
        severity: 'POSITIVE',
        type: 'RECOVERY',
        timestamp: new Date(),
        id: `${deviceId}-recovery`
      });
    }

    // Generate NEGATIVE alerts
    // Critical trust score
    if (trustScore < 30) {
      alerts.push({
        deviceId,
        text: `Critical trust score: ${trustScore.toFixed(1)}%`,
        severity: 'CRITICAL',
        type: 'TRUST_SCORE',
        timestamp: new Date(),
        id: `${deviceId}-trust-critical`
      });
    } 
    // Low trust score
    else if (trustScore < 50) {
      alerts.push({
        deviceId,
        text: `Low trust score: ${trustScore.toFixed(1)}%`,
        severity: 'HIGH',
        type: 'TRUST_SCORE',
        timestamp: new Date(),
        id: `${deviceId}-trust-low`
      });
    }

    // Risk level alerts
    if (d.riskLevel === 'CRITICAL') {
      alerts.push({
        deviceId,
        text: 'Device flagged as critical risk',
        severity: 'CRITICAL',
        type: 'RISK_LEVEL',
        timestamp: new Date(),
        id: `${deviceId}-risk-critical`
      });
    } else if (d.riskLevel === 'HIGH') {
      alerts.push({
        deviceId,
        text: 'Device flagged as high risk',
        severity: 'HIGH',
        type: 'RISK_LEVEL',
        timestamp: new Date(),
        id: `${deviceId}-risk-high`
      });
    }

    // Quarantine status
    if (d.quarantined || d.status === 'QUARANTINED') {
      alerts.push({
        deviceId,
        text: 'Device is quarantined',
        severity: 'HIGH',
        type: 'QUARANTINE',
        timestamp: new Date(),
        id: `${deviceId}-quarantined`
      });
    }

    // Firmware alerts
    if (d.firmwareValid === false || d.firmwareValid === 'invalid' || d.firmwareValid === 0) {
      alerts.push({
        deviceId,
        text: 'Firmware outdated',
        severity: 'MEDIUM',
        type: 'FIRMWARE',
        timestamp: new Date(),
        id: `${deviceId}-firmware`
      });
    }
  });

  // Sort alerts: Positive first, then by severity
  const sortedAlerts = alerts.sort((a, b) => {
    // Positive alerts go first
    if (a.severity === 'POSITIVE' && b.severity !== 'POSITIVE') return -1;
    if (a.severity !== 'POSITIVE' && b.severity === 'POSITIVE') return 1;
    
    const severityOrder = { 
      POSITIVE: 0,
      CRITICAL: 1, 
      HIGH: 2, 
      MEDIUM: 3, 
      LOW: 4,
      INFO: 5
    };
    const severityDiff = severityOrder[a.severity] - severityOrder[b.severity];
    return severityDiff !== 0 ? severityDiff : b.timestamp - a.timestamp;
  });

  const getSeverityColor = (severity) => {
    switch (severity) {
      case 'POSITIVE':
        return 'bg-green-50 border-green-200 text-green-800';
      case 'CRITICAL':
        return 'bg-red-50 border-red-200 text-red-800';
      case 'HIGH':
        return 'bg-orange-50 border-orange-200 text-orange-800';
      case 'MEDIUM':
        return 'bg-yellow-50 border-yellow-200 text-yellow-800';
      case 'INFO':
        return 'bg-blue-50 border-blue-200 text-blue-800';
      default:
        return 'bg-gray-50 border-gray-200 text-gray-800';
    }
  };

  const getSeverityIcon = (severity) => {
    switch (severity) {
      case 'POSITIVE':
        return <CheckCircle className="w-4 h-4 text-green-600" />;
      case 'CRITICAL':
        return <AlertTriangle className="w-4 h-4 text-red-600" />;
      case 'HIGH':
        return <AlertTriangle className="w-4 h-4 text-orange-600" />;
      case 'MEDIUM':
        return <AlertTriangle className="w-4 h-4 text-yellow-600" />;
      case 'INFO':
        return <Info className="w-4 h-4 text-blue-600" />;
      default:
        return <Info className="w-4 h-4 text-gray-600" />;
    }
  };

  const positiveCount = sortedAlerts.filter(a => a.severity === 'POSITIVE').length;
  const criticalCount = sortedAlerts.filter(a => a.severity === 'CRITICAL').length;
  const highCount = sortedAlerts.filter(a => a.severity === 'HIGH').length;
  const mediumCount = sortedAlerts.filter(a => a.severity === 'MEDIUM').length;

  return (
    <div className="bg-white border rounded-lg p-4">
      <div className="flex items-center justify-between mb-3">
        <div className="font-medium flex items-center">
          {positiveCount > criticalCount + highCount ? (
            <TrendingUp className="w-4 h-4 inline mr-2 text-green-600" />
          ) : (
            <AlertTriangle className="w-4 h-4 inline mr-2" />
          )}
          Security Alerts
          {sortedAlerts.length > 0 && (
            <span className={`ml-2 text-xs px-2 py-1 rounded-full ${
              positiveCount > criticalCount + highCount
                ? 'bg-green-100 text-green-700'
                : 'bg-red-100 text-red-700'
            }`}>
              {sortedAlerts.length}
            </span>
          )}
        </div>
        <div className="text-xs text-gray-500">Live</div>
      </div>

      <div className="space-y-2 max-h-56 overflow-y-auto">
        {sortedAlerts.length === 0 && (
          <div className="text-sm text-gray-500 text-center py-4">
            <Shield className="w-8 h-8 mx-auto mb-2 text-gray-400" />
            No active alerts
          </div>
        )}

        {sortedAlerts.map((a) => (
          <div
            key={a.id}
            className={`p-3 rounded border ${getSeverityColor(a.severity)}`}
          >
            <div className="flex items-center justify-between">
              <div className="flex-1">
                <div className="flex items-center space-x-2 mb-1">
                  {getSeverityIcon(a.severity)}
                  <div className="text-sm font-medium">{a.deviceId}</div>
                  <span className="text-xs bg-white bg-opacity-50 px-2 py-1 rounded">
                    {a.type.replace(/_/g, ' ')}
                  </span>
                </div>
                <div className="text-sm">{a.text}</div>
                <div className="text-xs text-gray-600 mt-1">
                  {a.timestamp.toLocaleTimeString()}
                </div>
              </div>
              <div className="text-xs font-medium ml-2">{a.severity}</div>
            </div>
          </div>
        ))}
      </div>

      {/* Alert Summary */}
      {sortedAlerts.length > 0 && (
        <div className="mt-4 pt-4 border-t">
          <div className="grid grid-cols-4 gap-2 text-center text-xs">
            <div>
              <div className="font-bold text-green-600">{positiveCount}</div>
              <div className="text-gray-600">Positive</div>
            </div>
            <div>
              <div className="font-bold text-red-600">{criticalCount}</div>
              <div className="text-gray-600">Critical</div>
            </div>
            <div>
              <div className="font-bold text-orange-600">{highCount}</div>
              <div className="text-gray-600">High</div>
            </div>
            <div>
              <div className="font-bold text-yellow-600">{mediumCount}</div>
              <div className="text-gray-600">Medium</div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}