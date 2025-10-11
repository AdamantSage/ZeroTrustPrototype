// src/components/Analytics/TrustAnalysisPanel.js
import React from 'react';

const h = React.createElement;

export function TrustAnalysisPanel({ data }) {
  if (!data) return null;

  return h('div', { className: 'bg-white rounded-lg shadow-sm border' },
    h('div', { className: 'p-4 border-b' },
      h('h3', { className: 'text-lg font-semibold' }, '📈 Trust Analysis')
    ),

    h('div', { className: 'p-4' },
      // Trust Status
      h('div', { className: 'mb-4 flex items-center justify-between' },
        h('span', { className: 'text-sm font-medium' }, 'Trust Status'),
        h('span', { 
          className: `px-2 py-1 rounded text-sm ${
            data.isTrusted ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
          }`
        }, data.isTrusted ? '✅ Trusted' : '❌ Untrusted')
      ),

      // Historical Context
      data.averageTrustScore7Days !== undefined && h('div', { className: 'mb-4' },
        h('h4', { className: 'text-sm font-medium mb-2' }, 'Historical Averages'),
        h('div', { className: 'grid grid-cols-2 gap-4 text-sm' },
          h('div', null,
            h('div', { className: 'text-gray-600' }, '7 Days'),
            h('div', { className: 'font-semibold' }, Math.round(data.averageTrustScore7Days || 0))
          ),
          h('div', null,
            h('div', { className: 'text-gray-600' }, '30 Days'),
            h('div', { className: 'font-semibold' }, Math.round(data.averageTrustScore30Days || 0))
          )
        )
      ),

      // Trend Direction
      data.trendDirection && h('div', { className: 'mb-4' },
        h('div', { className: 'flex items-center justify-between' },
          h('span', { className: 'text-sm font-medium' }, 'Trend'),
          h('span', { 
            className: `px-2 py-1 rounded text-sm ${
              data.trendDirection === 'IMPROVING' ? 'bg-green-100 text-green-700' :
              data.trendDirection === 'DEGRADING' ? 'bg-red-100 text-red-700' :
              'bg-blue-100 text-blue-700'
            }`
          }, 
            (data.trendDirection === 'IMPROVING' ? '📈 ' : 
             data.trendDirection === 'DEGRADING' ? '📉 ' : '➡️ ') + 
            data.trendDirection
          )
        )
      ),

      // Risk Indicators
      data.riskIndicators && data.riskIndicators.length > 0 && h('div', { className: 'mb-4' },
        h('h4', { className: 'text-sm font-medium mb-2 text-red-700' }, 'Risk Indicators'),
        h('ul', { className: 'text-sm space-y-1' },
          ...data.riskIndicators.slice(0, 3).map((indicator, idx) =>
            h('li', { key: idx, className: 'text-red-600 flex items-start' },
              h('span', { className: 'mr-2' }, '⚠️'),
              indicator
            )
          )
        )
      ),

      // Positive Indicators
      data.positiveIndicators && data.positiveIndicators.length > 0 && h('div', null,
        h('h4', { className: 'text-sm font-medium mb-2 text-green-700' }, 'Positive Indicators'),
        h('ul', { className: 'text-sm space-y-1' },
          ...data.positiveIndicators.slice(0, 3).map((indicator, idx) =>
            h('li', { key: idx, className: 'text-green-600 flex items-start' },
              h('span', { className: 'mr-2' }, '✅'),
              indicator
            )
          )
        )
      )
    )
  );
}