// src/services/deviceService.js
import axios from 'axios';

const API_BASE = 'http://localhost:8069/api';

export const getDevices = async () => {
  const response = await axios.get(`${API_BASE}/devices`);
  return response.data;
};

export const getDevice = async (deviceId) => {
  const response = await axios.get(`${API_BASE}/devices/${deviceId}`);
  return response.data;
};

export const getTrustScore = async (deviceId) => {
  const response = await axios.get(`${API_BASE}/devices/${deviceId}/trust-score`);
  return response.data;
};

export const quarantineDevice = async (deviceId, reason) => {
  await axios.post(`${API_BASE}/devices/${deviceId}/quarantine`, null, {
    params: { reason }
  });
};
export const getFirmwareSummary = async (deviceId) => {
  try {
    const { data } = await axios.get(`${API_BASE}/firmware/summary/${deviceId}`);
    return data;
  } catch (e) {
    console.warn('Firmware summary failed, using defaults', e);
    return {
      reportedFirmwareVersion: 'Unknown',
      expectedFirmwareVersion: 'Unknown',
      reportedPatchStatus: 'Unknown',
      firmwareValid: true,
      timestamp: new Date().toISOString(),
    };
  }
};

// Optional: to request a policy change (e.g. require update)
export const requestFirmwareUpdate = async (deviceId) => {
  try {
    await axios.post(`${API_BASE}/admin/firmware/require-update`, null, { params: { deviceId } });
    return true;
  } catch (e) {
    console.error('Request firmware update failed', e);
    return false;
  }
};
// Logs & other endpoints remain the same (assuming your backend implements them)
export const getIdentityLogs      = deviceId => axios.get(`${API_BASE}/identity/logs/${deviceId}`).then(r=>r.data);
export const getFirmwareLogs      = deviceId => axios.get(`${API_BASE}/firmware/logs/${deviceId}`).then(r=>r.data);
export const getAnomalyLogs       = deviceId => axios.get(`${API_BASE}/anomaly/logs/${deviceId}`).then(r=>r.data);
export const getComplianceLogs    = deviceId => axios.get(`${API_BASE}/compliance/logs/${deviceId}`).then(r=>r.data);
export const getLocationChanges   = deviceId => axios.get(`${API_BASE}/location/changes/${deviceId}`).then(r=>r.data);
export const getSession           = deviceId => axios.get(`${API_BASE}/sessions/${deviceId}`).then(r=>r.data);
