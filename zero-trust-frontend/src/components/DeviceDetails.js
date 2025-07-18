// src/components/DeviceDetails.js
import React, { useEffect, useState } from 'react';
import {
  getDevice,
  getTrustScore,
  quarantineDevice,
  getIdentityLogs,
  getFirmwareLogs,
  getAnomalyLogs,
  getComplianceLogs,
  getLocationChanges,
  getSession
} from '../services/deviceService';

export default function DeviceDetails({ deviceId, onBack }) {
  const [device, setDevice] = useState(null);
  const [logs, setLogs] = useState({});
  const [reason, setReason] = useState('');

  useEffect(() => {
    getDevice(deviceId).then(setDevice);
    getTrustScore(deviceId).then(score =>
      setDevice(d => ({ ...d, trustScore: score }))
    );
    Promise.all({
      identity: getIdentityLogs(deviceId),
      firmware: getFirmwareLogs(deviceId),
      anomaly: getAnomalyLogs(deviceId),
      compliance: getComplianceLogs(deviceId),
      context: getLocationChanges(deviceId),
      session: getSession(deviceId)
    }).then(setLogs);
  }, [deviceId]);

  const handleQuarantine = () => {
    quarantineDevice(deviceId, reason).then(() =>
      alert('Device quarantined')
    );
  };

  if (!device) return <div>Loading...</div>;

  return (
    <div className="p-4">
      <button
        onClick={onBack}
        className="mb-4 px-2 py-1 bg-gray-500 text-white rounded"
      >
        ← Back to list
      </button>

      <h2 className="text-xl font-bold mb-2">Details for {device.deviceId}</h2>
      <p>Trusted: {device.trusted ? '✅' : '❌'}</p>
      <p>Trust Score: {device.trustScore.toFixed(1)}</p>

      <div className="mt-4">
        <label>Quarantine Reason:</label>
        <input
          value={reason}
          onChange={e => setReason(e.target.value)}
          className="border p-1 ml-2"
        />
        <button
          onClick={handleQuarantine}
          className="ml-2 px-2 py-1 bg-red-500 text-white rounded"
        >
          Quarantine
        </button>
      </div>

      <div className="mt-6">
        <h3 className="font-semibold">Logs</h3>
        {['identity','firmware','anomaly','compliance','context','session'].map(key => (
          <div key={key} className="mt-4">
            <h4 className="font-medium capitalize">{key} Logs</h4>
            <pre className="bg-gray-100 p-2 rounded h-32 overflow-auto">
              {JSON.stringify(logs[key], null, 2)}
            </pre>
          </div>
        ))}
      </div>
    </div>
  );
}
