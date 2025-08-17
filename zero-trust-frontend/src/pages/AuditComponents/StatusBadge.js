import React from 'react';

export default function StatusBadge(props) {
  const h = React.createElement;
  const status = props.status || '';
  const statusColors = {
    SUCCESS: 'bg-green-100 text-green-800',
    FAILED: 'bg-red-100 text-red-800',
    PENDING: 'bg-yellow-100 text-yellow-800',
    ALREADY_QUARANTINED: 'bg-blue-100 text-blue-800',
    RECREATED: 'bg-purple-100 text-purple-800'
  };

  return h('span', { className: `px-2 py-1 text-xs font-medium rounded-full ${statusColors[status] || 'bg-gray-100 text-gray-800'}` }, status);
}