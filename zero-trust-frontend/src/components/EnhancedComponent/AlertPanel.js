// src/components/Enhanced/AlertPanel.js
import React from 'react';
import { AlertTriangle, Shield } from 'lucide-react';

export default function AlertsPanel({ devices = [] }) {
  // defensive: make sure devices is an array
  const devs = Array.isArray(devices) ? devices : [];

  const alerts = [];

  devs.forEach((d) => {
    const deviceId = d.deviceId || d.id || d.device_id || 'unknown-device';

    // Handle alerts that are strings OR objects { message|text, severity }
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

    // Generate alerts based on device state
    // Trust score alerts
    const trustScore = d.trustScore || 0;
    if (trustScore < 30) {
      alerts.push({
        deviceId,
        text: `Critical trust score: ${trustScore}%`,
        severity: 'CRITICAL',
        type: 'TRUST_SCORE',
        timestamp: new Date(),
        id: `${deviceId}-trust-critical`
      });
    } else if (trustScore < 50) {
      alerts.push({
        deviceId,
        text: `Low trust score: ${trustScore}%`,
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

    // Quarantine status alert
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
        text: 'Firmware Outdated',
        severity: 'MEDIUM',
        type: 'FIRMWARE',
        timestamp: new Date(),
        id: `${deviceId}-firmware`
      });
    }
  });

  // Sort alerts by severity and time
  const sortedAlerts = alerts.sort((a, b) => {
    const severityOrder = { CRITICAL: 0, HIGH: 1, MEDIUM: 2, LOW: 3 };
    const severityDiff = severityOrder[a.severity] - severityOrder[b.severity];
    return severityDiff !== 0 ? severityDiff : b.timestamp - a.timestamp;
  });

  const getSeverityColor = (severity) => {
    switch (severity) {
      case 'CRITICAL':
        return 'bg-red-50 border-red-200 text-red-800';
      case 'HIGH':
        return 'bg-orange-50 border-orange-200 text-orange-800';
      case 'MEDIUM':
        return 'bg-yellow-50 border-yellow-200 text-yellow-800';
      default:
        return 'bg-blue-50 border-blue-200 text-blue-800';
    }
  };

  const getSeverityIcon = (severity) => {
    switch (severity) {
      case 'CRITICAL':
        return '🚨';
      case 'HIGH':
        return '⚠️';
      case 'MEDIUM':
        return '⚡';
      default:
        return 'ℹ️';
    }
  };

  return (
    <div className="bg-white border rounded-lg p-4">
      <div className="flex items-center justify-between mb-3">
        <div className="font-medium">
          <AlertTriangle className="w-4 h-4 inline mr-2" />
          Security Alerts
          {sortedAlerts.length > 0 && (
            <span className="ml-2 text-xs bg-red-100 text-red-700 px-2 py-1 rounded-full">
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
                  <span>{getSeverityIcon(a.severity)}</span>
                  <div className="text-sm font-medium">{a.deviceId}</div>
                  <span className="text-xs bg-white bg-opacity-50 px-2 py-1 rounded">
                    {a.type.replace('_', ' ')}
                  </span>
                </div>
                <div className="text-sm">{a.text}</div>
                <div className="text-xs text-gray-600 mt-1">
                  {a.timestamp.toLocaleTimeString()}
                </div>
              </div>
              <div className="text-xs text-gray-500">{a.severity}</div>
            </div>
          </div>
        ))}
      </div>

      {/* Alert Summary */}
      {sortedAlerts.length > 0 && (
        <div className="mt-4 pt-4 border-t">
          <div className="grid grid-cols-3 gap-4 text-center text-xs">
            <div>
              <div className="font-bold text-red-600">
                {sortedAlerts.filter(a => a.severity === 'CRITICAL').length}
              </div>
              <div className="text-gray-600">Critical</div>
            </div>
            <div>
              <div className="font-bold text-orange-600">
                {sortedAlerts.filter(a => a.severity === 'HIGH').length}
              </div>
              <div className="text-gray-600">High</div>
            </div>
            <div>
              <div className="font-bold text-yellow-600">
                {sortedAlerts.filter(a => a.severity === 'MEDIUM').length}
              </div>
              <div className="text-gray-600">Medium</div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}