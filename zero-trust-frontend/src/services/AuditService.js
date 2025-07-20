// src/services/auditService.js
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8069';

class AuditService {
  
  /**
   * Get location/network changes
   * @param {string} deviceId - Optional device ID to filter by
   * @returns {Promise<Array>} Array of location change records
   */
  async getLocationChanges(deviceId = null) {
    try {
      const url = deviceId 
        ? `${API_BASE_URL}/api/audit/location-changes?deviceId=${encodeURIComponent(deviceId)}`
        : `${API_BASE_URL}/api/audit/location-changes`;
      
      const response = await fetch(url, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      
      if (!response.ok) {
        throw new Error(`Failed to fetch location changes: ${response.statusText}`);
      }
      
      return await response.json();
    } catch (error) {
      console.error('Error fetching location changes:', error);
      throw error;
    }
  }

  /**
   * Get quarantine history
   * @param {string} deviceId - Optional device ID to filter by
   * @returns {Promise<Array>} Array of quarantine log records
   */
  async getQuarantineHistory(deviceId = null) {
    try {
      const url = deviceId 
        ? `${API_BASE_URL}/api/audit/quarantine-history?deviceId=${encodeURIComponent(deviceId)}`
        : `${API_BASE_URL}/api/audit/quarantine-history`;
      
      const response = await fetch(url, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      
      if (!response.ok) {
        throw new Error(`Failed to fetch quarantine history: ${response.statusText}`);
      }
      
      return await response.json();
    } catch (error) {
      console.error('Error fetching quarantine history:', error);
      throw error;
    }
  }

  /**
   * Get firmware logs
   * @param {string} deviceId - Optional device ID to filter by
   * @returns {Promise<Array>} Array of firmware log records
   */
  async getFirmwareLogs(deviceId = null) {
    try {
      const url = deviceId 
        ? `${API_BASE_URL}/api/audit/firmware-logs?deviceId=${encodeURIComponent(deviceId)}`
        : `${API_BASE_URL}/api/audit/firmware-logs`;
      
      const response = await fetch(url, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      
      if (!response.ok) {
        throw new Error(`Failed to fetch firmware logs: ${response.statusText}`);
      }
      
      return await response.json();
    } catch (error) {
      console.error('Error fetching firmware logs:', error);
      throw error;
    }
  }

  /**
   * Get audit summary statistics
   * @returns {Promise<Object>} Summary statistics object
   */
  async getAuditSummary() {
    try {
      const response = await fetch(`${API_BASE_URL}/api/audit/summary`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      
      if (!response.ok) {
        throw new Error(`Failed to fetch audit summary: ${response.statusText}`);
      }
      
      return await response.json();
    } catch (error) {
      console.error('Error fetching audit summary:', error);
      throw error;
    }
  }

  /**
   * Get all devices for filtering dropdown
   * @returns {Promise<Array>} Array of device objects
   */
  async getDevices() {
    try {
      const response = await fetch(`${API_BASE_URL}/api/devices`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      
      if (!response.ok) {
        throw new Error(`Failed to fetch devices: ${response.statusText}`);
      }
      
      return await response.json();
    } catch (error) {
      console.error('Error fetching devices:', error);
      throw error;
    }
  }

  /**
   * Export audit data to CSV
   * @param {string} type - Type of data to export ('location', 'quarantine', 'firmware')
   * @param {string} deviceId - Optional device ID to filter by
   * @returns {Promise<Blob>} CSV data as blob
   */
  async exportToCsv(type, deviceId = null) {
    try {
      const params = new URLSearchParams();
      if (deviceId) params.append('deviceId', deviceId);
      params.append('format', 'csv');
      
      const url = `${API_BASE_URL}/api/audit/${type}-export?${params.toString()}`;
      const response = await fetch(url, {
        method: 'GET',
        headers: {
          'Accept': 'text/csv',
        },
      });
      
      if (!response.ok) {
        throw new Error(`Failed to export ${type} data: ${response.statusText}`);
      }
      
      return await response.blob();
    } catch (error) {
      console.error(`Error exporting ${type} data:`, error);
      throw error;
    }
  }

  /**
   * Download CSV file
   * @param {Blob} blob - CSV data blob
   * @param {string} filename - Name for the downloaded file
   */
  downloadCsv(blob, filename) {
    try {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (error) {
      console.error('Error downloading CSV:', error);
      throw error;
    }
  }

  /**
   * Get audit data with date range filter
   * @param {string} type - Type of data ('location-changes', 'quarantine-history', 'firmware-logs')
   * @param {string} startDate - Start date in ISO format
   * @param {string} endDate - End date in ISO format
   * @param {string} deviceId - Optional device ID to filter by
   * @returns {Promise<Array>} Filtered audit data
   */
  async getAuditDataByDateRange(type, startDate, endDate, deviceId = null) {
    try {
      const params = new URLSearchParams();
      if (startDate) params.append('startDate', startDate);
      if (endDate) params.append('endDate', endDate);
      if (deviceId) params.append('deviceId', deviceId);
      
      const url = `${API_BASE_URL}/api/audit/${type}?${params.toString()}`;
      const response = await fetch(url, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      
      if (!response.ok) {
        throw new Error(`Failed to fetch ${type}: ${response.statusText}`);
      }
      
      return await response.json();
    } catch (error) {
      console.error(`Error fetching ${type} by date range:`, error);
      throw error;
    }
  }

  /**
   * Search audit logs by keyword
   * @param {string} type - Type of data to search
   * @param {string} keyword - Search keyword
   * @param {string} deviceId - Optional device ID to filter by
   * @returns {Promise<Array>} Search results
   */
  async searchAuditData(type, keyword, deviceId = null) {
    try {
      const params = new URLSearchParams();
      params.append('search', keyword);
      if (deviceId) params.append('deviceId', deviceId);
      
      const url = `${API_BASE_URL}/api/audit/${type}/search?${params.toString()}`;
      const response = await fetch(url, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      
      if (!response.ok) {
        throw new Error(`Failed to search ${type}: ${response.statusText}`);
      }
      
      return await response.json();
    } catch (error) {
      console.error(`Error searching ${type}:`, error);
      throw error;
    }
  }

  /**
   * Get audit statistics for dashboard
   * @param {string} period - Time period ('day', 'week', 'month', 'year')
   * @returns {Promise<Object>} Statistics object
   */
  async getAuditStatistics(period = 'week') {
    try {
      const response = await fetch(`${API_BASE_URL}/api/audit/statistics?period=${period}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      
      if (!response.ok) {
        throw new Error(`Failed to fetch audit statistics: ${response.statusText}`);
      }
      
      return await response.json();
    } catch (error) {
      console.error('Error fetching audit statistics:', error);
      throw error;
    }
  }

  /**
   * Refresh audit data cache
   * @returns {Promise<Object>} Refresh status
   */
  async refreshAuditCache() {
    try {
      const response = await fetch(`${API_BASE_URL}/api/audit/refresh-cache`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      
      if (!response.ok) {
        throw new Error(`Failed to refresh audit cache: ${response.statusText}`);
      }
      
      return await response.json();
    } catch (error) {
      console.error('Error refreshing audit cache:', error);
      throw error;
    }
  }

  /**
   * Batch export multiple audit data types
   * @param {Array} types - Array of data types to export
   * @param {string} deviceId - Optional device ID to filter by
   * @returns {Promise<Blob>} ZIP file containing CSV files
   */
  async batchExport(types, deviceId = null) {
    try {
      const params = new URLSearchParams();
      types.forEach(type => params.append('types', type));
      if (deviceId) params.append('deviceId', deviceId);
      
      const response = await fetch(`${API_BASE_URL}/api/audit/batch-export?${params.toString()}`, {
        method: 'GET',
        headers: {
          'Accept': 'application/zip',
        },
      });
      
      if (!response.ok) {
        throw new Error(`Failed to batch export: ${response.statusText}`);
      }
      
      return await response.blob();
    } catch (error) {
      console.error('Error batch exporting:', error);
      throw error;
    }
  }

  /**
   * Format timestamp for display
   * @param {string|Date} timestamp - Timestamp to format
   * @returns {string} Formatted timestamp
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
        second: '2-digit',
        hour12: true
      });
    } catch (error) {
      console.error('Error formatting timestamp:', error);
      return timestamp;
    }
  }

  /**
   * Validate API response
   * @param {Response} response - Fetch response object
   * @returns {Promise<Object>} Validated JSON response
   */
  async validateResponse(response) {
    if (!response.ok) {
      let errorMessage = `HTTP ${response.status}: ${response.statusText}`;
      
      try {
        const errorData = await response.json();
        if (errorData.message) {
          errorMessage = errorData.message;
        }
      } catch (parseError) {
        // If we can't parse the error response, use the default message
        console.warn('Could not parse error response:', parseError);
      }
      
      throw new Error(errorMessage);
    }
    
    return await response.json();
  }
}

// Create and export a singleton instance
const auditService = new AuditService();
export default auditService;

// Named exports for individual functions if needed
export const {
  getLocationChanges,
  getQuarantineHistory,
  getFirmwareLogs,
  getAuditSummary,
  getDevices,
  exportToCsv,
  downloadCsv,
  getAuditDataByDateRange,
  searchAuditData,
  getAuditStatistics,
  refreshAuditCache,
  batchExport,
  formatTimestamp
} = auditService;