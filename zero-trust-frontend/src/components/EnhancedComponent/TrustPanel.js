// src/components/Enhanced/TrustPanel.js
import React from 'react';
import { Shield, Info, Target } from 'lucide-react';

export default function TrustPanel({ device, onClose, onQuarantine }) {
  const mockTrustAnalysis = device.mockTrustAnalysis || {
    deviceId: device.deviceId,
    overallHealth: device.trustScore > 75 ? 'EXCELLENT' : device.trustScore > 50 ? 'GOOD' : 'CONCERNING',
    riskLevel: device.riskLevel || 'MEDIUM',
    trustFactors: [
      { 
        factorName: 'Identity Verification', 
        category: 'SECURITY', 
        score: 75, 
        impact: 'NEUTRAL', 
        dataPoints: 12, 
        description: 'Identity checks mostly pass', 
        details: { successRate: '75%' } 
      },
      { 
        factorName: 'Location Context', 
        category: 'BEHAVIORAL', 
        score: 30, 
        impact: 'CRITICAL', 
        dataPoints: 6, 
        description: 'Unusual location jumps', 
        details: { recentLocationChanges: 4 } 
      }
    ],
    recommendations: ['Run full security scan', 'Force firmware update']
  };

  const analysis = mockTrustAnalysis;

  return (
    <div className="border rounded-lg p-4">
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center space-x-2">
          <Shield className="w-5 h-5" />
          <div>
            <div className="font-semibold">Trust Analysis – {analysis.deviceId}</div>
            <div className="text-xs text-gray-500">Overall: {analysis.overallHealth} • {analysis.riskLevel} risk</div>
          </div>
        </div>
        <div className="space-x-2">
          <button 
            type="button" 
            onClick={() => onQuarantine(device.deviceId)} 
            className="px-3 py-1 bg-red-500 text-white rounded text-sm"
          >
            Quarantine
          </button>
          <button 
            type="button" 
            onClick={onClose} 
            className="px-3 py-1 bg-gray-100 rounded text-sm"
          >
            Close
          </button>
        </div>
      </div>

      <div className="space-y-3">
        {analysis.trustFactors.map((f, i) => (
          <div key={i} className="border rounded p-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <div className={`p-2 rounded ${f.category === 'SECURITY' ? 'bg-red-50' : 'bg-blue-50'}`}>
                  <Info className="w-4 h-4" />
                </div>
                <div>
                  <div className="font-medium">{f.factorName}</div>
                  <div className="text-xs text-gray-500">{f.description}</div>
                </div>
              </div>
              <div className="text-right">
                <div className={`font-bold ${f.score >= 80 ? 'text-green-600' : f.score >= 60 ? 'text-yellow-600' : 'text-red-600'}`}>
                  {f.score}
                </div>
                <div className="text-xs text-gray-500">{f.dataPoints} checks</div>
              </div>
            </div>
            <div className="mt-2">
              <div className="w-full bg-gray-200 rounded-full h-2">
                <div 
                  className={`${f.score >= 80 ? 'bg-green-500' : f.score >= 60 ? 'bg-yellow-500' : 'bg-red-500'} h-2 rounded-full`} 
                  style={{ width: `${f.score}%` }} 
                />
              </div>
            </div>
          </div>
        ))}

        <div className="border-t pt-3">
          <div className="font-medium mb-2 flex items-center">
            <Target className="w-4 h-4 mr-2" />
            Recommendations
          </div>
          <div className="space-y-1">
            {analysis.recommendations.map((r, idx) => (
              <div key={idx} className="text-sm text-gray-700">• {r}</div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}