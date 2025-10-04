import React from 'react';

export default function Filters(props) {
  const h = React.createElement;
  const devices = props.devices || [];
  const selectedDevice = props.selectedDevice || '';

  return h('div', { className: 'bg-white rounded-lg shadow p-4 mb-6' },
    h('div', { className: 'flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4' },
      h('div', { className: 'flex items-center gap-4' },
        h('label', { htmlFor: 'device-filter', className: 'text-sm font-medium text-gray-700' }, 'Filter by Device:'),
        h('select', {
          id: 'device-filter',
          value: selectedDevice,
          onChange: function(e) { props.onDeviceChange && props.onDeviceChange(e.target.value); },
          className: 'border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500'
        },
          h('option', { value: '' }, 'All Devices'),
          devices.map(function(device) {
            return h('option', { key: device.deviceId, value: device.deviceId }, device.deviceId);
          })
        )
      ),

      h('div', { className: 'text-sm text-gray-500' }, selectedDevice ? `Showing data for: ${selectedDevice}` : 'Showing all devices')
    )
  );
}
