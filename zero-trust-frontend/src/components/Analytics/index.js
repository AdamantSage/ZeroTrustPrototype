// src/components/Analytics/index.js
import React from 'react';
import analyticsService from '../../services/analyticsService';

const h = React.createElement;

// System Overview Component
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

// Device Risk Assessment Panel
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

// Trust Analysis Panel
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

// Timeline Chart Component
export function TimelineChart({ data, deviceId }) {
  if (!data || data.length === 0) return null;

  // Simple timeline visualization
  const maxScore = Math.max(...data.map(d => d.trustScore));
  const minScore = Math.min(...data.map(d => d.trustScore));
  const range = maxScore - minScore || 1;

  return h('div', { className: 'bg-white rounded-lg shadow-sm border' },
    h('div', { className: 'p-4 border-b' },
      h('h3', { className: 'text-lg font-semibold' }, '📊 Trust Score Timeline')
    ),

    h('div', { className: 'p-4' },
      // Timeline visualization
      h('div', { className: 'relative' },
        h('div', { className: 'space-y-3' },
          ...data.slice(0, 10).map((point, idx) => {
            const heightPercent = ((point.trustScore - minScore) / range) * 100;
            return h('div', { key: idx, className: 'flex items-center space-x-4' },
              // Timestamp
              h('div', { className: 'w-32 text-xs text-gray-600 flex-shrink-0' },
                analyticsService.formatTimestamp(point.timestamp).split(' ')[1] || 'Unknown'
              ),
              // Visual bar
              h('div', { className: 'flex-1 flex items-center space-x-2' },
                h('div', { className: 'w-32 bg-gray-200 rounded-full h-2 relative' },
                  h('div', {
                    className: `h-2 rounded-full ${
                      point.trustScore >= 70 ? 'bg-green-500' :
                      point.trustScore >= 50 ? 'bg-yellow-500' : 'bg-red-500'
                    }`,
                    style: { width: `${Math.max(5, (point.trustScore / 100) * 100)}%` }
                  })
                ),
                h('span', { className: 'text-sm font-medium w-12' }, Math.round(point.trustScore)),
                // Event type indicator
                point.eventType && h('span', { 
                  className: `text-xs px-2 py-1 rounded ${
                    point.eventType.includes('IMPROVEMENT') ? 'bg-green-100 text-green-700' :
                    point.eventType.includes('DEGRADATION') ? 'bg-red-100 text-red-700' :
                    'bg-blue-100 text-blue-700'
                  }`
                }, point.eventType.replace(/_/g, ' '))
              )
            );
          })
        )
      ),

      // Summary stats
      h('div', { className: 'mt-4 pt-4 border-t' },
        h('div', { className: 'grid grid-cols-3 gap-4 text-center' },
          h('div', null,
            h('div', { className: 'text-lg font-bold' }, Math.round(maxScore)),
            h('div', { className: 'text-xs text-gray-600' }, 'Peak Score')
          ),
          h('div', null,
            h('div', { className: 'text-lg font-bold' }, Math.round(minScore)),
            h('div', { className: 'text-xs text-gray-600' }, 'Lowest Score')
          ),
          h('div', null,
            h('div', { className: 'text-lg font-bold' }, data.length),
            h('div', { className: 'text-xs text-gray-600' }, 'Data Points')
          )
        )
      )
    )
  );
}

// Recent Changes Panel
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

// Devices Requiring Attention
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

// Enhanced Location Map (simplified version for now)
export function EnhancedLocationMap({ data, onDeviceSelect, selectedDevice }) {
  if (!data || !data.deviceLocations) return null;

  const locations = data.deviceLocations || [];
  const locationGroups = locations.reduce((acc, device) => {
    const loc = device.location || 'Unknown';
    if (!acc[loc]) acc[loc] = [];
    acc[loc].push(device);
    return acc;
  }, {});

  return h('div', { className: 'bg-white rounded-lg shadow-sm border' },
    h('div', { className: 'p-4 border-b' },
      h('h3', { className: 'text-lg font-semibold' }, '🗺️ Enhanced Location Map')
    ),

    h('div', { className: 'p-4' },
      h('div', { className: 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4' },
        ...Object.entries(locationGroups).map(([location, devices]) => {
          const riskCounts = devices.reduce((acc, d) => {
            acc[d.riskLevel || 'LOW'] = (acc[d.riskLevel || 'LOW'] || 0) + 1;
            return acc;
          }, {});
          
          const highestRisk = riskCounts.CRITICAL ? 'CRITICAL' :
                             riskCounts.HIGH ? 'HIGH' :
                             riskCounts.MEDIUM ? 'MEDIUM' : 'LOW';

          return h('div', { 
            key: location, 
            className: `border rounded-lg p-4 ${analyticsService.getRiskLevelColor(highestRisk)}`
          },
            h('div', { className: 'font-medium mb-2' }, location),
            h('div', { className: 'text-sm text-gray-600 mb-3' }, 
              `${devices.length} device${devices.length !== 1 ? 's' : ''}`
            ),
            h('div', { className: 'space-y-1' },
              ...devices.slice(0, 3).map(device =>
                h('div', { 
                  key: device.deviceId,
                  className: `text-xs p-2 bg-white bg-opacity-50 rounded cursor-pointer hover:bg-opacity-75 ${
                    selectedDevice === device.deviceId ? 'ring-2 ring-blue-500' : ''
                  }`,
                  onClick: () => onDeviceSelect(device.deviceId)
                },
                  h('div', { className: 'flex items-center justify-between' },
                    h('span', { className: 'font-medium' }, device.deviceId),
                    h('span', { className: analyticsService.getTrustScoreColor(device.trustScore) }, 
                      Math.round(device.trustScore || 0)
                    )
                  ),
                  device.activeThreats > 0 && h('div', { className: 'text-red-600' }, 
                    `${device.activeThreats} threat${device.activeThreats !== 1 ? 's' : ''}`
                  )
                )
              ),
              devices.length > 3 && h('div', { className: 'text-xs text-gray-500 text-center' },
                `+${devices.length - 3} more...`
              )
            )
          );
        })
      )
    )
  );
}