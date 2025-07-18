// src/services/deviceService.js
import axios from 'axios';

const API_BASE = 'http://localhost:8069/api';

// Mock data for when the backend is not available
const mockDevices = [
  { deviceId: 'DEV001', trusted: true, quarantined: false },
  { deviceId: 'DEV002', trusted: false, quarantined: true },
  { deviceId: 'DEV003', trusted: true, quarantined: false },
  { deviceId: 'DEV004', trusted: false, quarantined: false },
];

const mockTrustScore = () => Math.random() * 100;

// Helper function to simulate API delay
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));

export const getDevices = async () => {
  try {
    const response = await axios.get(`${API_BASE}/devices`);
    return response.data;
  } catch (error) {
    console.warn('Backend not available, using mock data:', error.message);
    await delay(500); // Simulate network delay
    return mockDevices;
  }
};

export const getDevice = async (deviceId) => {
  try {
    const response = await axios.get(`${API_BASE}/devices/${deviceId}`);
    return response.data;
  } catch (error) {
    console.warn('Backend not available, using mock data:', error.message);
    await delay(300);
    return mockDevices.find(d => d.deviceId === deviceId) || mockDevices[0];
  }
};

export const getTrustScore = async (deviceId) => {
  try {
    const response = await axios.get(`${API_BASE}/devices/${deviceId}/trust-score`);
    return response.data;
  } catch (error) {
    console.warn('Backend not available, using mock data:', error.message);
    await delay(200);
    return mockTrustScore();
  }
};

export const quarantineDevice = async (deviceId, reason) => {
  try {
    const response = await axios.post(`${API_BASE}/devices/${deviceId}/quarantine`, null, {
      params: { reason }
    });
    return response.data;
  } catch (error) {
    console.warn('Backend not available, simulating quarantine:', error.message);
    await delay(400);
    return { success: true, message: 'Device quarantined (mock)' };
  }
};

// Optional: More device-specific logs with mock data
export const getIdentityLogs = async (deviceId) => {
  try {
    const response = await axios.get(`${API_BASE}/identity/logs/${deviceId}`);
    return response.data;
  } catch (error) {
    await delay(300);
    return { logs: [`Identity check for ${deviceId}`, 'Authentication successful'] };
  }
};

export const getFirmwareLogs = async (deviceId) => {
  try {
    const response = await axios.get(`${API_BASE}/firmware/logs/${deviceId}`);
    return response.data;
  } catch (error) {
    await delay(300);
    return { logs: [`Firmware version check for ${deviceId}`, 'Version 1.2.3 verified'] };
  }
};

export const getAnomalyLogs = async (deviceId) => {
  try {
    const response = await axios.get(`${API_BASE}/anomaly/logs/${deviceId}`);
    return response.data;
  } catch (error) {
    await delay(300);
    return { logs: [`Anomaly detection for ${deviceId}`, 'No anomalies detected'] };
  }
};

export const getComplianceLogs = async (deviceId) => {
  try {
    const response = await axios.get(`${API_BASE}/compliance/logs/${deviceId}`);
    return response.data;
  } catch (error) {
    await delay(300);
    return { logs: [`Compliance check for ${deviceId}`, 'All policies compliant'] };
  }
};

export const getLocationChanges = async (deviceId) => {
  try {
    const response = await axios.get(`${API_BASE}/location/changes/${deviceId}`);
    return response.data;
  } catch (error) {
    await delay(300);
    return { logs: [`Location tracking for ${deviceId}`, 'Location: Office Building A'] };
  }
};

export const getSession = async (deviceId) => {
  try {
    const response = await axios.get(`${API_BASE}/sessions/${deviceId}`);
    return response.data;
  } catch (error) {
    await delay(300);
    return { session: { id: 'sess123', active: true, startTime: new Date().toISOString() } };
  }
};