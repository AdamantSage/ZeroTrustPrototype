import React, { useState, useEffect } from 'react';
import { 
  Search, 
  RefreshCw, 
  AlertTriangle, 
  Shield, 
  ChevronDown,
  ChevronRight,
  Eye,
  RotateCcw,
  Clock,
  Activity,
  TrendingUp,
  TrendingDown,
  Minus,
  MapPin,
  Cpu,
  HardDrive,
  Wifi,
  CheckCircle,
  XCircle,
  AlertCircle,
  Info
} from 'lucide-react';
import Sidebar from '../components/common/Sidebar';
import Header from '../components/common/Header';
import deviceRegistryService from '../services/deviceRegistryService';

const DeviceRegistryPage = () => {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [devices, setDevices] = useState([]);
  const [filteredDevices, setFilteredDevices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedDevice, setSelectedDevice] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterBy, setFilterBy] = useState('all');
  const [sortBy, setSortBy] = useState('trustScore');
  const [sortOrder, setSortOrder] = useState('desc');
  const [expandedDevice, setExpandedDevice] = useState(null);
  const [showTrustAnalysis, setShowTrustAnalysis] = useState(false);
  const [trustAnalysis, setTrustAnalysis] = useState(null);
  const [trustTimeline, setTrustTimeline] = useState([]);
  const [loadingAnalysis, setLoadingAnalysis] = useState(false);

  const toggleSidebar = () => setSidebarOpen(prev => !prev);

  useEffect(() => {
    loadDevices();
  }, []);

  useEffect(() => {
    let filtered = [...devices];

    if (searchTerm) {
      filtered = filtered.filter(device =>
        device.deviceId.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (device.deviceType || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
        (device.location || '').toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    if (filterBy !== 'all') {
      filtered = filtered.filter(device => {
        switch (filterBy) {
          case 'trusted':
            return device.trusted;
          case 'untrusted':
            return !device.trusted;
          case 'quarantined':
            return device.quarantined;
          case 'critical':
            return device.trustScore < 30;
          case 'high-risk':
            return device.trustScore < 50;
          default:
            return true;
        }
      });
    }

    filtered.sort((a, b) => {
      let aValue = a[sortBy];
      let bValue = b[sortBy];
      
      if (typeof aValue === 'string') {
        aValue = aValue.toLowerCase();
        bValue = bValue.toLowerCase();
      }
      
      const comparison = aValue < bValue ? -1 : aValue > bValue ? 1 : 0;
      return sortOrder === 'asc' ? comparison : -comparison;
    });

    setFilteredDevices(filtered);
  }, [devices, searchTerm, filterBy, sortBy, sortOrder]);

  const loadDevices = async () => {
    try {
      setLoading(true);
      const data = await deviceRegistryService.getDeviceRegistry();
      setDevices(data);
    } catch (error) {
      console.error('Failed to load devices:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadTrustAnalysis = async (deviceId) => {
    try {
      setLoadingAnalysis(true);
      const [analysis, timeline] = await Promise.all([
        deviceRegistryService.getTrustAnalysis(deviceId),
        deviceRegistryService.getTrustScoreTimeline(deviceId, 7)
      ]);
      setTrustAnalysis(analysis);
      setTrustTimeline(timeline);
    } catch (error) {
      console.error('Failed to load trust analysis:', error);
      setTrustAnalysis(null);
      setTrustTimeline([]);
    } finally {
      setLoadingAnalysis(false);
    }
  };

  const handleDeviceSelect = (device) => {
    setSelectedDevice(device);
    setShowTrustAnalysis(true);
    loadTrustAnalysis(device.deviceId);
  };

  const handleExpandDevice = (deviceId) => {
    setExpandedDevice(expandedDevice === deviceId ? null : deviceId);
  };

  const handleResetTrustScore = async (deviceId, baselineScore = 50) => {
    try {
      await deviceRegistryService.resetTrustScore(deviceId, baselineScore);
      await loadDevices();
      if (selectedDevice?.deviceId === deviceId) {
        await loadTrustAnalysis(deviceId);
      }
    } catch (error) {
      console.error('Failed to reset trust score:', error);
    }
  };

  const getTrustScoreColor = (score) => {
    if (score < 30) return 'text-red-600 bg-red-50 border-red-200';
    if (score < 50) return 'text-orange-600 bg-orange-50 border-orange-200';
    if (score < 70) return 'text-yellow-600 bg-yellow-50 border-yellow-200';
    return 'text-green-600 bg-green-50 border-green-200';
  };

  const getTrustScoreIcon = (score) => {
    if (score < 30) return <AlertTriangle className="w-4 h-4" />;
    if (score < 50) return <AlertCircle className="w-4 h-4" />;
    if (score < 70) return <Shield className="w-4 h-4" />;
    return <CheckCircle className="w-4 h-4" />;
  };

  const getDeviceTypeIcon = (deviceType) => {
    const iconMap = {
      'DESKTOP': '🖥️',
      'LAPTOP': '💻',
      'MOBILE': '📱',
      'TABLET': '📲',
      'IOT_SENSOR': '📡',
      'NETWORK_DEVICE': '🌐',
      'SERVER': '🖥️'
    };
    return iconMap[deviceType] || '❓';
  };

  const renderTrustFactorStatus = (status) => {
    const statusConfig = {
      'HIGH_RISK': { 
        color: 'text-red-700', 
        bg: 'bg-red-100', 
        border: 'border-red-200',
        icon: <XCircle className="w-3 h-3" />,
        text: 'High Risk' 
      },
      'MEDIUM_RISK': { 
        color: 'text-orange-700', 
        bg: 'bg-orange-100', 
        border: 'border-orange-200',
        icon: <AlertTriangle className="w-3 h-3" />,
        text: 'Medium Risk' 
      },
      'LOW_RISK': { 
        color: 'text-green-700', 
        bg: 'bg-green-100', 
        border: 'border-green-200',
        icon: <CheckCircle className="w-3 h-3" />,
        text: 'Low Risk' 
      },
      'NO_DATA': { 
        color: 'text-gray-600', 
        bg: 'bg-gray-100', 
        border: 'border-gray-200',
        icon: <Info className="w-3 h-3" />,
        text: 'No Data' 
      }
    };
    
    const config = statusConfig[status] || statusConfig.NO_DATA;
    return (
      <span className={`inline-flex items-center space-x-1 px-2 py-1 rounded-full text-xs font-medium border ${config.color} ${config.bg} ${config.border}`}>
        {config.icon}
        <span>{config.text}</span>
      </span>
    );
  };

  const getTrendIcon = (trend) => {
    switch (trend) {
      case 'IMPROVING':
        return <TrendingUp className="w-4 h-4 text-green-600" />;
      case 'DEGRADING':
        return <TrendingDown className="w-4 h-4 text-red-600" />;
      default:
        return <Minus className="w-4 h-4 text-gray-600" />;
    }
  };

  const renderDeviceRow = (device) => {
    const isExpanded = expandedDevice === device.deviceId;
    const trustColor = getTrustScoreColor(device.trustScore || 0);
    const trustIcon = getTrustScoreIcon(device.trustScore || 0);

    return (
      <div key={device.deviceId} className="border border-gray-200 rounded-lg mb-4 overflow-hidden bg-white shadow-sm">
        <div className="p-4 hover:bg-gray-50 cursor-pointer" onClick={() => handleExpandDevice(device.deviceId)}>
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-4">
              <button className="text-gray-400 hover:text-gray-600">
                {isExpanded ? <ChevronDown className="w-5 h-5" /> : <ChevronRight className="w-5 h-5" />}
              </button>
              
              <div className="flex items-center space-x-3">
                <span className="text-2xl">{getDeviceTypeIcon(device.deviceType)}</span>
                <div>
                  <h3 className="font-semibold text-gray-900">{device.deviceId}</h3>
                  <p className="text-sm text-gray-500">{device.deviceType || 'Unknown'}</p>
                </div>
              </div>
            </div>

            <div className="flex items-center space-x-6">
              <div className="text-center">
                <div className={`flex items-center space-x-2 px-3 py-2 rounded-lg border ${trustColor}`}>
                  {trustIcon}
                  <span className="font-semibold">{(device.trustScore || 0).toFixed(1)}</span>
                </div>
                <p className="text-xs text-gray-500 mt-1">Trust Score</p>
              </div>

              <div className="flex space-x-2">
                {device.quarantined && (
                  <span className="px-2 py-1 bg-red-100 text-red-800 rounded-full text-xs font-medium border border-red-200">
                    Quarantined
                  </span>
                )}
                {device.trusted ? (
                  <span className="px-2 py-1 bg-green-100 text-green-800 rounded-full text-xs font-medium border border-green-200">
                    Trusted
                  </span>
                ) : (
                  <span className="px-2 py-1 bg-yellow-100 text-yellow-800 rounded-full text-xs font-medium border border-yellow-200">
                    Not Trusted
                  </span>
                )}
              </div>

              <div className="flex space-x-2">
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    handleDeviceSelect(device);
                  }}
                  className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors border border-transparent hover:border-blue-200"
                  title="View Trust Analysis"
                >
                  <Eye className="w-4 h-4" />
                </button>
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    handleResetTrustScore(device.deviceId);
                  }}
                  className="p-2 text-gray-600 hover:bg-gray-50 rounded-lg transition-colors border border-transparent hover:border-gray-200"
                  title="Reset Trust Score"
                >
                  <RotateCcw className="w-4 h-4" />
                </button>
              </div>
            </div>
          </div>
        </div>

        {isExpanded && (
          <div className="border-t border-gray-200 bg-gray-50 p-4">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="space-y-3">
                <h4 className="font-medium text-gray-900 flex items-center">
                  <MapPin className="w-4 h-4 mr-2" />
                  Location & Network
                </h4>
                <div className="text-sm space-y-2">
                  <div className="flex justify-between">
                    <span className="text-gray-500">Location:</span>
                    <span className="font-medium">{device.location || 'Unknown'}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">IP Address:</span>
                    <span className="font-medium">{device.ipAddress || 'Unknown'}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">Last Seen:</span>
                    <span className="font-medium">{device.lastSeen ? deviceRegistryService.formatTimeAgo(device.lastSeen) : 'Unknown'}</span>
                  </div>
                </div>
              </div>

              <div className="space-y-3">
                <h4 className="font-medium text-gray-900 flex items-center">
                  <Activity className="w-4 h-4 mr-2" />
                  System Status
                </h4>
                <div className="text-sm space-y-2">
                  <div className="flex justify-between">
                    <span className="text-gray-500">Firmware:</span>
                    <span className="font-medium">{device.firmwareVersion || 'Unknown'}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">Compliance:</span>
                    <span className={`font-medium ${device.compliant ? 'text-green-600' : 'text-red-600'}`}>
                      {device.compliant ? 'Compliant' : 'Non-Compliant'}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">Anomalies:</span>
                    <span className={`font-medium ${(device.recentAnomalies || 0) > 0 ? 'text-orange-600' : 'text-green-600'}`}>
                      {device.recentAnomalies || 0}
                    </span>
                  </div>
                </div>
              </div>

              <div className="space-y-3">
                <h4 className="font-medium text-gray-900 flex items-center">
                  <Shield className="w-4 h-4 mr-2" />
                  Trust Factors
                </h4>
                <div className="space-y-2">
                  {device.trustFactors ? Object.entries(device.trustFactors).map(([factor, status]) => (
                    <div key={factor} className="flex justify-between items-center">
                      <span className="text-sm capitalize">{factor.replace(/([A-Z])/g, ' $1').trim()}:</span>
                      {renderTrustFactorStatus(status)}
                    </div>
                  )) : (
                    <p className="text-sm text-gray-500">No trust factor data available</p>
                  )}
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    );
  };

  const renderTrustAnalysis = () => {
    if (!showTrustAnalysis || !selectedDevice) return null;

    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
        <div className="bg-white rounded-lg max-w-6xl w-full max-h-[95vh] overflow-y-auto shadow-2xl">
          <div className="p-6 border-b border-gray-200 sticky top-0 bg-white">
            <div className="flex justify-between items-center">
              <h2 className="text-2xl font-bold text-gray-900">
                Trust Analysis: {selectedDevice.deviceId}
              </h2>
              <button
                onClick={() => setShowTrustAnalysis(false)}
                className="text-gray-400 hover:text-gray-600 text-3xl font-light"
              >
                ×
              </button>
            </div>
          </div>

          <div className="p-6">
            {loadingAnalysis ? (
              <div className="text-center py-12">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
                <p className="mt-4 text-gray-600">Loading comprehensive trust analysis...</p>
              </div>
            ) : trustAnalysis ? (
              <div className="space-y-8">
                {/* Trust Score Overview */}
                <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                  <div className="text-center p-6 bg-blue-50 rounded-lg border border-blue-200">
                    <div className="text-4xl font-bold text-blue-600 mb-2">
                      {trustAnalysis.currentTrustScore.toFixed(1)}
                    </div>
                    <p className="text-sm text-blue-700 font-medium">Current Trust Score</p>
                  </div>
                  
                  <div className="text-center p-6 bg-gray-50 rounded-lg border border-gray-200">
                    <div className="text-3xl font-bold text-gray-600 mb-2">
                      {trustAnalysis.averageTrustScore7Days ? trustAnalysis.averageTrustScore7Days.toFixed(1) : 'N/A'}
                    </div>
                    <p className="text-sm text-gray-700 font-medium">7-Day Average</p>
                  </div>

                  <div className="text-center p-6 bg-purple-50 rounded-lg border border-purple-200">
                    <div className="flex items-center justify-center mb-2">
                      {getTrendIcon(trustAnalysis.trendDirection)}
                    </div>
                    <div className="text-lg font-bold text-purple-600 capitalize">
                      {(trustAnalysis.trendDirection || 'stable').toLowerCase()}
                    </div>
                    <p className="text-sm text-purple-700 font-medium">Trend</p>
                  </div>

                  <div className="text-center p-6 bg-red-50 rounded-lg border border-red-200">
                    <div className="text-lg font-bold text-red-600 capitalize mb-2">
                      {(trustAnalysis.overallRiskLevel || 'unknown').toLowerCase()}
                    </div>
                    <p className="text-sm text-red-700 font-medium">Risk Level</p>
                  </div>
                </div>

                {/* Trust Factors Analysis */}
                <div>
                  <h3 className="text-xl font-semibold mb-6 text-gray-900">Trust Factors Analysis</h3>
                  <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    {trustAnalysis.factorStatuses && Object.entries(trustAnalysis.factorStatuses).map(([factor, status]) => {
                      const factorData = trustAnalysis.trustFactors[factor];
                      return (
                        <div key={factor} className="p-6 border border-gray-200 rounded-lg bg-white shadow-sm">
                          <div className="flex justify-between items-center mb-4">
                            <h4 className="font-medium text-gray-900 text-lg capitalize">
                              {factor.replace(/([A-Z])/g, ' $1').trim()}
                            </h4>
                            {renderTrustFactorStatus(status)}
                          </div>
                          
                          {factorData && (
                            <div className="space-y-3">
                              {factorData.totalChecks && (
                                <div className="flex justify-between items-center py-2 border-b border-gray-100">
                                  <span className="text-sm font-medium text-gray-700">Total Checks:</span>
                                  <span className="text-sm font-semibold">{factorData.totalChecks}</span>
                                </div>
                              )}
                              {factorData.failures !== undefined && (
                                <div className="flex justify-between items-center py-2 border-b border-gray-100">
                                  <span className="text-sm font-medium text-gray-700">Failures:</span>
                                  <span className="text-sm font-semibold text-red-600">{factorData.failures}</span>
                                </div>
                              )}
                              {factorData.failureRate !== undefined && (
                                <div className="flex justify-between items-center py-2 border-b border-gray-100">
                                  <span className="text-sm font-medium text-gray-700">Failure Rate:</span>
                                  <span className="text-sm font-semibold text-red-600">{factorData.failureRate}%</span>
                                </div>
                              )}
                              {factorData.anomaliesDetected !== undefined && (
                                <div className="flex justify-between items-center py-2 border-b border-gray-100">
                                  <span className="text-sm font-medium text-gray-700">Anomalies:</span>
                                  <span className="text-sm font-semibold text-orange-600">{factorData.anomaliesDetected}</span>
                                </div>
                              )}
                              {factorData.violations !== undefined && (
                                <div className="flex justify-between items-center py-2 border-b border-gray-100">
                                  <span className="text-sm font-medium text-gray-700">Violations:</span>
                                  <span className="text-sm font-semibold text-red-600">{factorData.violations}</span>
                                </div>
                              )}
                              {factorData.locationChanges !== undefined && (
                                <div className="flex justify-between items-center py-2 border-b border-gray-100">
                                  <span className="text-sm font-medium text-gray-700">Location Changes:</span>
                                  <span className="text-sm font-semibold">{factorData.locationChanges}</span>
                                </div>
                              )}
                              {factorData.uniqueLocations && factorData.uniqueLocations.length > 0 && (
                                <div className="py-2">
                                  <span className="text-sm font-medium text-gray-700">Recent Locations:</span>
                                  <div className="mt-2 flex flex-wrap gap-2">
                                    {factorData.uniqueLocations.slice(0, 3).map((location, index) => (
                                      <span key={index} className="px-2 py-1 bg-blue-100 text-blue-800 rounded-full text-xs">
                                        {location}
                                      </span>
                                    ))}
                                    {factorData.uniqueLocations.length > 3 && (
                                      <span className="px-2 py-1 bg-gray-100 text-gray-600 rounded-full text-xs">
                                        +{factorData.uniqueLocations.length - 3} more
                                      </span>
                                    )}
                                  </div>
                                </div>
                              )}
                              {factorData.version && (
                                <div className="flex justify-between items-center py-2">
                                  <span className="text-sm font-medium text-gray-700">Version:</span>
                                  <span className="text-sm font-semibold">{factorData.version}</span>
                                </div>
                              )}
                            </div>
                          )}
                        </div>
                      );
                    })}
                  </div>
                </div>

                {/* Trust Score Timeline */}
                {trustTimeline.length > 0 && (
                  <div>
                    <h3 className="text-xl font-semibold mb-6 text-gray-900">Trust Score Timeline (7 Days)</h3>
                    <div className="bg-gray-50 rounded-lg p-6 border border-gray-200">
                      <div className="space-y-4">
                        {trustTimeline.slice(0, 10).map((point, index) => (
                          <div key={index} className="flex items-center justify-between p-4 bg-white rounded-lg border border-gray-200 shadow-sm">
                            <div className="flex items-center space-x-4">
                              <div className="text-sm text-gray-500 font-medium">
                                {deviceRegistryService.formatTimestamp(point.timestamp)}
                              </div>
                              <div className="flex items-center space-x-3">
                                <span className={`px-3 py-1 rounded-lg border text-sm font-semibold ${getTrustScoreColor(point.trustScore)}`}>
                                  {point.trustScore.toFixed(1)}
                                </span>
                                {point.scoreChange && (
                                  <span className={`text-sm font-medium ${point.scoreChange > 0 ? 'text-green-600' : 'text-red-600'}`}>
                                    ({point.scoreChange > 0 ? '+' : ''}{point.scoreChange.toFixed(1)})
                                  </span>
                                )}
                              </div>
                            </div>
                            <div className="text-sm text-gray-600 max-w-md text-right font-medium">
                              {point.description}
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                )}

                {/* Risk and Positive Indicators */}
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                  {trustAnalysis.riskIndicators && trustAnalysis.riskIndicators.length > 0 && (
                    <div>
                      <h3 className="text-lg font-semibold mb-4 text-red-600 flex items-center">
                        <AlertTriangle className="w-5 h-5 mr-2" />
                        Risk Indicators
                      </h3>
                      <div className="space-y-3">
                        {trustAnalysis.riskIndicators.map((indicator, index) => (
                          <div key={index} className="flex items-start space-x-3 p-4 bg-red-50 rounded-lg border border-red-200">
                            <AlertTriangle className="w-5 h-5 text-red-600 mt-0.5 flex-shrink-0" />
                            <span className="text-sm text-red-800 font-medium">{indicator}</span>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                  {trustAnalysis.positiveIndicators && trustAnalysis.positiveIndicators.length > 0 && (
                    <div>
                      <h3 className="text-lg font-semibold mb-4 text-green-600 flex items-center">
                        <CheckCircle className="w-5 h-5 mr-2" />
                        Positive Indicators
                      </h3>
                      <div className="space-y-3">
                        {trustAnalysis.positiveIndicators.map((indicator, index) => (
                          <div key={index} className="flex items-start space-x-3 p-4 bg-green-50 rounded-lg border border-green-200">
                            <CheckCircle className="w-5 h-5 text-green-600 mt-0.5 flex-shrink-0" />
                            <span className="text-sm text-green-800 font-medium">{indicator}</span>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                </div>

                {/* Recommendations */}
                {trustAnalysis.actionableRecommendations && trustAnalysis.actionableRecommendations.length > 0 && (
                  <div>
                    <h3 className="text-lg font-semibold mb-4 text-blue-600 flex items-center">
                      <Info className="w-5 h-5 mr-2" />
                      Recommendations
                    </h3>
                    <div className="space-y-3">
                      {trustAnalysis.actionableRecommendations.map((recommendation, index) => (
                        <div key={index} className="flex items-start space-x-3 p-4 bg-blue-50 rounded-lg border border-blue-200">
                          <Info className="w-5 h-5 text-blue-600 mt-0.5 flex-shrink-0" />
                          <span className="text-sm text-blue-800 font-medium">{recommendation}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* Performance Metrics */}
                {(trustAnalysis.complianceMetrics || trustAnalysis.reliabilityMetrics) && (
                  <div>
                    <h3 className="text-lg font-semibold mb-4 text-gray-900">Performance Metrics</h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      {trustAnalysis.complianceMetrics && (
                        <div className="p-6 border border-gray-200 rounded-lg bg-white shadow-sm">
                          <h4 className="font-medium text-gray-900 mb-4 flex items-center">
                            <Shield className="w-4 h-4 mr-2" />
                            Compliance Metrics
                          </h4>
                          {Object.entries(trustAnalysis.complianceMetrics).map(([metric, value]) => (
                            <div key={metric} className="flex justify-between items-center py-2 border-b border-gray-100 last:border-b-0">
                              <span className="text-sm text-gray-600 capitalize font-medium">
                                {metric.replace(/([A-Z])/g, ' $1').trim()}:
                              </span>
                              <div className="flex items-center space-x-2">
                                <span className="font-semibold text-lg">{value}%</span>
                                <div className={`w-12 h-2 rounded-full ${value >= 80 ? 'bg-green-200' : value >= 60 ? 'bg-yellow-200' : 'bg-red-200'}`}>
                                  <div 
                                    className={`h-full rounded-full ${value >= 80 ? 'bg-green-500' : value >= 60 ? 'bg-yellow-500' : 'bg-red-500'}`}
                                    style={{ width: `${value}%` }}
                                  ></div>
                                </div>
                              </div>
                            </div>
                          ))}
                        </div>
                      )}
                      
                      {trustAnalysis.reliabilityMetrics && (
                        <div className="p-6 border border-gray-200 rounded-lg bg-white shadow-sm">
                          <h4 className="font-medium text-gray-900 mb-4 flex items-center">
                            <Activity className="w-4 h-4 mr-2" />
                            Reliability Metrics
                          </h4>
                          {Object.entries(trustAnalysis.reliabilityMetrics).map(([metric, value]) => (
                            <div key={metric} className="flex justify-between items-center py-2 border-b border-gray-100 last:border-b-0">
                              <span className="text-sm text-gray-600 capitalize font-medium">
                                {metric.replace(/([A-Z])/g, ' $1').trim()}:
                              </span>
                              <div className="flex items-center space-x-2">
                                <span className="font-semibold text-lg">{value}%</span>
                                <div className={`w-12 h-2 rounded-full ${value >= 80 ? 'bg-green-200' : value >= 60 ? 'bg-yellow-200' : 'bg-red-200'}`}>
                                  <div 
                                    className={`h-full rounded-full ${value >= 80 ? 'bg-green-500' : value >= 60 ? 'bg-yellow-500' : 'bg-red-500'}`}
                                    style={{ width: `${value}%` }}
                                  ></div>
                                </div>
                              </div>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  </div>
                )}

                {/* Actions */}
                <div className="flex space-x-4 pt-6 border-t border-gray-200">
                  <button
                    onClick={() => handleResetTrustScore(selectedDevice.deviceId)}
                    className="flex items-center space-x-2 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors shadow-sm"
                  >
                    <RotateCcw className="w-4 h-4" />
                    <span>Reset Trust Score</span>
                  </button>
                  <button
                    onClick={() => loadTrustAnalysis(selectedDevice.deviceId)}
                    className="flex items-center space-x-2 px-6 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors shadow-sm"
                  >
                    <RefreshCw className="w-4 h-4" />
                    <span>Refresh Analysis</span>
                  </button>
                </div>
              </div>
            ) : (
              <div className="text-center py-12">
                <AlertTriangle className="w-16 h-16 text-gray-400 mx-auto mb-4" />
                <p className="text-gray-600 text-lg font-medium">Unable to load trust analysis</p>
                <p className="text-gray-500 text-sm mt-2">Please try again or check the device connection</p>
                <button
                  onClick={() => loadTrustAnalysis(selectedDevice.deviceId)}
                  className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                >
                  Retry Analysis
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="flex h-screen w-screen overflow-hidden bg-gray-100 text-gray-800">
      {/* Sidebar */}
      <Sidebar isOpen={sidebarOpen} onToggle={toggleSidebar} />

      {/* Main content wrapper */}
      <div className="flex flex-col flex-1 w-full transition-all duration-300 lg:ml-64">
        {/* Header */}
        <Header onMenuToggle={toggleSidebar} />

        {/* Main section */}
        <main className="flex-1 w-full max-w-full p-6 overflow-y-auto">
          {/* Page Header */}
          <div className="mb-6 flex flex-col lg:flex-row lg:items-center lg:justify-between">
            <div>
              <h1 className="text-3xl font-bold mb-2">📱 Device Registry</h1>
              <p className="text-gray-600">Comprehensive view of all registered devices with advanced trust score analysis</p>
            </div>
            <div className="mt-4 lg:mt-0">
              <button
                onClick={loadDevices}
                disabled={loading}
                className="flex items-center space-x-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors shadow-sm"
              >
                <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
                <span>{loading ? 'Loading...' : 'Refresh'}</span>
              </button>
            </div>
          </div>

          {/* Controls */}
          <div className="mb-6 flex flex-col sm:flex-row gap-4">
            <div className="flex-1">
              <div className="relative">
                <Search className="absolute left-3 top-3 w-4 h-4 text-gray-400" />
                <input
                  type="text"
                  placeholder="Search devices by ID, type, or location..."
                  className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent shadow-sm"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
            </div>

            <div className="flex space-x-2">
              <select
                className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 shadow-sm"
                value={filterBy}
                onChange={(e) => setFilterBy(e.target.value)}
              >
                <option value="all">All Devices</option>
                <option value="trusted">Trusted</option>
                <option value="untrusted">Untrusted</option>
                <option value="quarantined">Quarantined</option>
                <option value="critical">Critical Risk</option>
                <option value="high-risk">High Risk</option>
              </select>

              <select
                className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 shadow-sm"
                value={`${sortBy}-${sortOrder}`}
                onChange={(e) => {
                  const [field, order] = e.target.value.split('-');
                  setSortBy(field);
                  setSortOrder(order);
                }}
              >
                <option value="trustScore-desc">Trust Score (High to Low)</option>
                <option value="trustScore-asc">Trust Score (Low to High)</option>
                <option value="deviceId-asc">Device ID (A-Z)</option>
                <option value="deviceId-desc">Device ID (Z-A)</option>
                <option value="lastSeen-desc">Last Seen (Recent)</option>
              </select>
            </div>
          </div>

          {/* Stats Summary */}
          <div className="mb-6 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
              <div className="text-3xl font-bold text-gray-900">{filteredDevices.length}</div>
              <div className="text-sm text-gray-600 font-medium">Total Devices</div>
            </div>
            <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
              <div className="text-3xl font-bold text-green-600">
                {filteredDevices.filter(d => d.trusted).length}
              </div>
              <div className="text-sm text-gray-600 font-medium">Trusted</div>
            </div>
            <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
              <div className="text-3xl font-bold text-red-600">
                {filteredDevices.filter(d => d.quarantined).length}
              </div>
              <div className="text-sm text-gray-600 font-medium">Quarantined</div>
            </div>
            <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
              <div className="text-3xl font-bold text-orange-600">
                {filteredDevices.filter(d => (d.trustScore || 0) < 50).length}
              </div>
              <div className="text-sm text-gray-600 font-medium">High Risk</div>
            </div>
          </div>

          {/* Device List */}
          <div className="space-y-4">
            {loading ? (
              <div className="bg-white rounded-lg shadow-sm p-12 text-center">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
                <p className="text-gray-600 font-medium">Loading devices...</p>
              </div>
            ) : filteredDevices.length === 0 ? (
              <div className="bg-white rounded-lg shadow-sm p-12 text-center">
                <Search className="w-16 h-16 text-gray-400 mx-auto mb-4" />
                <p className="text-gray-600 font-medium text-lg">No devices found matching your criteria</p>
                <p className="text-gray-500 text-sm mt-2">Try adjusting your search terms or filters</p>
              </div>
            ) : (
              filteredDevices.map(renderDeviceRow)
            )}
          </div>

          {/* Trust Analysis Modal */}
          {renderTrustAnalysis()}
        </main>
      </div>

      {/* Mobile backdrop */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-black bg-opacity-50 z-20 lg:hidden"
          onClick={toggleSidebar}
        />
      )}
    </div>
  );
};

export default DeviceRegistryPage;