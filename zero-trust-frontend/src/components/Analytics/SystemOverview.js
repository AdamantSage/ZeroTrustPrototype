// src/components/Analytics/SystemOverview.js
import React from 'react';

const h = React.createElement;

export function SystemOverview({ data, onDeviceSelect }) {
  if (!data) return null;

  const riskStats = data.riskDistribution || {};
  const processingStats = data.processingStats || {};
  const trustStats = data.trustStats || {};

  return h('div', { className: 'bg-white rounded-lg shadow-sm border p-6 mb-6' },
    h('h2', { className: 'text-xl font-semibold mb-4 flex items-center' },
      '🎯 System Risk Overview'
    ),
    
    // Risk Distribution Cards
    h('div', { className: 'grid grid-cols-1 md:grid-cols-4 gap-4 mb-6' },
      h('div', { className: 'bg-red-50 border border-red-200 rounded-lg p-4' },
        h('div', { className: 'text-2xl font-bold text-red-600' }, riskStats.CRITICAL || 0),
        h('div', { className: 'text-sm text-red-700' }, 'Critical Risk'),
        h('div', { className: 'text-xs text-red-600' }, '🚨 Immediate Action')
      ),
      h('div', { className: 'bg-orange-50 border border-orange-200 rounded-lg p-4' },
        h('div', { className: 'text-2xl font-bold text-orange-600' }, riskStats.HIGH || 0),
        h('div', { className: 'text-sm text-orange-700' }, 'High Risk'),
        h('div', { className: 'text-xs text-orange-600' }, '⚠️ Review Soon')
      ),
      h('div', { className: 'bg-yellow-50 border border-yellow-200 rounded-lg p-4' },
        h('div', { className: 'text-2xl font-bold text-yellow-600' }, riskStats.MEDIUM || 0),
        h('div', { className: 'text-sm text-yellow-700' }, 'Medium Risk'),
        h('div', { className: 'text-xs text-yellow-600' }, '⚡ Monitor Closely')
      ),
      h('div', { className: 'bg-green-50 border border-green-200 rounded-lg p-4' },
        h('div', { className: 'text-2xl font-bold text-green-600' }, riskStats.LOW || 0),
        h('div', { className: 'text-sm text-green-700' }, 'Low Risk'),
        h('div', { className: 'text-xs text-green-600' }, '✅ Operating Normally')
      )
    ),

    // System Health Metrics
    h('div', { className: 'grid grid-cols-1 md:grid-cols-3 gap-4' },
      h('div', { className: 'bg-gray-50 rounded-lg p-4' },
        h('div', { className: 'text-lg font-semibold' }, 'System Health'),
        h('div', { className: 'text-2xl font-bold text-blue-600' }, 
          `${Math.round(data.systemHealthScore || 0)}%`
        ),
        h('div', { className: 'text-xs text-gray-600' }, 'Average Trust Score')
      ),
      h('div', { className: 'bg-gray-50 rounded-lg p-4' },
        h('div', { className: 'text-lg font-semibold' }, 'Total Devices'),
        h('div', { className: 'text-2xl font-bold text-purple-600' }, data.totalDevices || 0),
        h('div', { className: 'text-xs text-gray-600' }, 
          `${trustStats.trustedDevices || 0} trusted, ${trustStats.quarantinedDevices || 0} quarantined`
        )
      ),
      h('div', { className: 'bg-gray-50 rounded-lg p-4' },
        h('div', { className: 'text-lg font-semibold' }, 'Recent Issues'),
        h('div', { className: 'text-2xl font-bold text-red-600' }, 
          (data.devicesWithRecentIssues || []).length
        ),
        h('div', { className: 'text-xs text-gray-600' }, 'Devices with concerning changes')
      )
    )
  );
}