// src/components/Analytics/EnhancedLocationMap.js
import React from 'react';
import analyticsService from '../../services/analyticsService';

const h = React.createElement;

export function EnhancedLocationMap({ data, onDeviceSelect, selectedDevice }) {
  if (!data || !data.deviceLocations) return null;

  const locations = data.deviceLocations || [];
  const locationGroups = locations.reduce((acc, device) => {
    const loc = device.location || 'Unknown';
    if (!acc[loc]) acc[loc] = [];
    acc[loc].push(device);
    return acc;
  }, {});

  return h('div', { className: 'bg-white rounded-lg shadow-sm border' },
    h('div', { className: 'p-4 border-b' },
      h('h3', { className: 'text-lg font-semibold' }, '🗺️ Enhanced Location Map')
    ),

    h('div', { className: 'p-4' },
      h('div', { className: 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4' },
        ...Object.entries(locationGroups).map(([location, devices]) => {
          const riskCounts = devices.reduce((acc, d) => {
            acc[d.riskLevel || 'LOW'] = (acc[d.riskLevel || 'LOW'] || 0) + 1;
            return acc;
          }, {});
          
          const highestRisk = riskCounts.CRITICAL ? 'CRITICAL' :
                             riskCounts.HIGH ? 'HIGH' :
                             riskCounts.MEDIUM ? 'MEDIUM' : 'LOW';

          return h('div', { 
            key: location, 
            className: `border rounded-lg p-4 ${analyticsService.getRiskLevelColor(highestRisk)}`
          },
            h('div', { className: 'font-medium mb-2' }, location),
            h('div', { className: 'text-sm text-gray-600 mb-3' }, 
              `${devices.length} device${devices.length !== 1 ? 's' : ''}`
            ),
            h('div', { className: 'space-y-1' },
              ...devices.slice(0, 3).map(device =>
                h('div', { 
                  key: device.deviceId,
                  className: `text-xs p-2 bg-white bg-opacity-50 rounded cursor-pointer hover:bg-opacity-75 ${
                    selectedDevice === device.deviceId ? 'ring-2 ring-blue-500' : ''
                  }`,
                  onClick: () => onDeviceSelect(device.deviceId)
                },
                  h('div', { className: 'flex items-center justify-between' },
                    h('span', { className: 'font-medium' }, device.deviceId),
                    h('span', { className: analyticsService.getTrustScoreColor(device.trustScore) }, 
                      Math.round(device.trustScore || 0)
                    )
                  ),
                  device.activeThreats > 0 && h('div', { className: 'text-red-600' }, 
                    `${device.activeThreats} threat${device.activeThreats !== 1 ? 's' : ''}`
                  )
                )
              ),
              devices.length > 3 && h('div', { className: 'text-xs text-gray-500 text-center' },
                `+${devices.length - 3} more...`
              )
            )
          );
        })
      )
    )
  );
}