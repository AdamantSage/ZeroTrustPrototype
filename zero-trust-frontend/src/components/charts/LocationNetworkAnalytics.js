import React, { useMemo } from 'react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  LineChart,
  Line,
  PieChart,
  Pie,
  Cell,
  Legend,
  CartesianGrid
} from 'recharts';

// Business-focused Location & Network Analytics component
// - Uses simple, easy-to-understand metrics (no entropy/math jargon)
// - Charts: Device Risk (bar), Changes Over Time (line), Stability Breakdown (donut)
// Props:
// - locationChanges: Array of { deviceId, newLocation, timestamp }
// - expectedLocations (optional): { [deviceId]: ["Office A", "Office B"] }

export default function LocationNetworkBusinessCharts({ locationChanges = [], expectedLocations = {} }) {
  // compute metrics per device and global time-series
  const { deviceRows, dailySeries, stabilityBreakdown } = useMemo(() => {
    const byDevice = {}; // deviceId -> { total, locations: Map, events: [] }
    const dailyCounts = {}; // yyyy-mm-dd -> count

    // normalize input
    const changes = Array.isArray(locationChanges) ? locationChanges : [];

    changes.forEach(evt => {
      const deviceId = evt.deviceId || 'unknown';
      const loc = evt.newLocation || 'unknown';
      const ts = evt.timestamp ? new Date(evt.timestamp) : null;
      const day = ts ? ts.toISOString().slice(0, 10) : 'unknown';

      if (!byDevice[deviceId]) {
        byDevice[deviceId] = { total: 0, locations: {}, events: [] };
      }

      byDevice[deviceId].total += 1;
      byDevice[deviceId].locations[loc] = (byDevice[deviceId].locations[loc] || 0) + 1;
      if (ts) byDevice[deviceId].events.push({ ts: ts.getTime(), loc });

      dailyCounts[day] = (dailyCounts[day] || 0) + 1;
    });

    // compute per-device derived metrics
    const deviceRowsTmp = Object.entries(byDevice).map(([deviceId, info]) => {
      const total = info.total || 0;
      const uniqueLocations = Object.keys(info.locations).length;
      const mostVisitedCount = Math.max(...Object.values(info.locations), 0);

      // stability: percent of events that occurred at the device's most visited location
      const stability = total > 0 ? (mostVisitedCount / total) * 100 : 100;

      // rapid hops: number of times there was a location change within 10 minutes
      let rapidHops = 0;
      info.events.sort((a, b) => a.ts - b.ts);
      for (let i = 1; i < info.events.length; i++) {
        const deltaMin = (info.events[i].ts - info.events[i - 1].ts) / 60000; // minutes
        if (deltaMin > 0 && deltaMin <= 10 && info.events[i].loc !== info.events[i - 1].loc) rapidHops++;
      }

      // off-hours activity: percent of events between 00:00-05:00
      const offHoursCount = info.events.reduce((acc, e) => {
        const hour = new Date(e.ts).getUTCHours();
        return acc + (hour >= 0 && hour < 5 ? 1 : 0);
      }, 0);
      const offHoursPct = total > 0 ? (offHoursCount / total) * 100 : 0;

      // geofence violations if expectedLocations provided
      const expected = expectedLocations[deviceId] || null;
      const geoViolations = expected
        ? Object.keys(info.locations).reduce((acc, loc) => (expected.includes(loc) ? acc : acc + info.locations[loc]), 0)
        : 0;
      const geoViolationPct = total > 0 ? (geoViolations / total) * 100 : 0;

      return {
        deviceId,
        total,
        uniqueLocations,
        stability: Math.round(stability), // percent (0-100), larger is safer
        rapidHops, // raw count
        rapidHopsRate: total > 0 ? rapidHops / total : 0,
        offHoursPct: Math.round(offHoursPct),
        geoViolationPct: Math.round(geoViolationPct)
      };
    });

    // normalize some values to produce a simple 0-100 risk score
    const maxUnique = Math.max(...deviceRowsTmp.map(r => r.uniqueLocations), 1);
    const maxRapidHops = Math.max(...deviceRowsTmp.map(r => r.rapidHops), 1);

    const deviceRows = deviceRowsTmp.map(r => {
      // risk components (higher = more risky)
      const rapidComponent = (r.rapidHops / maxRapidHops) * 100; // 0-100
      const uniqueLocComponent = (r.uniqueLocations / maxUnique) * 100;
      const offHoursComponent = r.offHoursPct; // already 0-100
      const geoViolationComponent = r.geoViolationPct; // 0-100

      // weighted risk: rapid changes are most suspicious, then unexpected locations, then off-hours, then unique locations
      const riskScore = Math.min(
        Math.round(rapidComponent * 0.45 + geoViolationComponent * 0.25 + offHoursComponent * 0.2 + uniqueLocComponent * 0.1),
        100
      );

      return { ...r, riskScore };
    });

    // prepare daily series sorted by date for a line chart
    const dailySeries = Object.entries(dailyCounts)
      .map(([day, count]) => ({ date: day, count }))
      .sort((a, b) => (a.date > b.date ? 1 : -1));

    // stability breakdown for donut chart (percentages of devices by stability bracket)
    const stable = deviceRows.filter(d => d.stability >= 80).length;
    const moderate = deviceRows.filter(d => d.stability >= 50 && d.stability < 80).length;
    const unstable = deviceRows.filter(d => d.stability < 50).length;

    const stabilityBreakdown = [
      { name: 'Stable (>=80%)', value: stable },
      { name: 'Moderate (50-79%)', value: moderate },
      { name: 'Unstable (<50%)', value: unstable }
    ];

    return { deviceRows, dailySeries, stabilityBreakdown };
  }, [locationChanges, expectedLocations]);

  // helper to color-code risk: green/yellow/red
  const riskColor = score => (score >= 70 ? '#ef4444' : score >= 35 ? '#f59e0b' : '#10b981');

  // tiny convenience components
  const DeviceRiskBar = ({ data }) => (
    <div className="p-3 bg-white rounded shadow">
      <h4 className="text-sm font-medium mb-3">Device Risk Scores</h4>
      <div className="space-y-2">
        {data.map(row => (
          <div key={row.deviceId} className="flex items-center justify-between">
            <div className="flex-1 min-w-0 mr-4">
              <div className="text-sm font-medium truncate">{row.deviceId}</div>
              <div className="text-xs text-gray-500">Stability: {row.stability}% • Rapid hops: {row.rapidHops}</div>
            </div>
            <div className="w-48 ml-4 mr-4">
              <div className="w-full bg-gray-200 h-3 rounded overflow-hidden">
                <div
                  style={{ width: `${row.riskScore}%`, background: riskColor(row.riskScore) }}
                  className="h-3"
                />
              </div>
            </div>
            <div className="w-12 text-right font-semibold">{row.riskScore}</div>
          </div>
        ))}
      </div>
    </div>
  );

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
      {/* Left: Risk bars + Stability donut */}
      <div className="space-y-6">
        <DeviceRiskBar data={deviceRows.sort((a, b) => b.riskScore - a.riskScore)} />

        <div className="p-3 bg-white rounded shadow">
          <h4 className="text-sm font-medium mb-3">Stability Breakdown (by device)</h4>
          <div style={{ width: '100%', height: 200 }}>
            <ResponsiveContainer width="100%" height={200}>
              <PieChart>
                <Pie data={stabilityBreakdown} dataKey="value" nameKey="name" outerRadius={70} label>
                  {stabilityBreakdown.map((entry, idx) => (
                    <Cell key={idx} fill={idx === 0 ? '#10b981' : idx === 1 ? '#f59e0b' : '#ef4444'} />
                  ))}
                </Pie>
                <Legend verticalAlign="bottom" />
              </PieChart>
            </ResponsiveContainer>
          </div>
          <div className="text-xs text-gray-500 mt-2">Stability shows how often a device stays in its most-visited location — higher is more trusted.</div>
        </div>
      </div>

      {/* Right: Changes over time + optional detail */}
      <div className="space-y-6">
        <div className="p-3 bg-white rounded shadow">
          <h4 className="text-sm font-medium mb-3">Changes Over Time</h4>
          <div style={{ width: '100%', height: 240 }}>
            <ResponsiveContainer width="100%" height={240}>
              <LineChart data={dailySeries} margin={{ top: 5, right: 20, left: 0, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis />
                <Tooltip />
                <Line type="monotone" dataKey="count" stroke="#3b82f6" strokeWidth={2} dot={{ r: 2 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
          <div className="text-xs text-gray-500 mt-2">Shows the volume of location/network change events per day.</div>
        </div>

        <div className="p-3 bg-white rounded shadow">
          <h4 className="text-sm font-medium mb-3">Quick Filters & Notes</h4>
          <div className="text-xs text-gray-600">
            • Risk score combines rapid location hops, unexpected locations, and off-hours activity.  
            • Use the risk bars to quickly identify devices that need investigation (scores &gt;= 70).
          </div>
        </div>
      </div>
    </div>
  );
}
