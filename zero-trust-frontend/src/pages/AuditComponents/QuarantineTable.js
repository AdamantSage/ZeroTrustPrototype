import React from 'react';
import StatusBadge from './StatusBadge';

export default function QuarantineTable(props) {
  const h = React.createElement;
  const rows = props.quarantineHistory || [];

  return h('div', { className: 'bg-white rounded-lg shadow overflow-hidden' },
    h('div', { className: 'px-6 py-4 border-b border-gray-200 flex justify-between items-center' },
      h('div', null,
        h('h3', { className: 'text-lg font-semibold text-gray-900' }, 'Quarantine History'),
        h('p', { className: 'text-sm text-gray-600' }, 'Complete audit trail of security actions')
      ),
      h('button', { onClick: function() { props.handleExport && props.handleExport('quarantine-history'); }, className: 'px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 text-sm' }, 'Export CSV')
    ),

    h('div', { className: 'overflow-x-auto' },
      h('table', { className: 'min-w-full divide-y divide-gray-200' }, [
        h('thead', { className: 'bg-gray-50', key: 'thead' },
          h('tr', null,
            ['Device ID','Reason','Status','Error Message','Timestamp'].map(function(col) {
              return h('th', { key: col, className: 'px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider' }, col);
            })
          )
        ),

        h('tbody', { className: 'bg-white divide-y divide-gray-200', key: 'tbody' },
          rows.length === 0 ? h('tr', null, h('td', { colSpan: 5, className: 'text-center py-8 text-gray-500' }, 'No quarantine actions recorded')) :
          rows.map(function(log, index) {
            return h('tr', { key: log.id || index, className: 'hover:bg-gray-50' }, [
              h('td', { className: 'px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900' }, log.deviceId),
              h('td', { className: 'px-6 py-4 text-sm text-gray-500' }, log.reason),
              h('td', { className: 'px-6 py-4 whitespace-nowrap' }, h(StatusBadge, { status: log.status })),
              h('td', { className: 'px-6 py-4 text-sm text-gray-500' }, log.errorMessage || '—'),
              h('td', { className: 'px-6 py-4 whitespace-nowrap text-sm text-gray-500' }, props.auditService && props.auditService.formatTimestamp ? props.auditService.formatTimestamp(log.timestamp) : String(log.timestamp || '—'))
            ]);
          })
        )
      ])
    )
  );
}