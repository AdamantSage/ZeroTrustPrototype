import React from 'react';

export default function TabsNav(props) {
  const h = React.createElement;
  const tabs = props.tabs || [];
  const activeTab = props.activeTab || '';

  return h('nav', { className: '-mb-px flex space-x-8' },
    tabs.map(function(tab) {
      var isActive = activeTab === tab.id;
      return h('button', {
        key: tab.id,
        onClick: function() { props.onTabChange && props.onTabChange(tab.id); },
        className: `py-2 px-1 border-b-2 font-medium text-sm whitespace-nowrap ${isActive ? 'border-blue-500 text-blue-600' : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'}`
      }, [
        h('span', { className: 'mr-2' }, tab.icon),
        tab.name
      ]);
    })
  );
}