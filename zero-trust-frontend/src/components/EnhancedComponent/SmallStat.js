// src/components/Enhanced/SmallStat.js
import React from 'react';

export default function SmallStat({ icon, title, value }) {
  return (
    <div className="bg-white p-3 rounded-md border flex items-center space-x-3">
      <div className="p-2 bg-gray-50 rounded">{icon}</div>
      <div>
        <div className="text-xs text-gray-500">{title}</div>
        <div className="text-lg font-bold text-gray-900">{value}</div>
      </div>
    </div>
  );
}