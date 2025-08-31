// src/components/common/Sidebar.js
import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';

export default function Sidebar({ isOpen, onToggle }) {
  const [auditExpanded, setAuditExpanded] = useState(false);
  const location = useLocation();

  const isActive = (path) => location.pathname === path;
  const isAuditActive = () => location.pathname.startsWith('/audit');

  const toggleAudit = () => setAuditExpanded(!auditExpanded);

  const classNames = (...classes) => classes.filter(Boolean).join(' ');

  return (
    <>
      {/* Desktop Sidebar */}
      <div className={classNames(
        'fixed inset-y-0 left-0 z-30 w-64 bg-gray-900 transform transition-transform duration-300 ease-in-out lg:translate-x-0',
        isOpen ? 'translate-x-0' : '-translate-x-full'
      )}>
        {/* Header */}
        <div className="flex items-center justify-center h-16 bg-gray-800">
          <div className="flex items-center">
            <span className="text-white text-xl font-semibold">🛡️ IoT Security Hub</span>
          </div>
        </div>
        
        <nav className="mt-5 px-2">
          {/* Dashboard */}
          <Link
            to="/dashboard"
            className={classNames(
              'group flex items-center px-2 py-2 text-base font-medium rounded-md transition-colors',
              isActive('/dashboard') || location.pathname === '/'
                ? 'bg-gray-800 text-white'
                : 'text-gray-300 hover:bg-gray-700 hover:text-white'
            )}
          >
            <span className="mr-4 text-lg">📊</span>
            Dashboard
          </Link>

          {/* Audit & Analytics */}
          <div className="mt-1">
            <button
              onClick={toggleAudit}
              className={classNames(
                'w-full group flex items-center px-2 py-2 text-base font-medium rounded-md transition-colors',
                isAuditActive()
                  ? 'bg-gray-800 text-white'
                  : 'text-gray-300 hover:bg-gray-700 hover:text-white'
              )}
            >
              <span className="mr-4 text-lg">📋</span>
              <span className="flex-1 text-left">Audit & Logs</span>
              <span className={classNames(
                'ml-2 transform transition-transform',
                auditExpanded || isAuditActive() ? 'rotate-90' : ''
              )}>
                ▶
              </span>
            </button>
            
            {/* Audit Submenu */}
            <div className={classNames(
              'mt-1 ml-4 space-y-1 transition-all duration-200',
              auditExpanded || isAuditActive() ? 'block' : 'hidden'
            )}>
              <Link
                to="/audit?tab=location"
                className={classNames(
                  'group flex items-center px-2 py-2 text-sm font-medium rounded-md transition-colors',
                  location.search.includes('tab=location') || (isAuditActive() && !location.search)
                    ? 'bg-gray-700 text-white'
                    : 'text-gray-400 hover:bg-gray-700 hover:text-white'
                )}
              >
                <span className="mr-3">🌍</span>
                Location/Network Changes
              </Link>
              
              <Link
                to="/audit?tab=quarantine"
                className={classNames(
                  'group flex items-center px-2 py-2 text-sm font-medium rounded-md transition-colors',
                  location.search.includes('tab=quarantine')
                    ? 'bg-gray-700 text-white'
                    : 'text-gray-400 hover:bg-gray-700 hover:text-white'
                )}
              >
                <span className="mr-3">🚨</span>
                Quarantine History
              </Link>
              
              <Link
                to="/audit?tab=firmware"
                className={classNames(
                  'group flex items-center px-2 py-2 text-sm font-medium rounded-md transition-colors',
                  location.search.includes('tab=firmware')
                    ? 'bg-gray-700 text-white'
                    : 'text-gray-400 hover:bg-gray-700 hover:text-white'
                )}
              >
                <span className="mr-3">💾</span>
                Firmware Management
              </Link>
            </div>
          </div>

          {/* Device Registry */}
          <Link
            to="/devices"
            className={classNames(
              'group flex items-center px-2 py-2 text-base font-medium rounded-md transition-colors mt-1',
              isActive('/devices')
                ? 'bg-gray-800 text-white'
                : 'text-gray-300 hover:bg-gray-700 hover:text-white'
            )}
          >
            <span className="mr-4 text-lg">📱</span>
            Device Registry
          </Link>
        </nav>

        {/* System Status Section - Enhanced */}
        <div className="absolute bottom-0 left-0 right-0 p-4 bg-gray-800">
          <div className="text-center">
            <div className="text-xs text-gray-400 mb-2">System Status</div>
            <div className="flex items-center justify-center space-x-2 mb-2">
              <div className="w-2 h-2 bg-green-500 rounded-full"></div>
              <span className="text-xs text-green-400">Online</span>
            </div>
            <div className="text-gray-400 text-xs">
              IoT Security Platform v2.0
            </div>
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