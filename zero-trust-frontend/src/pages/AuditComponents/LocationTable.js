import React from 'react';
import StatusBadge from './StatusBadge';

export default function LocationTable(props) {
  const h = React.createElement;
  const rows = props.locationChanges || [];
  const auditService = props.auditService;

  return h('div', { className: 'bg-white rounded-lg shadow overflow-hidden' },
    h('div', { className: 'px-6 py-4 border-b border-gray-200 flex justify-between items-center' },
      h('div', null,
        h('h3', { className: 'text-lg font-semibold text-gray-900' }, 'Location & Network Changes'),
        h('p', { className: 'text-sm text-gray-600' }, 'Track device movement and IP address changes')
      ),
      h('button', { onClick: function() { props.handleExport && props.handleExport('location-changes'); }, className: 'px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 text-sm' }, 'Export CSV')
    ),

    h('div', { className: 'overflow-x-auto' },
      h('table', { className: 'min-w-full divide-y divide-gray-200' }, [
        h('thead', { className: 'bg-gray-50', key: 'thead' },
          h('tr', null,
            ['Device ID','Old Location','New Location','Old IP','New IP','Timestamp'].map(function(col) {
              return h('th', { key: col, className: 'px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider' }, col);
            })
          )
        ),

        h('tbody', { className: 'bg-white divide-y divide-gray-200', key: 'tbody' },
          rows.length === 0 ? h('tr', null, h('td', { colSpan: 6, className: 'text-center py-8 text-gray-500' }, 'No location or network changes recorded')) :
          rows.map(function(change, index) {
            return h('tr', { key: change.id || index, className: 'hover:bg-gray-50' }, [
              h('td', { className: 'px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900' }, change.deviceId),
              h('td', { className: 'px-6 py-4 whitespace-nowrap text-sm text-gray-500' }, change.oldLocation || '—'),
              h('td', { className: 'px-6 py-4 whitespace-nowrap text-sm text-gray-500' }, change.newLocation || '—'),
              h('td', { className: 'px-6 py-4 whitespace-nowrap text-sm text-gray-500 font-mono' }, change.oldIpAddress || '—'),
              h('td', { className: 'px-6 py-4 whitespace-nowrap text-sm text-gray-500 font-mono' }, change.newIpAddress || '—'),
              h('td', { className: 'px-6 py-4 whitespace-nowrap text-sm text-gray-500' }, auditService && auditService.formatTimestamp ? auditService.formatTimestamp(change.timestamp) : String(change.timestamp || '—'))
            ]);
          })
        )
      ])
    )
  );
}
