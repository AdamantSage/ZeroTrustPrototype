// src/components/Enhanced/DeviceList.js
import React from 'react';
import { Monitor, Lock, MapPin, Network, Clock, Target } from 'lucide-react';

export default function DeviceList({ 
  devices, 
  onDeviceClick, 
  onTrustScoreClick, 
  getStatusColor, 
  getRiskColor, 
  getLocationTypeColor 
}) {
  return (
    <div className="border rounded-lg">
      <div className="p-4 border-b flex items-center justify-between">
        <div className="font-medium">Devices ({devices.length})</div>
        <div className="text-sm text-gray-500">Click a device to open details</div>
      </div>

      <div className="divide-y">
        {devices.map(device => (
          <div 
            key={device.deviceId} 
            className="p-4 hover:bg-gray-50 cursor-pointer flex items-start justify-between" 
            onClick={() => onDeviceClick(device)}
          >
            <div className="flex items-start space-x-4 min-w-0">
              <div className="relative">
                <Monitor className="w-10 h-10 text-gray-600" />
                {(device.status === 'QUARANTINED' || device.quarantined) && (
                  <Lock className="w-4 h-4 text-red-600 absolute -top-1 -right-1 bg-white rounded-full" />
                )}
              </div>
              
              <div className="min-w-0">
                <div className="font-semibold text-gray-900 truncate">
                  {device.name || device.deviceId}
                </div>
                
                <div className="flex items-center space-x-2 mt-1 text-xs text-gray-600">
                  <span className={`px-2 py-0.5 rounded-full border ${getStatusColor(device.status || (device.trusted ? 'TRUSTED' : 'SUSPICIOUS'))}`}>
                    {device.status || (device.trusted ? 'TRUSTED' : 'SUSPICIOUS')}
                  </span>
                  {device.location && (
                    <span className={`px-2 py-0.5 rounded-full ${getLocationTypeColor(device.locationType || '')}`}>
                      {(device.location || '').replace(/-/g, ' ')}
                    </span>
                  )}
                </div>

                <div className="flex items-center space-x-4 mt-2 text-xs text-gray-600">
                  <div className="flex items-center">
                    <MapPin className="w-4 h-4 mr-1" />
                    {device.location || '–'}
                  </div>
                  <div className="flex items-center">
                    <Network className="w-4 h-4 mr-1" />
                    {device.ipAddress || '–'}
                  </div>
                  <div className="flex items-center">
                    <Clock className="w-4 h-4 mr-1" />
                    {device.lastSeen ? new Date(device.lastSeen).toLocaleTimeString() : '–'}
                  </div>
                </div>
              </div>
            </div>

            <div className="text-right space-y-2">
              <div 
                onClick={(e) => { 
                  e.stopPropagation(); 
                  onTrustScoreClick(device); 
                }} 
                className="p-2 rounded-lg hover:bg-gray-100 cursor-pointer"
              >
                <div className={`text-2xl font-bold ${
                  device.trustScore >= 70 
                    ? 'text-green-600' 
                    : device.trustScore >= 50 
                    ? 'text-yellow-600' 
                    : 'text-red-600'
                }`}>
                  {Math.round(device.trustScore || 0)}
                </div>
                <div className="text-xs text-gray-500">Trust Score</div>
              </div>
              
              <div className="flex items-center justify-end space-x-2 text-xs">
                <div className={`${getRiskColor(device.riskLevel)} flex items-center`}>
                  <Target className="w-3 h-3 mr-1" />
                  {device.riskLevel || 'N/A'}
                </div>
                <div className="text-gray-600">
                  {Math.round((device.anomalyScore || 0) * 100)}%
                </div>
              </div>
            </div>
          </div>
        ))}

        {devices.length === 0 && (
          <div className="p-6 text-center text-sm text-gray-500">
            No devices match the filter
          </div>
        )}
      </div>
    </div>
  );
}