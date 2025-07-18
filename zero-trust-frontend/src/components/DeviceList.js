// src/components/DeviceList.js
import React, { useEffect, useState } from 'react';
import { getDevices } from '../services/deviceService';

export default function DeviceList({ onSelectDevice }) {
  const [devices, setDevices] = useState([]);

  useEffect(() => {
    getDevices().then(setDevices);
  }, []);

  return (
    <div className="p-4">
      <h2 className="text-xl font-bold mb-4">Devices</h2>
      <table className="min-w-full table-auto">
        <thead>
          <tr>
            <th>Device ID</th>
            <th>Trusted</th>
            <th>Trust Score</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {devices.map(d => (
            <tr key={d.deviceId} className="hover:bg-gray-100">
              <td>{d.deviceId}</td>
              <td>{d.trusted ? '✅' : '❌'}</td>
              <td>{d.trustScore.toFixed(1)}</td>
              <td>
                <button
                  onClick={() => onSelectDevice(d.deviceId)}
                  className="px-2 py-1 bg-blue-500 text-white rounded"
                >
                  Details
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
