import React from 'react';

export default function SummaryCards(props) {
  const h = React.createElement;
  const summary = props.auditSummary;
  if (!summary) return h('div', null);

  return h('div', { className: 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6' },
    h('div', { className: 'bg-white rounded-lg shadow p-6' },
      h('div', { className: 'flex items-center' },
        h('div', { className: 'flex-shrink-0' }, h('span', { className: 'text-2xl' }, '🚨')),
        h('div', { className: 'ml-4' },
          h('p', { className: 'text-sm font-medium text-gray-500' }, 'Quarantine Actions'),
          h('p', { className: 'text-2xl font-semibold text-gray-900' }, String(summary.totalQuarantineActions || 0))
        )
      )
    ),

    h('div', { className: 'bg-white rounded-lg shadow p-6' },
      h('div', { className: 'flex items-center' },
        h('div', { className: 'flex-shrink-0' }, h('span', { className: 'text-2xl' }, '🌍')),
        h('div', { className: 'ml-4' },
          h('p', { className: 'text-sm font-medium text-gray-500' }, 'Location Changes'),
          h('p', { className: 'text-2xl font-semibold text-gray-900' }, String(summary.totalLocationChanges || 0))
        )
      )
    ),

    h('div', { className: 'bg-white rounded-lg shadow p-6' },
      h('div', { className: 'flex items-center' },
        h('div', { className: 'flex-shrink-0' }, h('span', { className: 'text-2xl' }, '💾')),
        h('div', { className: 'ml-4' },
          h('p', { className: 'text-sm font-medium text-gray-500' }, 'Firmware Checks'),
          h('p', { className: 'text-2xl font-semibold text-gray-900' }, String(summary.totalFirmwareChecks || 0))
        )
      )
    ),

    h('div', { className: 'bg-white rounded-lg shadow p-6' },
      h('div', { className: 'flex items-center' },
        h('div', { className: 'flex-shrink-0' }, h('span', { className: 'text-2xl' }, '⚠️')),
        h('div', { className: 'ml-4' },
          h('p', { className: 'text-sm font-medium text-gray-500' }, 'Devices with Issues'),
          h('p', { className: 'text-2xl font-semibold text-red-600' }, String(summary.devicesWithIssues || 0))
        )
      )
    )
  );
}
