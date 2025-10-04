// src/components/Enhanced/CampusMap.js
import React, { useMemo } from 'react';
import { MapIcon } from 'lucide-react';

export default function CampusMap({ devices = [] }) {
  const deviceLocations = useMemo(() => devices.map(d => ({
    deviceId: d.deviceId,
    trustScore: Math.round(d.trustScore || 0),
    riskLevel: d.riskLevel || 'LOW',
    coordinates: d.coordinates || null,
    locationType: d.locationType || ''
  })), [devices]);

  const positionsPreset = [
    { left: '15%', top: '25%' },
    { left: '85%', top: '25%' },
    { left: '15%', top: '75%' },
    { left: '85%', top: '75%' },
    { left: '50%', top: '10%' },
    { left: '50%', top: '85%' }
  ];

  return (
    <div className="bg-white rounded-xl shadow-sm border">
      <div className="p-6 border-b">
        <h2 className="text-xl font-bold text-gray-900 flex items-center">
          <MapIcon className="w-5 h-5 mr-2" />
          Campus Map
        </h2>
      </div>

      <div className="p-6">
        <div className="relative bg-gradient-to-br from-green-50 to-blue-50 rounded-lg h-80 p-4 border overflow-hidden">
          <div className="text-center text-sm font-medium text-gray-700 mb-4">
            University Campus - Real-time Device Locations
          </div>

          {/* Buildings */}
          <div className="absolute top-12 left-6 w-16 h-12 bg-blue-200 rounded-lg shadow-sm border border-blue-300 flex items-center justify-center text-xs">
            <div className="text-center">
              <div className="text-xs font-medium">Library</div>
              <div className="text-xs text-blue-600">Academic</div>
            </div>
          </div>

          <div className="absolute top-12 right-6 w-16 h-12 bg-purple-200 rounded-lg shadow-sm border border-purple-300 flex items-center justify-center text-xs">
            <div className="text-center">
              <div className="text-xs font-medium">Lab</div>
              <div className="text-xs text-purple-600">Research</div>
            </div>
          </div>

          <div className="absolute bottom-16 left-6 w-16 h-12 bg-red-200 rounded-lg shadow-sm border border-red-300 flex items-center justify-center text-xs">
            <div className="text-center">
              <div className="text-xs font-medium">Admin</div>
              <div className="text-xs text-red-600">Restricted</div>
            </div>
          </div>

          <div className="absolute bottom-16 right-6 w-16 h-12 bg-green-200 rounded-lg shadow-sm border border-green-300 flex items-center justify-center text-xs">
            <div className="text-center">
              <div className="text-xs font-medium">Student</div>
              <div className="text-xs text-green-600">Social</div>
            </div>
          </div>

          {/* Off-campus */}
          <div className="absolute top-6 left-1/2 transform -translate-x-1/2 w-14 h-8 bg-orange-200 rounded shadow-sm border border-orange-300 flex items-center justify-center text-xs">
            <div className="text-xs font-medium">Off-Campus</div>
          </div>

          {/* Markers */}
          {deviceLocations.map((device, idx) => {
            const pos = positionsPreset[idx] || positionsPreset[0];
            const isCritical = device.riskLevel === 'CRITICAL';
            const isHigh = device.riskLevel === 'HIGH';
            const isMedium = device.riskLevel === 'MEDIUM';
            const markerClass = isCritical ? 'bg-red-500 animate-pulse' : isHigh ? 'bg-orange-500' : isMedium ? 'bg-yellow-500' : 'bg-green-500';

            return (
              <div key={device.deviceId} className="absolute" style={{ left: pos.left, top: pos.top }}>
                <div 
                  className={`relative w-5 h-5 rounded-full border-2 border-white shadow-lg transform transition-all hover:scale-110 ${markerClass}`} 
                  title={`${device.deviceId} - Trust: ${device.trustScore}`} 
                />
                {isCritical && <div className="absolute inset-0 rounded-full bg-red-500 animate-ping opacity-60" />}
              </div>
            );
          })}

          {/* SVG lines for HIGH/CRITICAL */}
          <svg className="absolute inset-0 w-full h-full pointer-events-none">
            {deviceLocations
              .map((d, i) => ({ d, i }))
              .filter(({ d }) => d.riskLevel === 'HIGH' || d.riskLevel === 'CRITICAL')
              .map(({ d, i }, idx) => {
                const end = positionsPreset[i] || positionsPreset[0];
                return (
                  <line
                    key={idx}
                    x1="50%"
                    y1="10%"
                    x2={end.left}
                    y2={end.top}
                    stroke="#ef4444"
                    strokeWidth="1"
                    strokeDasharray="4,3"
                    opacity="0.6"
                  />
                );
              })}
          </svg>
        </div>

        {/* Legend */}
        <div className="mt-4 space-y-3">
          <div className="text-sm font-medium text-gray-700">Device Risk Levels</div>
          <div className="grid grid-cols-2 gap-2 text-xs">
            <div className="flex items-center space-x-2">
              <div className="w-3 h-3 bg-green-500 rounded-full" /> 
              <span>Low Risk</span>
            </div>
            <div className="flex items-center space-x-2">
              <div className="w-3 h-3 bg-yellow-500 rounded-full" /> 
              <span>Medium Risk</span>
            </div>
            <div className="flex items-center space-x-2">
              <div className="w-3 h-3 bg-orange-500 rounded-full" /> 
              <span>High Risk</span>
            </div>
            <div className="flex items-center space-x-2">
              <div className="w-3 h-3 bg-red-500 rounded-full animate-pulse" /> 
              <span>Critical</span>
            </div>
          </div>

          <div className="text-sm font-medium text-gray-700 pt-2">Location Types</div>
          <div className="grid grid-cols-1 gap-1 text-xs">
            <div className="flex items-center justify-between">
              <span className="px-2 py-1 bg-blue-100 text-blue-800 rounded">Academic</span>
              <span className="text-gray-600">Libraries, Classrooms</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="px-2 py-1 bg-purple-100 text-purple-800 rounded">Lab</span>
              <span className="text-gray-600">Research Labs</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="px-2 py-1 bg-red-100 text-red-800 rounded">Restricted</span>
              <span className="text-gray-600">Admin Areas</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="px-2 py-1 bg-orange-100 text-orange-800 rounded">External</span>
              <span className="text-gray-600">Off-Campus</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}