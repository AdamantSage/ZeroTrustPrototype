// src/components/Analytics/RecentChangesPanel.js
import React from 'react';
import analyticsService from '../../services/analyticsService';

const h = React.createElement;

export function RecentChangesPanel({ data }) {
  if (!data) return null;

  const trustChanges = data.trustChanges || {};
  const locationChanges = data.locationChanges || [];
  const locationStats = data.locationStatistics || {};

  return h('div', { className: 'bg-white rounded-lg shadow-sm border' },
    h('div', { className: 'p-4 border-b' },
      h('h3', { className: 'text-lg font-semibold' }, '🔄 Recent Changes')
    ),

    h('div', { className: 'p-4 space-y-4' },
      // Trust Changes Summary
      trustChanges.totalChanges > 0 && h('div', null,
        h('h4', { className: 'text-sm font-medium mb-2' }, 'Trust Score Changes'),
        h('div', { className: 'grid grid-cols-2 gap-4 text-sm' },
          h('div', null,
            h('div', { className: 'text-gray-600' }, 'Total Changes'),
            h('div', { className: 'font-semibold' }, trustChanges.totalChanges)
          ),
          h('div', null,
            h('div', { className: 'text-gray-600' }, 'Net Change'),
            h('div', { 
              className: `font-semibold ${trustChanges.netScoreChange >= 0 ? 'text-green-600' : 'text-red-600'}`
            }, `${trustChanges.netScoreChange >= 0 ? '+' : ''}${Math.round(trustChanges.netScoreChange * 10) / 10}`)
          )
        )
      ),

      // Location Changes
      locationChanges.length > 0 && h('div', null,
        h('h4', { className: 'text-sm font-medium mb-2' }, 'Location Changes'),
        h('div', { className: 'space-y-1 max-h-32 overflow-y-auto' },
          ...locationChanges.slice(0, 5).map((change, idx) =>
            h('div', { key: idx, className: 'text-xs bg-gray-50 p-2 rounded' },
              h('div', { className: 'font-medium' }, change.location || 'Unknown Location'),
              h('div', { className: 'text-gray-600' }, 
                analyticsService.formatTimestamp(change.timestamp)
              )
            )
          )
        )
      ),

      // Location Statistics
      locationStats.totalLocations && h('div', null,
        h('h4', { className: 'text-sm font-medium mb-2' }, 'Location Statistics'),
        h('div', { className: 'text-sm space-y-1' },
          h('div', { className: 'flex justify-between' },
            h('span', null, 'Total Locations'),
            h('span', { className: 'font-medium' }, locationStats.totalLocations)
          ),
          locationStats.mostFrequentLocation && h('div', { className: 'flex justify-between' },
            h('span', null, 'Most Frequent'),
            h('span', { className: 'font-medium' }, locationStats.mostFrequentLocation)
          )
        )
      ),

      // Factor Analysis
      trustChanges.factorImpacts && h('div', null,
        h('h4', { className: 'text-sm font-medium mb-2' }, 'Impact Factors'),
        h('div', { className: 'space-y-1' },
          ...Object.entries(trustChanges.factorImpacts)
            .filter(([_, count]) => count > 0)
            .slice(0, 3)
            .map(([factor, count]) =>
              h('div', { key: factor, className: 'flex justify-between text-sm' },
                h('span', { className: 'capitalize' }, factor.replace(/([A-Z])/g, ' $1')),
                h('span', { className: 'text-red-600 font-medium' }, count)
              )
            )
        )
      )
    )
  );
}