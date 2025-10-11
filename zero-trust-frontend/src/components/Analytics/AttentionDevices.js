// src/components/Analytics/AttentionDevices.js
import React from 'react';
import analyticsService from '../../services/analyticsService';

const h = React.createElement;

export function AttentionDevices({ devices, onDeviceSelect }) {
  if (!devices || devices.length === 0) return null;

  return h('div', { className: 'bg-white rounded-lg shadow-sm border' },
    h('div', { className: 'p-4 border-b' },
      h('h3', { className: 'text-lg font-semibold flex items-center' },
        '🚨 Devices Requiring Attention'
      )
    ),

    h('div', { className: 'p-4' },
      h('div', { className: 'space-y-3' },
        ...devices.slice(0, 8).map(device =>
          h('div', { 
            key: device.deviceId, 
            className: 'flex items-center justify-between p-3 bg-gray-50 rounded-lg hover:bg-gray-100 cursor-pointer transition-colors',
            onClick: () => onDeviceSelect(device.deviceId)
          },
            h('div', { className: 'flex items-center space-x-3' },
              h('div', { 
                className: `w-3 h-3 rounded-full ${
             device.urgency === 'URGENT' ? 'bg-red-500' :
                  device.urgency === 'HIGH' ? 'bg-orange-500' :
                  device.urgency === 'MEDIUM' ? 'bg-yellow-500' : 'bg-green-500'
                }`
              }),
              h('div', null,
                h('div', { className: 'font-medium text-sm' }, device.deviceId),
                h('div', { className: 'text-xs text-gray-600' },
                  `${device.primaryIssues?.length || 0} issues • Trust: ${Math.round(device.trustScore || 0)}`
                )
              )
            ),
            h('div', { className: 'flex items-center space-x-2' },
              h('span', { 
                className: `text-xs px-2 py-1 rounded ${analyticsService.getRiskLevelColor(device.riskLevel)}`
              }, device.riskLevel),
              device.isQuarantined && h('span', { className: 'text-xs px-2 py-1 bg-red-100 text-red-700 rounded' }, 'QUARANTINED')
            )
          )
        )
      )
    )
  );
}