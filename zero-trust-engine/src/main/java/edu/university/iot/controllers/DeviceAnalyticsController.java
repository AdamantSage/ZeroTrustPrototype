package edu.university.iot.controllers;

import edu.university.iot.model.dtoModel.*;
import edu.university.iot.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for enhanced device analytics and risk assessment.
 * Provides detailed insights for frontend dashboard integration.
 */
@RestController
@RequestMapping("/api/analytics")
public class DeviceAnalyticsController {

    private static final Logger logger = LoggerFactory.getLogger(DeviceAnalyticsController.class);

    private final DeviceRiskAssessmentService riskAssessmentService;
    private final TrustScoreHistoryService trustHistoryService;
    private final TrustAnalysisService trustAnalysisService;
    private final TrustScoreService trustScoreService;
    private final TelemetryProcessorService telemetryProcessorService;
    private final LocationService locationService;

    public DeviceAnalyticsController(
            DeviceRiskAssessmentService riskAssessmentService,
            TrustScoreHistoryService trustHistoryService,
            TrustAnalysisService trustAnalysisService,
            TrustScoreService trustScoreService,
            TelemetryProcessorService telemetryProcessorService,
            LocationService locationService) {
        
        this.riskAssessmentService = riskAssessmentService;
        this.trustHistoryService = trustHistoryService;
        this.trustAnalysisService = trustAnalysisService;
        this.trustScoreService = trustScoreService;
        this.telemetryProcessorService = telemetryProcessorService;
        this.locationService = locationService;
    }

    // === DEVICE-SPECIFIC ANALYTICS ===

