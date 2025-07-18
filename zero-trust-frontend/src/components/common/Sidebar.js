// src/components/common/Sidebar.js
import React from 'react';

export default function Sidebar({ isOpen, onToggle }) {
  return (
    <aside
      className={`${
        isOpen ? 'w-64' : 'w-0 lg:w-64'
      } transition-all duration-300 ease-in-out bg-sidebar-background1 text-sidebar-text1 shadow-lg overflow-hidden lg:relative fixed lg:translate-x-0 ${
        isOpen ? 'translate-x-0' : '-translate-x-full'
      } inset-y-0 left-0 z-30 lg:z-auto`}
    >
      <div className="w-64 p-6 h-full flex flex-col">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-semibold text-gray-800">Zero‑Trust Admin</h2>
          <button 
            onClick={onToggle} 
            className="lg:hidden text-gray-600 hover:text-gray-800 text-xl"
          >
            ✕
          </button>
        </div>
        
        <nav className="space-y-2 flex-1">
          <div className="space-y-1">
            <button className="w-full text-left py-3 px-4 rounded-lg hover:bg-gray-200 transition-colors duration-200 flex items-center space-x-3">
              <span className="text-lg">📊</span>
              <span className="font-medium">Dashboard</span>
            </button>
            <button className="w-full text-left py-3 px-4 rounded-lg hover:bg-gray-200 transition-colors duration-200 flex items-center space-x-3">
              <span className="text-lg">🔒</span>
              <span className="font-medium">Devices</span>
            </button>
            <button className="w-full text-left py-3 px-4 rounded-lg hover:bg-gray-200 transition-colors duration-200 flex items-center space-x-3">
              <span className="text-lg">📈</span>
              <span className="font-medium">Analytics</span>
            </button>
            <button className="w-full text-left py-3 px-4 rounded-lg hover:bg-gray-200 transition-colors duration-200 flex items-center space-x-3">
              <span className="text-lg">⚙️</span>
              <span className="font-medium">Settings</span>
            </button>
          </div>
        </nav>
        
        <div className="mt-auto pt-6">
          <div className="border-t border-gray-300 pt-4">
            <button className="w-full text-left py-2 px-4 rounded-lg hover:bg-gray-200 transition-colors duration-200 text-sm text-gray-600">
              Help & Support
            </button>
            <button className="w-full text-left py-2 px-4 rounded-lg hover:bg-gray-200 transition-colors duration-200 text-sm text-gray-600">
              Logout
            </button>
          </div>
        </div>
      </div>
    </aside>
  );
}