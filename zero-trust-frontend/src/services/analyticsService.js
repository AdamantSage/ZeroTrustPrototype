// src/services/analyticsService.js
import axios from 'axios';

const API_BASE = 'http://localhost:8069/api';

class AnalyticsService {
  // Device-specific analytics
  async getDeviceRiskAssessment(deviceId) {
    try {
      const response = await axios.get(`${API_BASE}/analytics/device/${deviceId}/risk-assessment`);
      return response.data;
    } catch (error) {
      console.error('Failed to get device risk assessment:', error);
      throw error;
    }
  }

  async getDeviceTrustAnalysis(deviceId, hours = 24) {
    try {
      const response = await axios.get(`${API_BASE}/analytics/device/${deviceId}/trust-analysis`, {
        params: { hours }
      });
      return response.data;
    } catch (error) {
      console.error('Failed to get device trust analysis:', error);
      throw error;
    }
  }

  async getTrustScoreTimeline(deviceId, days = 7) {
    try {
      const response = await axios.get(`${API_BASE}/analytics/device/${deviceId}/trust-timeline`, {
        params: { days }
      });
      return response.data;
    } catch (error) {
      console.error('Failed to get trust score timeline:', error);
      throw error;
    }
  }

  async getRecentChanges(deviceId, hours = 6) {
    try {
      const response = await axios.get(`${API_BASE}/analytics/device/${deviceId}/recent-changes`, {
        params: { hours }
      });
      return response.data;
    } catch (error) {
      console.error('Failed to get recent changes:', error);
      throw error;
    }
  }

  async getBehaviorAnalysis(deviceId, hours = 24) {
    try {
      const response = await axios.get(`${API_BASE}/analytics/device/${deviceId}/behavior-analysis`, {
        params: { hours }
      });
      return response.data;
    } catch (error) {
      console.error('Failed to get behavior analysis:', error);
      throw error;
    }
  }

  // System-wide analytics
  async getSystemRiskOverview() {
    try {
      const response = await axios.get(`${API_BASE}/analytics/system/risk-overview`);
      return response.data;
    } catch (error) {
      console.error('Failed to get system risk overview:', error);
      throw error;
    }
  }

  async getDevicesRequiringAttention() {
    try {
      const response = await axios.get(`${API_BASE}/analytics/system/devices-requiring-attention`);
      return response.data;
    } catch (error) {
      console.error('Failed to get devices requiring attention:', error);
      throw error;
    }
  }

  async getEnhancedLocationMapData() {
    try {
      const response = await axios.get(`${API_BASE}/analytics/system/location-map`);
      return response.data;
    } catch (error) {
      console.error('Failed to get enhanced location map data:', error);
      throw error;
    }
  }

  // Administrative actions
  async resetDeviceTrustScore(deviceId, baselineScore = 50.0) {
    try {
      const response = await axios.post(
        `${API_BASE}/analytics/device/${deviceId}/reset-trust-score`,
        null,
        { params: { baselineScore } }
      );
      return response.data;
    } catch (error) {
      console.error('Failed to reset trust score:', error);
      throw error;
    }
  }

  async simulateTrustScoreChange(deviceId, factorChanges) {
    try {
      const response = await axios.post(
        `${API_BASE}/analytics/device/${deviceId}/simulate-trust-change`,
        factorChanges
      );
      return response.data;
    } catch (error) {
      console.error('Failed to simulate trust score change:', error);
      throw error;
    }
  }

  async healthCheck() {
    try {
      const response = await axios.get(`${API_BASE}/analytics/health`);
      return response.data;
    } catch (error) {
      console.error('Analytics service health check failed:', error);
      throw error;
    }
  }

  // Utility methods
  getRiskLevelColor(riskLevel) {
    switch (riskLevel?.toUpperCase()) {
      case 'CRITICAL':
        return 'text-red-700 bg-red-100 border-red-200';
      case 'HIGH':
        return 'text-orange-700 bg-orange-100 border-orange-200';
      case 'MEDIUM':
        return 'text-yellow-700 bg-yellow-100 border-yellow-200';
      case 'LOW':
        return 'text-green-700 bg-green-100 border-green-200';
      default:
        return 'text-gray-700 bg-gray-100 border-gray-200';
    }
  }

  getRiskLevelIcon(riskLevel) {
    switch (riskLevel?.toUpperCase()) {
      case 'CRITICAL':
        return '🚨';
      case 'HIGH':
        return '⚠️';
      case 'MEDIUM':
        return '⚡';
      case 'LOW':
        return '✅';
      default:
        return '❓';
    }
  }

  getTrustScoreColor(trustScore) {
    if (trustScore >= 85) return 'text-green-600';
    if (trustScore >= 70) return 'text-blue-600';
    if (trustScore >= 50) return 'text-yellow-600';
    if (trustScore >= 30) return 'text-orange-600';
    return 'text-red-600';
  }

  formatTimestamp(timestamp) {
    if (!timestamp) return 'Unknown';
    try {
      return new Date(timestamp).toLocaleString();
    } catch (error) {
      return 'Invalid date';
    }
  }

  formatDuration(hours) {
    if (hours < 1) return `${Math.round(hours * 60)} minutes`;
    if (hours < 24) return `${Math.round(hours)} hours`;
    return `${Math.round(hours / 24)} days`;
  }
}

export default new AnalyticsService();