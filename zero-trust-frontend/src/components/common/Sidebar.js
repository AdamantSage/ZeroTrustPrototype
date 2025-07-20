// src/components/common/Sidebar.js
import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';

export default function Sidebar({ isOpen, onToggle }) {
  const [auditExpanded, setAuditExpanded] = useState(false);
  const location = useLocation();

  const isActive = (path) => location.pathname === path;
  const isAuditActive = () => location.pathname.startsWith('/audit');

  const toggleAudit = () => setAuditExpanded(!auditExpanded);

  return (
    <>
      {/* Desktop Sidebar */}
      <div className={`fixed inset-y-0 left-0 z-30 w-64 bg-gray-900 transform transition-transform duration-300 ease-in-out lg:translate-x-0 ${
        isOpen ? 'translate-x-0' : '-translate-x-full'
      }`}>
        <div className="flex items-center justify-center h-16 bg-gray-800">
          <h1 className="text-white text-lg font-bold">IoT Security Hub</h1>
        </div>
        
        <nav className="mt-5 px-2">
          {/* Dashboard */}
          <Link
            to="/dashboard"
            className={`group flex items-center px-2 py-2 text-base font-medium rounded-md transition-colors ${
              isActive('/dashboard') || location.pathname === '/'
                ? 'bg-gray-800 text-white'
                : 'text-gray-300 hover:bg-gray-700 hover:text-white'
            }`}
          >
            <span className="mr-4">📊</span>
            Dashboard
          </Link>

          {/* Audit & Analytics */}
          <div className="mt-1">
            <button
              onClick={toggleAudit}
              className={`w-full group flex items-center px-2 py-2 text-base font-medium rounded-md transition-colors ${
                isAuditActive()
                  ? 'bg-gray-800 text-white'
                  : 'text-gray-300 hover:bg-gray-700 hover:text-white'
              }`}
            >
              <span className="mr-4">🔍</span>
              <span className="flex-1 text-left">Audit & Analytics</span>
              <span className={`ml-2 transform transition-transform ${auditExpanded ? 'rotate-90' : ''}`}>
                ▶
              </span>
            </button>
            
            {/* Audit Submenu */}
            <div className={`mt-1 ml-4 space-y-1 transition-all duration-200 ${
              auditExpanded || isAuditActive() ? 'block' : 'hidden'
            }`}>
              <Link
                to="/audit?tab=location"
                className={`group flex items-center px-2 py-2 text-sm font-medium rounded-md transition-colors ${
                  location.search.includes('tab=location') || (isAuditActive() && !location.search)
                    ? 'bg-gray-700 text-white'
                    : 'text-gray-400 hover:bg-gray-700 hover:text-white'
                }`}
              >
                <span className="mr-3">🌍</span>
                Location/Network Changes
              </Link>
              
              <Link
                to="/audit?tab=quarantine"
                className={`group flex items-center px-2 py-2 text-sm font-medium rounded-md transition-colors ${
                  location.search.includes('tab=quarantine')
                    ? 'bg-gray-700 text-white'
                    : 'text-gray-400 hover:bg-gray-700 hover:text-white'
                }`}
              >
                <span className="mr-3">🚨</span>
                Quarantine History
              </Link>
              
              <Link
                to="/audit?tab=firmware"
                className={`group flex items-center px-2 py-2 text-sm font-medium rounded-md transition-colors ${
                  location.search.includes('tab=firmware')
                    ? 'bg-gray-700 text-white'
                    : 'text-gray-400 hover:bg-gray-700 hover:text-white'
                }`}
              >
                <span className="mr-3">💾</span>
                Firmware Management
              </Link>
            </div>
          </div>

          {/* Device Registry */}
          <Link
            to="/devices"
            className={`group flex items-center px-2 py-2 text-base font-medium rounded-md transition-colors mt-1 ${
              isActive('/devices')
                ? 'bg-gray-800 text-white'
                : 'text-gray-300 hover:bg-gray-700 hover:text-white'
            }`}
          >
            <span className="mr-4">📱</span>
            Device Registry
          </Link>

          {/* Settings */}
          <Link
            to="/settings"
            className={`group flex items-center px-2 py-2 text-base font-medium rounded-md transition-colors mt-1 ${
              isActive('/settings')
                ? 'bg-gray-800 text-white'
                : 'text-gray-300 hover:bg-gray-700 hover:text-white'
            }`}
          >
            <span className="mr-4">⚙️</span>
            Settings
          </Link>
        </nav>

        {/* Footer */}
        <div className="absolute bottom-0 left-0 right-0 p-4 bg-gray-800">
          <div className="text-gray-400 text-sm text-center">
            IoT Security Platform v2.0
          </div>
        </div>
      </div>

      {/* Mobile overlay */}
      {isOpen && (
        <div
          className="fixed inset-0 bg-black bg-opacity-50 z-20 lg:hidden"
          onClick={onToggle}
        />
      )}
    </>
  );
}


// // src/components/common/Sidebar.js
// import React from 'react';

// export default function Sidebar({ isOpen, onToggle }) {
//   return (
//     <aside
//       className={`${
//         isOpen ? 'w-64' : 'w-0 lg:w-64'
//       } transition-all duration-300 ease-in-out bg-sidebar-background1 text-sidebar-text1 shadow-lg overflow-hidden lg:relative fixed lg:translate-x-0 ${
//         isOpen ? 'translate-x-0' : '-translate-x-full'
//       } inset-y-0 left-0 z-30 lg:z-auto`}
//     >
//       <div className="w-64 p-6 h-full flex flex-col">
//         <div className="flex items-center justify-between mb-6">
//           <h2 className="text-xl font-semibold text-gray-800">Zero‑Trust Admin</h2>
//           <button 
//             onClick={onToggle} 
//             className="lg:hidden text-gray-600 hover:text-gray-800 text-xl"
//           >
//             ✕
//           </button>
//         </div>
        
//         <nav className="space-y-2 flex-1">
//           <div className="space-y-1">
//             <button className="w-full text-left py-3 px-4 rounded-lg hover:bg-gray-200 transition-colors duration-200 flex items-center space-x-3">
//               <span className="text-lg">📊</span>
//               <span className="font-medium">Dashboard</span>
//             </button>
//             <button className="w-full text-left py-3 px-4 rounded-lg hover:bg-gray-200 transition-colors duration-200 flex items-center space-x-3">
//               <span className="text-lg">🔒</span>
//               <span className="font-medium">Devices</span>
//             </button>
//             <button className="w-full text-left py-3 px-4 rounded-lg hover:bg-gray-200 transition-colors duration-200 flex items-center space-x-3">
//               <span className="text-lg">📈</span>
//               <span className="font-medium">Analytics</span>
//             </button>
//             <button className="w-full text-left py-3 px-4 rounded-lg hover:bg-gray-200 transition-colors duration-200 flex items-center space-x-3">
//               <span className="text-lg">⚙️</span>
//               <span className="font-medium">Settings</span>
//             </button>
//           </div>
//         </nav>
        
//         <div className="mt-auto pt-6">
//           <div className="border-t border-gray-300 pt-4">
//             <button className="w-full text-left py-2 px-4 rounded-lg hover:bg-gray-200 transition-colors duration-200 text-sm text-gray-600">
//               Help & Support
//             </button>
//             <button className="w-full text-left py-2 px-4 rounded-lg hover:bg-gray-200 transition-colors duration-200 text-sm text-gray-600">
//               Logout
//             </button>
//           </div>
//         </div>
//       </div>
//     </aside>
//   );
// }