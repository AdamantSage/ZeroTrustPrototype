// src/services/deviceRegistryService.js
import axios from 'axios';

const API_BASE = 'http://localhost:8069/api';

/**
 * Device Registry Service for comprehensive device management and trust analysis
 */
class DeviceRegistryService {
  
  /**
   * Get all devices with basic registry information
   */
  async getDeviceRegistry() {
    try {
      const response = await axios.get(`${API_BASE}/devices`);
      return response.data;
    } catch (error) {
      console.error('Error fetching device registry:', error);
      throw error;
    }
  }

  /**
   * Get detailed device information including trust factors
   */
  async getDeviceDetails(deviceId) {
    try {
      const response = await axios.get(`${API_BASE}/devices/${deviceId}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching device details for ${deviceId}:`, error);
      throw error;
    }
  }

  /**
   * Get comprehensive trust analysis for a device
   */
  async getTrustAnalysis(deviceId) {
    try {
      const response = await axios.get(`${API_BASE}/analytics/trust-analysis/${deviceId}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching trust analysis for ${deviceId}:`, error);
      throw error;
    }
  }

  /**
   * Get trust score breakdown with factor weights
   */
  async getTrustScoreBreakdown(deviceId) {
    try {
      const response = await axios.get(`${API_BASE}/devices/${deviceId}/trust-breakdown`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching trust breakdown for ${deviceId}:`, error);
      throw error;
    }
  }

  /**
   * Get trust score timeline for visualization
   */
  async getTrustScoreTimeline(deviceId, days = 7) {
    try {
      const response = await axios.get(`${API_BASE}/analytics/trust-timeline/${deviceId}`, {
        params: { days }
      });
      return response.data;
    } catch (error) {
      console.error(`Error fetching trust timeline for ${deviceId}:`, error);
      throw error;
    }
  }

  /**
   * Get detailed trust change analysis
   */
  async getTrustChangeAnalysis(deviceId, hours = 24) {
    try {
      const response = await axios.get(`${API_BASE}/analytics/trust-changes/${deviceId}`, {
        params: { hours }
      });
      return response.data;
    } catch (error) {
      console.error(`Error fetching trust change analysis for ${deviceId}:`, error);
      throw error;
    }
  }

  /**
   * Get device risk assessment
   */
  async getDeviceRiskAssessment(deviceId) {
    try {
      const response = await axios.get(`${API_BASE}/analytics/risk-assessment/${deviceId}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching risk assessment for ${deviceId}:`, error);
      throw error;
    }
  }

  /**
   * Reset device trust score to baseline
   */
  async resetTrustScore(deviceId, baselineScore = 50.0) {
    try {
      const response = await axios.post(`${API_BASE}/devices/${deviceId}/reset-trust-score`, null, {
        params: { baselineScore }
      });
      return response.data;
    } catch (error) {
      console.error(`Error resetting trust score for ${deviceId}:`, error);
      throw error;
    }
  }

  /**
   * Simulate trust score change to predict impact
   */
  async simulateTrustScoreChange(deviceId, factors) {
    try {
      const response = await axios.post(`${API_BASE}/devices/${deviceId}/simulate-trust-change`, factors);
      return response.data;
    } catch (error) {
      console.error(`Error simulating trust change for ${deviceId}:`, error);
      throw error;
    }
  }

  /**
   * Get system-wide trust statistics
   */
  async getSystemTrustStatistics() {
    try {
      const response = await axios.get(`${API_BASE}/analytics/system-trust-stats`);
      return response.data;
    } catch (error) {
      console.error('Error fetching system trust statistics:', error);
      throw error;
    }
  }

  /**
   * Get devices requiring attention
   */
  async getDevicesRequiringAttention() {
    try {
      const response = await axios.get(`${API_BASE}/analytics/devices-requiring-attention`);
      return response.data;
    } catch (error) {
      console.error('Error fetching devices requiring attention:', error);
      throw error;
    }
  }

  /**
   * Get factor logs for detailed analysis
   */
  async getFactorLogs(deviceId, factor, hours = 24) {
    try {
      let endpoint;
      switch (factor) {
        case 'identity':
          endpoint = `${API_BASE}/identity/logs/${deviceId}`;
          break;
        case 'firmware':
          endpoint = `${API_BASE}/firmware/logs/${deviceId}`;
          break;
        case 'anomaly':
          endpoint = `${API_BASE}/anomaly/logs/${deviceId}`;
          break;
        case 'compliance':
          endpoint = `${API_BASE}/compliance/logs/${deviceId}`;
          break;
        case 'location':
          endpoint = `${API_BASE}/location/changes/${deviceId}`;
          break;
        default:
          throw new Error(`Unknown factor: ${factor}`);
      }

      const response = await axios.get(endpoint, {
        params: { hours }
      });
      return response.data;
    } catch (error) {
      console.error(`Error fetching ${factor} logs for ${deviceId}:`, error);
      throw error;
    }
  }

  /**
   * Search devices by various criteria
   */
  async searchDevices(searchCriteria) {
    try {
      const response = await axios.post(`${API_BASE}/devices/search`, searchCriteria);
      return response.data;
    } catch (error) {
      console.error('Error searching devices:', error);
      throw error;
    }
  }

  /**
   * Export device registry data
   */
  async exportDeviceRegistry(format = 'csv', filters = {}) {
    try {
      const response = await axios.get(`${API_BASE}/devices/export`, {
        params: { format, ...filters },
        responseType: 'blob'
      });
      return response.data;
    } catch (error) {
      console.error('Error exporting device registry:', error);
      throw error;
    }
  }

  /**
   * Update device registry information
   */
  async updateDevice(deviceId, updates) {
    try {
      const response = await axios.put(`${API_BASE}/devices/${deviceId}`, updates);
      return response.data;
    } catch (error) {
      console.error(`Error updating device ${deviceId}:`, error);
      throw error;
    }
  }

  /**
   * Quarantine device with detailed logging
   */
  async quarantineDevice(deviceId, reason, additionalContext = {}) {
    try {
      const response = await axios.post(`${API_BASE}/devices/${deviceId}/quarantine`, {
        reason,
        context: additionalContext,
        timestamp: new Date().toISOString()
      });
      return response.data;
    } catch (error) {
      console.error(`Error quarantining device ${deviceId}:`, error);
      throw error;
    }
  }

  /**
   * Remove device from quarantine
   */
  async removeFromQuarantine(deviceId, reason) {
    try {
      const response = await axios.post(`${API_BASE}/devices/${deviceId}/remove-quarantine`, {
        reason,
        timestamp: new Date().toISOString()
      });
      return response.data;
    } catch (error) {
      console.error(`Error removing device ${deviceId} from quarantine:`, error);
      throw error;
    }
  }

  /**
   * Get trust score trend indicators
   */
  getTrustScoreTrend(currentScore, averageScore) {
    const difference = currentScore - averageScore;
    if (Math.abs(difference) < 2) return 'stable';
    return difference > 0 ? 'improving' : 'declining';
  }

  /**
   * Get risk level based on trust score
   */
  getRiskLevel(trustScore) {
    if (trustScore < 30) return { level: 'critical', color: '#dc2626', icon: '🚨' };
    if (trustScore < 50) return { level: 'high', color: '#ea580c', icon: '⚠️' };
    if (trustScore < 70) return { level: 'medium', color: '#d97706', icon: '⚡' };
    return { level: 'low', color: '#16a34a', icon: '✅' };
  }

  /**
   * Format trust score change for display
   */
  formatTrustScoreChange(change) {
    const absChange = Math.abs(change);
    const direction = change > 0 ? '+' : '';
    const color = change > 0 ? '#16a34a' : '#dc2626';
    const icon = change > 0 ? '📈' : '📉';
    
    return {
      text: `${direction}${change.toFixed(1)}`,
      color,
      icon,
      severity: absChange >= 10 ? 'major' : absChange >= 5 ? 'moderate' : 'minor'
    };
  }

  /**
   * Get factor status color and icon
   */
  getFactorStatus(factor, value) {
    const statusMap = {
      HIGH_RISK: { color: '#dc2626', icon: '❌', text: 'High Risk' },
      MEDIUM_RISK: { color: '#d97706', icon: '⚠️', text: 'Medium Risk' },
      LOW_RISK: { color: '#16a34a', icon: '✅', text: 'Low Risk' },
      NO_DATA: { color: '#6b7280', icon: '❓', text: 'No Data' }
    };
    
    return statusMap[value] || statusMap.NO_DATA;
  }

  /**
   * Calculate trust score health percentage
   */
  calculateHealthPercentage(trustScore) {
    // Normalize trust score to health percentage
    return Math.max(0, Math.min(100, (trustScore / 100) * 100));
  }

  /**
   * Get device type icon
   */
  getDeviceTypeIcon(deviceType) {
    const iconMap = {
      'DESKTOP': '🖥️',
      'LAPTOP': '💻',
      'MOBILE': '📱',
      'TABLET': '📲',
      'IOT_SENSOR': '📡',
      'NETWORK_DEVICE': '🌐',
      'SERVER': '🖥️',
      'UNKNOWN': '❓'
    };
    
    return iconMap[deviceType] || iconMap.UNKNOWN;
  }

  /**
   * Format timestamp for display
   */
  formatTimestamp(timestamp) {
    try {
      const date = new Date(timestamp);
      return date.toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        hour12: true
      });
    } catch (error) {
      console.error('Error formatting timestamp:', error);
      return timestamp;
    }
  }

  /**
   * Format time ago
   */
  formatTimeAgo(timestamp) {
    try {
      const now = new Date();
      const date = new Date(timestamp);
      const diffInMinutes = Math.floor((now - date) / (1000 * 60));
      
      if (diffInMinutes < 1) return 'Just now';
      if (diffInMinutes < 60) return `${diffInMinutes}m ago`;
      if (diffInMinutes < 1440) return `${Math.floor(diffInMinutes / 60)}h ago`;
      return `${Math.floor(diffInMinutes / 1440)}d ago`;
    } catch (error) {
      return 'Unknown';
    }
  }
}

// Create and export singleton instance
const deviceRegistryService = new DeviceRegistryService();
export default deviceRegistryService;

// Named exports for individual methods if needed
export const {
  getDeviceRegistry,
  getDeviceDetails,
  getTrustAnalysis,
  getTrustScoreBreakdown,
  getTrustScoreTimeline,
  getTrustChangeAnalysis,
  getDeviceRiskAssessment,
  resetTrustScore,
  simulateTrustScoreChange,
  getSystemTrustStatistics,
  getDevicesRequiringAttention,
  getFactorLogs,
  searchDevices,
  exportDeviceRegistry,
  updateDevice,
  quarantineDevice,
  removeFromQuarantine
} = deviceRegistryService;