// src/components/common/Header.js
import React from 'react';

export default function Header({ onMenuToggle }) {
  return (
    <header className="flex items-center justify-between bg-header-background1 text-white px-6 py-4">
      <button onClick={onMenuToggle} className="lg:hidden text-white text-xl">
        ☰
      </button>
      <h1 className="text-xl font-medium">Zero Trust Dashboard</h1>
      <div className="flex items-center space-x-4">
        <div className="text-sm">
          <span className="opacity-75">Welcome back,</span>
        </div>
        <span className="rounded-full bg-white text-header-background1 px-3 py-1 text-sm font-medium">
          Admin
        </span>
      </div>
    </header>
  );
}