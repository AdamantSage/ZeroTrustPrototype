// src/components/Enhanced/SystemHealth.js
import React from 'react';
import { Activity } from 'lucide-react';

export default function SystemHealth() {
  return (
    <div className="bg-white border rounded-lg p-4">
      <div className="flex items-center justify-between mb-3">
        <div className="font-medium">
          <Activity className="w-4 h-4 inline mr-2" />
          System Health
        </div>
        <div className="text-xs text-gray-500">Summary</div>
      </div>

      <div className="space-y-3 text-sm text-gray-700">
        <div className="flex items-center justify-between">
          <div>Overall Security Score</div>
          <div className="font-medium">65%</div>
        </div>
        <div className="flex items-center justify-between">
          <div>Network Compliance</div>
          <div className="font-medium">82%</div>
        </div>
        <div className="flex items-center justify-between">
          <div>Firmware Compliance</div>
          <div className="font-medium">45%</div>
        </div>
        <div className="flex items-center justify-between">
          <div>Identity Verification</div>
          <div className="font-medium">88%</div>
        </div>
      </div>

      <div className="mt-3 border-t pt-3">
        <button className="w-full px-3 py-2 bg-blue-50 rounded text-sm">
          Force Firmware Updates
        </button>
        <button className="w-full mt-2 px-3 py-2 bg-red-50 rounded text-sm">
          Quarantine Suspicious Devices
        </button>
      </div>
    </div>
  );
}