    /**
     * Get comprehensive risk assessment for a specific device
     * This is what you'll call when a device is clicked on the map
     */
    @GetMapping("/device/{deviceId}/risk-assessment")
    public ResponseEntity<DeviceRiskAssessmentDto> getDeviceRiskAssessment(
            @PathVariable String deviceId) {
        try {
            logger.info("Getting risk assessment for device: {}", deviceId);
            DeviceRiskAssessmentDto assessment = riskAssessmentService.getDeviceRiskAssessment(deviceId);
            return ResponseEntity.ok(assessment);
        } catch (Exception e) {
            logger.error("Error getting risk assessment for device [{}]: {}", deviceId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get detailed trust score analysis showing what factors are affecting trust
     */
    @GetMapping("/device/{deviceId}/trust-analysis")
    public ResponseEntity<TrustAnalysisDto> getDeviceTrustAnalysis(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "24") int hours) {
        try {
            logger.info("Getting trust analysis for device: {} over {} hours", deviceId, hours);
            
            // Get detailed trust factor analysis
            TrustAnalysisDto analysis = trustAnalysisService.getTrustAnalysis(deviceId);
            
            // Add trust change analysis
            TrustChangeAnalysisDto changeAnalysis = trustHistoryService.analyzeTrustChanges(deviceId, hours);
            
            // Enhance the response with change data
            Map<String, Object> enhancedResponse = new HashMap<>();
            enhancedResponse.put("trustAnalysis", analysis);
            enhancedResponse.put("changeAnalysis", changeAnalysis);
            enhancedResponse.put("analysisPeriod", hours + " hours");
            
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            logger.error("Error getting trust analysis for device [{}]: {}", deviceId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get trust score timeline for visualization (charts/graphs)
     */
    @GetMapping("/device/{deviceId}/trust-timeline")
    public ResponseEntity<List<TrustScoreTimelineDto>> getTrustScoreTimeline(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "7") int days) {
        try {
            logger.info("Getting trust timeline for device: {} over {} days", deviceId, days);
            List<TrustScoreTimelineDto> timeline = trustHistoryService.getTrustScoreTimeline(deviceId, days);
            return ResponseEntity.ok(timeline);
        } catch (Exception e) {
            logger.error("Error getting trust timeline for device [{}]: {}", deviceId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get what specifically changed recently for a device (for "What Changed?" feature)
     */
    @GetMapping("/device/{deviceId}/recent-changes")
    public ResponseEntity<Map<String, Object>> getRecentChanges(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "6") int hours) {
        try {
            logger.info("Getting recent changes for device: {} in last {} hours", deviceId, hours);
            
            Map<String, Object> changes = new HashMap<>();
            
            // Trust score changes
            TrustChangeAnalysisDto trustChanges = trustHistoryService.analyzeTrustChanges(deviceId, hours);
            changes.put("trustChanges", trustChanges);
            
            // Location changes
            var locationChanges = locationService.getLocationHistory(deviceId, hours);
            changes.put("locationChanges", locationChanges);
            
            // Location statistics
            var locationStats = locationService.getLocationStatistics(deviceId);
            changes.put("locationStatistics", locationStats);
            
            // Current trust score breakdown
            var trustBreakdown = trustScoreService.getTrustScoreBreakdown(deviceId);
            changes.put("trustBreakdown", trustBreakdown);
            
            changes.put("analysisPeriod", hours + " hours");
            changes.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(changes);
        } catch (Exception e) {
            logger.error("Error getting recent changes for device [{}]: {}", deviceId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get device behavior patterns and anomalies
     */
    @GetMapping("/device/{deviceId}/behavior-analysis")
    public ResponseEntity<Map<String, Object>> getDeviceBehaviorAnalysis(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "24") int hours) {
        try {
            logger.info("Getting behavior analysis for device: {} over {} hours", deviceId, hours);
            
            Map<String, Object> analysis = new HashMap<>();
            
            // Get trust analysis which includes behavioral factors
            TrustAnalysisDto trustAnalysis = trustAnalysisService.getTrustAnalysis(deviceId);
            analysis.put("behavioralFactors", trustAnalysis.getTrustFactors());
            
            // Get location patterns
            var locationStats = locationService.getLocationStatistics(deviceId);
            analysis.put("locationPatterns", locationStats);
            
            // Get trust change patterns
            TrustChangeAnalysisDto changeAnalysis = trustHistoryService.analyzeTrustChanges(deviceId, hours);
            analysis.put("trustPatterns", changeAnalysis.getPatterns());
            analysis.put("criticalEvents", changeAnalysis.getCriticalEvents());
            
            analysis.put("analysisPeriod", hours + " hours");
            analysis.put("riskLevel", changeAnalysis.getRiskLevel());
            analysis.put("summary", changeAnalysis.getSummary());
            
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            logger.error("Error getting behavior analysis for device [{}]: {}", deviceId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // === SYSTEM-WIDE ANALYTICS ===

    /**
     * Get system-wide risk overview for dashboard
     */
    @GetMapping("/system/risk-overview")
    public ResponseEntity<Map<String, Object>> getSystemRiskOverview() {
        try {
            logger.info("Getting system risk overview");
            Map<String, Object> overview = riskAssessmentService.getSystemRiskOverview();
            
            // Add processing statistics
            Map<String, Object> processingStats = telemetryProcessorService.getProcessingStatistics();
            overview.put("processingStats", processingStats);
            
            // Add trust score statistics
            Map<String, Object> trustStats = trustScoreService.getSystemTrustStatistics();
            overview.put("trustStats", trustStats);
            
            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            logger.error("Error getting system risk overview: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get devices requiring immediate attention (for alerts/notifications)
     */
    @GetMapping("/system/devices-requiring-attention")
    public ResponseEntity<List<Map<String, Object>>> getDevicesRequiringAttention() {
        try {
            logger.info("Getting devices requiring attention");
            List<Map<String, Object>> devices = riskAssessmentService.getDevicesRequiringAttention();
            return ResponseEntity.ok(devices);
        } catch (Exception e) {
            logger.error("Error getting devices requiring attention: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get location map data with enhanced risk indicators
     */
    @GetMapping("/system/location-map")
    public ResponseEntity<Map<String, Object>> getEnhancedLocationMapData() {
        try {
            logger.info("Getting enhanced location map data");
            Map<String, Object> mapData = locationService.getLocationMapData();
            
            // Enhance with risk information for each device
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> deviceLocations = (List<Map<String, Object>>) mapData.get("deviceLocations");
            
            if (deviceLocations != null) {
                for (Map<String, Object> deviceLocation : deviceLocations) {
                    String deviceId = (String) deviceLocation.get("deviceId");
                    if (deviceId != null) {
                        try {
                            // Add risk level
                            DeviceRiskAssessmentDto risk = riskAssessmentService.getDeviceRiskAssessment(deviceId);
                            deviceLocation.put("riskLevel", risk.getRiskLevel());
                            deviceLocation.put("activeThreats", risk.getActiveThreats().size());
                            deviceLocation.put("recentAnomalies", risk.getRecentAnomalies());
                            
                            // Add trust score
                            double trustScore = trustScoreService.getTrustScore(deviceId);
                            deviceLocation.put("trustScore", Math.round(trustScore * 100.0) / 100.0);
                            
                        } catch (Exception e) {
                            logger.warn("Could not enhance location data for device {}: {}", deviceId, e.getMessage());
                        }
                    }
                }
            }
            
            return ResponseEntity.ok(mapData);
        } catch (Exception e) {
            logger.error("Error getting enhanced location map data: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // === ADMINISTRATIVE ACTIONS ===

    /**
     * Manually trigger trust score reset for a device (admin function)
     */
    @PostMapping("/device/{deviceId}/reset-trust-score")
    public ResponseEntity<Map<String, Object>> resetDeviceTrustScore(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "50.0") double baselineScore) {
        try {
            logger.info("Manually resetting trust score for device: {} to {}", deviceId, baselineScore);
            
            trustScoreService.resetTrustScore(deviceId, baselineScore);
            
            Map<String, Object> response = new HashMap<>();
            response.put("deviceId", deviceId);
            response.put("newTrustScore", baselineScore);
            response.put("resetTimestamp", System.currentTimeMillis());
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error resetting trust score for device [{}]: {}", deviceId, e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to reset trust score: " + e.getMessage());
            errorResponse.put("deviceId", deviceId);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Simulate trust score change to predict impact (for what-if analysis)
     */
    @PostMapping("/device/{deviceId}/simulate-trust-change")
    public ResponseEntity<Map<String, Object>> simulateTrustScoreChange(
            @PathVariable String deviceId,
            @RequestBody Map<String, Boolean> factorChanges) {
        try {
            logger.info("Simulating trust score change for device: {}", deviceId);
            
            boolean identity = factorChanges.getOrDefault("identityPass", true);
            boolean context = factorChanges.getOrDefault("contextPass", true);
            boolean firmware = factorChanges.getOrDefault("firmwareValid", true);
            boolean anomaly = factorChanges.getOrDefault("anomalyDetected", false);
            boolean compliance = factorChanges.getOrDefault("compliancePassed", true);
            
            double currentScore = trustScoreService.getTrustScore(deviceId);
            double simulatedScore = trustScoreService.simulateTrustScoreChange(
                deviceId, identity, context, firmware, anomaly, compliance);
            
            Map<String, Object> simulation = new HashMap<>();
            simulation.put("deviceId", deviceId);
            simulation.put("currentTrustScore", currentScore);
            simulation.put("simulatedTrustScore", simulatedScore);
            simulation.put("expectedChange", simulatedScore - currentScore);
            simulation.put("factorsUsed", factorChanges);
            simulation.put("wouldBeTrusted", simulatedScore >= 70.0);
            
            return ResponseEntity.ok(simulation);
        } catch (Exception e) {
            logger.error("Error simulating trust change for device [{}]: {}", deviceId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // === HEALTH CHECK ===

    /**
     * Health check endpoint for the analytics service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("service", "Device Analytics API");
        health.put("version", "1.0.0");
        return ResponseEntity.ok(health);
    }
}