// src/components/Analytics/DeviceRiskPanel.js
import React from 'react';
import analyticsService from '../../services/analyticsService';

const h = React.createElement;

export function DeviceRiskPanel({ data, onResetTrustScore }) {
  if (!data) return null;

  const handleResetTrust = () => {
    const baselineScore = prompt('Enter baseline trust score (0-100):', '50');
    if (baselineScore && !isNaN(baselineScore)) {
      onResetTrustScore(data.deviceId, parseFloat(baselineScore));
    }
  };

  return h('div', { className: 'bg-white rounded-lg shadow-sm border' },
    h('div', { className: 'p-4 border-b' },
      h('div', { className: 'flex items-center justify-between' },
        h('h3', { className: 'text-lg font-semibold flex items-center' },
          '🛡️ Risk Assessment'
        ),
        h('button', {
          onClick: handleResetTrust,
          className: 'text-sm px-3 py-1 bg-gray-100 text-gray-700 rounded hover:bg-gray-200',
          title: 'Reset trust score to baseline'
        }, '🔄 Reset')
      )
    ),

    h('div', { className: 'p-4' },
      // Risk Level Badge
      h('div', { className: 'mb-4' },
        h('div', { 
          className: `inline-flex items-center px-3 py-1 rounded-full text-sm font-medium border ${
            analyticsService.getRiskLevelColor(data.riskLevel)
          }`
        },
          h('span', { className: 'mr-1' }, analyticsService.getRiskLevelIcon(data.riskLevel)),
          `${data.riskLevel} RISK`
        )
      ),

      // Trust Score
      h('div', { className: 'mb-4' },
        h('div', { className: 'flex items-center justify-between mb-2' },
          h('span', { className: 'text-sm font-medium' }, 'Current Trust Score'),
          h('span', { 
            className: `text-lg font-bold ${analyticsService.getTrustScoreColor(data.currentTrustScore)}`
          }, `${Math.round(data.currentTrustScore)}/100`)
        ),
        h('div', { className: 'w-full bg-gray-200 rounded-full h-2' },
          h('div', {
            className: `h-2 rounded-full ${data.currentTrustScore >= 70 ? 'bg-green-500' : 
              data.currentTrustScore >= 50 ? 'bg-yellow-500' : 'bg-red-500'}`,
            style: { width: `${Math.max(5, data.currentTrustScore)}%` }
          })
        )
      ),

      // Risk Factors
      data.riskFactors && h('div', { className: 'mb-4' },
        h('h4', { className: 'text-sm font-medium mb-2' }, 'Risk Factors'),
        h('div', { className: 'space-y-2' },
          ...Object.entries(data.riskFactors).map(([factor, status]) =>
            h('div', { key: factor, className: 'flex items-center justify-between' },
              h('span', { className: 'text-sm capitalize' }, factor.replace(/([A-Z])/g, ' $1')),
              h('span', { 
                className: `text-xs px-2 py-1 rounded ${
                  status === 'HIGH_RISK' ? 'bg-red-100 text-red-700' :
                  status === 'MEDIUM_RISK' ? 'bg-yellow-100 text-yellow-700' :
                  status === 'LOW_RISK' ? 'bg-green-100 text-green-700' :
                  'bg-gray-100 text-gray-700'
                }`
              }, status.replace('_', ' '))
            )
          )
        )
      ),

      // Active Threats
      data.activeThreats && data.activeThreats.length > 0 && h('div', { className: 'mb-4' },
        h('h4', { className: 'text-sm font-medium mb-2 text-red-700' }, '🚨 Active Threats'),
        h('ul', { className: 'text-sm space-y-1' },
          ...data.activeThreats.map((threat, idx) =>
            h('li', { key: idx, className: 'text-red-600 flex items-start' },
              h('span', { className: 'mr-2' }, '•'),
              threat
            )
          )
        )
      ),

      // Recommendations
      data.recommendations && data.recommendations.length > 0 && h('div', null,
        h('h4', { className: 'text-sm font-medium mb-2' }, '💡 Recommendations'),
        h('ul', { className: 'text-sm space-y-1' },
          ...data.recommendations.slice(0, 3).map((rec, idx) =>
            h('li', { key: idx, className: 'text-gray-700 flex items-start' },
              h('span', { className: 'mr-2 text-blue-500' }, '•'),
              rec
            )
          )
        )
      )
    )
  );
}