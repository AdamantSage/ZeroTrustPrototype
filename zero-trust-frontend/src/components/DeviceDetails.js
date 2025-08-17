// src/components/Enhanced/DeviceDetails.js
import React from 'react';

export default function DeviceDetails({ device, onQuarantine, onClose, getRiskColor }) {
  return (
    <div className="border rounded-lg p-4">
      <div className="flex items-center justify-between">
        <div>
          <div className="font-semibold">{device.name || device.deviceId}</div>
          <div className="text-xs text-gray-600">
            {device.location || '–'} • {device.ipAddress || '–'}
          </div>
        </div>
        <div className="space-x-2">
          {!device.quarantined && (
            <button 
              type="button" 
              onClick={() => onQuarantine(device.deviceId)} 
              className="px-3 py-1 bg-red-500 text-white text-sm rounded"
            >
              Quarantine
            </button>
          )}
          <button 
            type="button" 
            onClick={onClose} 
            className="px-3 py-1 bg-gray-100 text-sm rounded"
          >
            Close
          </button>
        </div>
      </div>

      <div className="mt-4 grid grid-cols-3 gap-4 text-xs text-gray-600">
        <div>
          <div className="font-medium">Firmware</div>
          <div className={`${device.firmwareValid ? 'text-green-600' : 'text-red-600'}`}>
            {device.firmwareVersion || '–'}
          </div>
        </div>
        <div>
          <div className="font-medium">Suspicious</div>
          <div className={`${(device.suspiciousActivityScore || 0) > 5 ? 'text-red-600' : 'text-green-600'}`}>
            Level {device.suspiciousActivityScore || 0}/10
          </div>
        </div>
        <div>
          <div className="font-medium">Location Risk</div>
          <div className={`${getRiskColor(device.riskLevel)}`}>
            {device.riskLevel || 'N/A'}
          </div>
        </div>
      </div>
    </div>
  );
}