// src/components/Enhanced/AlertsPanel.js
import React from 'react';
import { AlertTriangle } from 'lucide-react';

export default function AlertsPanel({ devices = [] }) {
  // defensive: make sure devices is an array
  const devs = Array.isArray(devices) ? devices : [];

  const alerts = [];

  devs.forEach((d) => {
    const deviceId = d.deviceId || d.id || d.device_id || 'unknown-device';

    // handle alerts that are strings OR objects { message|text, severity }
    if (Array.isArray(d.alerts) && d.alerts.length > 0) {
      d.alerts.slice(0, 3).forEach((a) => {
        let text = '';
        let sev = 'HIGH';
        if (typeof a === 'string') {
          text = a;
        } else if (a && typeof a === 'object') {
          text = a.message || a.text || JSON.stringify(a);
          if (a.severity) sev = String(a.severity).toUpperCase();
        } else {
          text = String(a);
        }
        alerts.push({ deviceId, text, severity: sev });
      });
    }

    // firmware: 
    // original used (d.firmwareValid === false). If your backend returns null/0/'invalid',
    // you might prefer to trigger on falsy: (!d.firmwareValid)
    // Keep original strict check if you only want explicit `false` to be flagged.
    if (d.firmwareValid === false || d.firmwareValid === 'invalid' || d.firmwareValid === 0) {
      alerts.push({
        deviceId,
        text: 'Firmware Outdated',
        severity: 'MEDIUM'
      });
    }
  });

  return (
    <div className="bg-white border rounded-lg p-4">
      <div className="flex items-center justify-between mb-3">
        <div className="font-medium">
          <AlertTriangle className="w-4 h-4 inline mr-2" />
          Security Alerts
        </div>
        <div className="text-xs text-gray-500">Live</div>
      </div>

      <div className="space-y-2 max-h-56 overflow-y-auto">
        {alerts.length === 0 && (
          <div className="text-sm text-gray-500">No active alerts</div>
        )}

        {alerts.map((a, i) => (
          <div
            key={`${a.deviceId}-${i}`}
            className={`p-3 rounded border ${
              a.severity === 'HIGH'
                ? 'bg-red-50 border-red-200'
                : 'bg-yellow-50 border-yellow-200'
            }`}
          >
            <div className="flex items-center justify-between">
              <div className="text-sm font-medium">{a.deviceId} – {a.text}</div>
              <div className="text-xs text-gray-500">{a.severity}</div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
