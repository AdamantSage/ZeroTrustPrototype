// src/components/Analytics/TimelineChart.js
import React from 'react';
import analyticsService from '../../services/analyticsService';

const h = React.createElement;

export function TimelineChart({ data, deviceId, onRefresh }) {
  // Get current timestamp for "last updated" display
  const [lastUpdate, setLastUpdate] = React.useState(new Date());
  
  // Auto-refresh effect
  React.useEffect(() => {
    if (!deviceId || !onRefresh) return;
    
    // Refresh every 5 seconds
    const interval = setInterval(() => {
      onRefresh();
      setLastUpdate(new Date());
    }, 5000);
    
    return () => clearInterval(interval);
  }, [deviceId, onRefresh]);
  
  // Manual refresh handler
  const handleManualRefresh = () => {
    if (onRefresh) {
      onRefresh();
      setLastUpdate(new Date());
    }
  };
  
  if (!data || data.length === 0) {
    return h('div', { className: 'bg-white rounded-lg shadow-sm border' },
      h('div', { className: 'p-4 border-b flex items-center justify-between' },
        h('h3', { className: 'text-lg font-semibold' }, '📊 Trust Score Timeline'),
        h('button', {
          onClick: handleManualRefresh,
          className: 'text-sm px-3 py-1 bg-blue-50 text-blue-600 rounded hover:bg-blue-100',
          title: 'Refresh timeline'
        }, '🔄 Refresh')
      ),
      h('div', { className: 'p-8 text-center text-gray-500' },
        h('p', null, 'No trust score history available yet.'),
        h('p', { className: 'text-sm mt-2' }, 'Timeline will update as telemetry is processed.'),
        h('p', { className: 'text-xs mt-2 text-gray-400' }, 
          `Auto-refreshing every 5 seconds • Last: ${lastUpdate.toLocaleTimeString()}`
        )
      )
    );
  }

  // Calculate statistics
  const maxScore = Math.max(...data.map(d => d.trustScore));
  const minScore = Math.min(...data.map(d => d.trustScore));
  
  // Calculate trend from recent data
  const recentData = data.slice(0, Math.min(5, data.length));
  const avgChange = recentData.length > 1 
    ? recentData.reduce((sum, point) => sum + (point.scoreChange || 0), 0) / recentData.length
    : 0;
  const trendIndicator = avgChange > 1 ? '📈 Improving' : avgChange < -1 ? '📉 Declining' : '➡️ Stable';

  return h('div', { className: 'bg-white rounded-lg shadow-sm border' },
    h('div', { className: 'p-4 border-b flex items-center justify-between' },
      h('div', { className: 'flex items-center space-x-3' },
        h('h3', { className: 'text-lg font-semibold' }, '📊 Trust Score Timeline'),
        h('span', { 
          className: `text-sm px-3 py-1 rounded ${
            avgChange > 1 ? 'bg-green-100 text-green-700' :
            avgChange < -1 ? 'bg-red-100 text-red-700' :
            'bg-blue-100 text-blue-700'
          }`
        }, trendIndicator)
      ),
      h('div', { className: 'flex items-center space-x-2' },
        h('span', { className: 'text-xs text-gray-500' }, 
          `Updated: ${lastUpdate.toLocaleTimeString()}`
        ),
        h('button', {
          onClick: handleManualRefresh,
          className: 'text-sm px-3 py-1 bg-blue-50 text-blue-600 rounded hover:bg-blue-100 transition-colors',
          title: 'Refresh timeline now'
        }, '🔄')
      )
    ),

    h('div', { className: 'p-4' },
      // Timeline visualization
      h('div', { className: 'relative mb-4' },
        h('div', { className: 'space-y-2 max-h-96 overflow-y-auto' },
          ...data.slice(0, 20).map((point, idx) => {
            const isImprovement = point.scoreChange && point.scoreChange > 0;
            const isDegradation = point.scoreChange && point.scoreChange < 0;
            const isHealthy = point.eventType && point.eventType.includes('HEALTHY');
            const changeAbs = Math.abs(point.scoreChange || 0);
            
            return h('div', { 
              key: idx, 
              className: `flex items-center space-x-3 py-2 px-2 rounded transition-colors ${
                isHealthy ? 'bg-green-50 hover:bg-green-100' : 'hover:bg-gray-50'
              }`
            },
              // Timestamp
              h('div', { className: 'w-28 text-xs text-gray-600 flex-shrink-0 font-mono' },
                analyticsService.formatTimestamp(point.timestamp).substring(11) || 'Unknown'
              ),
              // Visual bar
              h('div', { className: 'flex-1 flex items-center space-x-2' },
                h('div', { className: 'w-full max-w-xs bg-gray-200 rounded-full h-3 relative overflow-hidden' },
                  h('div', {
                    className: `h-3 rounded-full transition-all duration-300 ${
                      point.trustScore >= 85 ? 'bg-green-500' :
                      point.trustScore >= 70 ? 'bg-green-400' :
                      point.trustScore >= 50 ? 'bg-yellow-500' :
                      point.trustScore >= 30 ? 'bg-orange-500' : 'bg-red-500'
                    }`,
                    style: { width: `${Math.max(5, point.trustScore)}%` }
                  })
                ),
                h('span', { className: 'text-sm font-semibold w-12 text-right' }, Math.round(point.trustScore)),
                // Change indicator with healthy behavior highlight
                changeAbs > 0 && h('span', { 
                  className: `text-xs font-medium w-16 text-right ${
                    isHealthy ? 'text-green-700 font-bold' :
                    isImprovement ? 'text-green-600' : 
                    isDegradation ? 'text-red-600' : 'text-gray-600'
                  }`
                }, `${isImprovement ? '+' : ''}${Math.round(changeAbs * 10) / 10}`)
              ),
              // Event type indicator with special styling for healthy behavior
              point.eventType && h('span', { 
                className: `text-xs px-2 py-1 rounded whitespace-nowrap flex-shrink-0 ${
                  isHealthy ? 'bg-green-200 text-green-800 font-semibold' :
                  point.eventType.includes('IMPROVEMENT') || point.eventType.includes('CURRENT') ? 'bg-green-100 text-green-700' :
                  point.eventType.includes('DEGRADATION') ? 'bg-red-100 text-red-700' :
                  point.eventType.includes('CHANGE') ? 'bg-blue-100 text-blue-700' :
                  'bg-gray-100 text-gray-700'
                }`
              }, 
                (isHealthy ? '✅ ' : '') + 
                point.eventType.replace(/_/g, ' ').replace('HEALTHY ', '').substring(0, 12)
              )
            );
          })
        )
      ),

      // Summary stats
      h('div', { className: 'mt-4 pt-4 border-t' },
        h('div', { className: 'grid grid-cols-4 gap-4 text-center' },
          h('div', null,
            h('div', { className: 'text-xl font-bold text-green-600' }, Math.round(maxScore)),
            h('div', { className: 'text-xs text-gray-600' }, 'Peak Score')
          ),
          h('div', null,
            h('div', { className: 'text-xl font-bold text-red-600' }, Math.round(minScore)),
            h('div', { className: 'text-xs text-gray-600' }, 'Lowest Score')
          ),
          h('div', null,
            h('div', { className: 'text-xl font-bold text-blue-600' }, data.length),
            h('div', { className: 'text-xs text-gray-600' }, 'Data Points')
          ),
          h('div', null,
            h('div', { className: 'text-xl font-bold text-purple-600' }, 
              Math.round((maxScore - minScore) * 10) / 10
            ),
            h('div', { className: 'text-xs text-gray-600' }, 'Variation')
          )
        )
      )
    )
  );
}