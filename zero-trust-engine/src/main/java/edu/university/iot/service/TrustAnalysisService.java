package edu.university.iot.service;

import edu.university.iot.entity.DeviceRegistry;
import edu.university.iot.model.dtoModel.TrustAnalysisDto;
import edu.university.iot.model.dtoModel.TrustChangeAnalysisDto;
import edu.university.iot.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for comprehensive trust analysis of devices.
 * Provides detailed insights into trust factors and their impact.
 */
@Service
public class TrustAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(TrustAnalysisService.class);

    private final DeviceRegistryRepository registryRepo;
    private final TrustScoreHistoryService trustHistoryService;
    private final TrustScoreService trustScoreService;
    private final LocationService locationService;
    private final AnomalyLogRepository anomalyRepo;
    private final ComplianceLogRepository complianceRepo;
    private final FirmwareLogRepository firmwareRepo;
    private final IdentityLogRepository identityRepo;

    // Trust scoring weights (should match TrustScoreService)
    private static final double TRUSTED_THRESHOLD = 70.0;
    private static final Map<String, Double> FACTOR_WEIGHTS = Map.of(
        "identity", 5.0,
        "context", 2.0,
        "firmware", 5.0,
        "anomaly", 10.0,
        "compliance", 10.0
    );

    public TrustAnalysisService(
            DeviceRegistryRepository registryRepo,
            TrustScoreHistoryService trustHistoryService,
            TrustScoreService trustScoreService,
            LocationService locationService,
            AnomalyLogRepository anomalyRepo,
            ComplianceLogRepository complianceRepo,
            FirmwareLogRepository firmwareRepo,
            IdentityLogRepository identityRepo) {
        
        this.registryRepo = registryRepo;
        this.trustHistoryService = trustHistoryService;
        this.trustScoreService = trustScoreService;
        this.locationService = locationService;
        this.anomalyRepo = anomalyRepo;
        this.complianceRepo = complianceRepo;
        this.firmwareRepo = firmwareRepo;
        this.identityRepo = identityRepo;
    }

    /**
     * Get comprehensive trust analysis for a device
     */
    public TrustAnalysisDto getTrustAnalysis(String deviceId) {
        TrustAnalysisDto analysis = new TrustAnalysisDto();
        analysis.setDeviceId(deviceId);

        try {
            DeviceRegistry device = registryRepo.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found: " + deviceId));

            // Basic trust information
            double currentTrustScore = device.getTrustScore() != null ? device.getTrustScore() : 50.0;
            analysis.setCurrentTrustScore(currentTrustScore);
            analysis.setTrusted(device.isTrusted());
            analysis.setTrustThreshold(TRUSTED_THRESHOLD);
            analysis.setLastUpdated(Instant.now());

            // Analyze trust factors
            analyzeTrustFactors(deviceId, analysis);

            // Historical context
            analyzeHistoricalContext(deviceId, analysis);

            // Risk assessment
            performRiskAssessment(deviceId, analysis, currentTrustScore);

            // Generate recommendations
            generateRecommendations(deviceId, analysis);

            // Performance metrics
            calculatePerformanceMetrics(deviceId, analysis);

        } catch (Exception e) {
            logger.error("Error performing trust analysis for device [{}]: {}", deviceId, e.getMessage(), e);
            analysis.setOverallRiskLevel("ERROR");
            analysis.setRiskIndicators(List.of("Analysis failed: " + e.getMessage()));
        }

        return analysis;
    }

    // === PRIVATE HELPER METHODS ===

    private void analyzeTrustFactors(String deviceId, TrustAnalysisDto analysis) {
        Map<String, Object> trustFactors = new HashMap<>();
        Map<String, String> factorStatuses = new HashMap<>();
        
        try {
            Instant cutoff = Instant.now().minusSeconds(24 * 3600); // 24 hours
            
            // Identity factor analysis
            var identityLogs = identityRepo.findByDeviceId(deviceId).stream()
                .filter(log -> log.getTimestamp().isAfter(cutoff))
                .collect(Collectors.toList());
            
            Map<String, Object> identityAnalysis = new HashMap<>();
            if (!identityLogs.isEmpty()) {
                long totalChecks = identityLogs.size();
                long failures = identityLogs.stream()
                    .mapToLong(log -> log.isIdentityVerified() ? 0L : 1L)
                    .sum();
                double failureRate = (double) failures / totalChecks;
                
                identityAnalysis.put("totalChecks", totalChecks);
                identityAnalysis.put("failures", failures);
                identityAnalysis.put("failureRate", Math.round(failureRate * 1000.0) / 10.0); // percentage
                identityAnalysis.put("lastCheck", identityLogs.get(0).getTimestamp());
                
                if (failureRate > 0.3) factorStatuses.put("identity", "HIGH_RISK");
                else if (failureRate > 0.1) factorStatuses.put("identity", "MEDIUM_RISK");
                else factorStatuses.put("identity", "LOW_RISK");
            } else {
                factorStatuses.put("identity", "NO_DATA");
            }
            trustFactors.put("identity", identityAnalysis);
            
            // Location/Context factor analysis
            var locationChanges = locationService.getLocationHistory(deviceId, 24);
            Map<String, Object> contextAnalysis = new HashMap<>();
            contextAnalysis.put("locationChanges", locationChanges.size());
            contextAnalysis.put("locations", locationChanges);
            
            if (locationChanges.size() > 5) factorStatuses.put("context", "HIGH_RISK");
            else if (locationChanges.size() > 2) factorStatuses.put("context", "MEDIUM_RISK");
            else factorStatuses.put("context", "LOW_RISK");
            trustFactors.put("context", contextAnalysis);
            
            // Anomaly factor analysis
            var anomalyLogs = anomalyRepo.findByDeviceId(deviceId).stream()
                .filter(log -> log.getTimestamp().isAfter(cutoff))
                .collect(Collectors.toList());
            
            Map<String, Object> anomalyAnalysis = new HashMap<>();
            if (!anomalyLogs.isEmpty()) {
                long totalChecks = anomalyLogs.size();
                long anomalies = anomalyLogs.stream()
                    .mapToLong(log -> log.isAnomalyDetected() ? 1L : 0L)
                    .sum();
                double anomalyRate = (double) anomalies / totalChecks;
                
                anomalyAnalysis.put("totalChecks", totalChecks);
                anomalyAnalysis.put("anomaliesDetected", anomalies);
                anomalyAnalysis.put("anomalyRate", Math.round(anomalyRate * 1000.0) / 10.0);
                
                if (anomalyRate > 0.3) factorStatuses.put("behavior", "HIGH_RISK");
                else if (anomalyRate > 0.1) factorStatuses.put("behavior", "MEDIUM_RISK");
                else factorStatuses.put("behavior", "LOW_RISK");
            } else {
                factorStatuses.put("behavior", "NO_DATA");
            }
            trustFactors.put("behavior", anomalyAnalysis);
            
            // Compliance factor analysis
            var complianceLogs = complianceRepo.findByDeviceId(deviceId).stream()
                .filter(log -> log.getTimestamp().isAfter(cutoff))
                .collect(Collectors.toList());
            
            Map<String, Object> complianceAnalysis = new HashMap<>();
            if (!complianceLogs.isEmpty()) {
                long totalChecks = complianceLogs.size();
                long violations = complianceLogs.stream()
                    .mapToLong(log -> log.isCompliant() ? 0L : 1L)
                    .sum();
                double violationRate = (double) violations / totalChecks;
                
                complianceAnalysis.put("totalChecks", totalChecks);
                complianceAnalysis.put("violations", violations);
                complianceAnalysis.put("violationRate", Math.round(violationRate * 1000.0) / 10.0);
                
                if (violationRate > 0.3) factorStatuses.put("compliance", "HIGH_RISK");
                else if (violationRate > 0.1) factorStatuses.put("compliance", "MEDIUM_RISK");
                else factorStatuses.put("compliance", "LOW_RISK");
            } else {
                factorStatuses.put("compliance", "NO_DATA");
            }
            trustFactors.put("compliance", complianceAnalysis);
            
            // Firmware factor analysis
            var firmwareLog = firmwareRepo.findTopByDeviceIdOrderByTimestampDesc(deviceId);
            Map<String, Object> firmwareAnalysis = new HashMap<>();
            if (firmwareLog.isPresent()) {
                var fw = firmwareLog.get();
                firmwareAnalysis.put("version", fw.getFirmwareVersion());
                firmwareAnalysis.put("isValid", fw.isFirmwareValid());
                firmwareAnalysis.put("lastCheck", fw.getTimestamp());
                
                factorStatuses.put("firmware", fw.isFirmwareValid() ? "LOW_RISK" : "HIGH_RISK");
            } else {
                factorStatuses.put("firmware", "NO_DATA");
            }
            trustFactors.put("firmware", firmwareAnalysis);
            
            analysis.setTrustFactors(trustFactors);
            analysis.setFactorStatuses(factorStatuses);
            analysis.setFactorWeights(FACTOR_WEIGHTS);
            
        } catch (Exception e) {
            logger.error("Error analyzing trust factors for device [{}]: {}", deviceId, e.getMessage());
        }
    }

    private void analyzeHistoricalContext(String deviceId, TrustAnalysisDto analysis) {
        try {
            // Get historical averages
            var timeline7d = trustHistoryService.getTrustScoreTimeline(deviceId, 7);
            var timeline30d = trustHistoryService.getTrustScoreTimeline(deviceId, 30);
            
            if (!timeline7d.isEmpty()) {
                double avg7d = timeline7d.stream()
                    .mapToDouble(t -> t.getTrustScore())
                    .average()
                    .orElse(analysis.getCurrentTrustScore());
                analysis.setAverageTrustScore7Days(Math.round(avg7d * 100.0) / 100.0);
            }
            
            if (!timeline30d.isEmpty()) {
                double avg30d = timeline30d.stream()
                    .mapToDouble(t -> t.getTrustScore())
                    .average()
                    .orElse(analysis.getCurrentTrustScore());
                analysis.setAverageTrustScore30Days(Math.round(avg30d * 100.0) / 100.0);
            }
            
            // Get trend analysis
            TrustChangeAnalysisDto changeAnalysis = trustHistoryService.analyzeTrustChanges(deviceId, 24);
            analysis.setTrendDirection(changeAnalysis.getTrend());
            analysis.setTrustScoreChanges24h(changeAnalysis.getTotalChanges());
            
        } catch (Exception e) {
            logger.error("Error analyzing historical context for device [{}]: {}", deviceId, e.getMessage());
        }
    }

    private void performRiskAssessment(String deviceId, TrustAnalysisDto analysis, double currentTrustScore) {
        List<String> riskIndicators = new ArrayList<>();
        List<String> positiveIndicators = new ArrayList<>();
        
        try {
            // Assess current trust level
            String riskLevel;
            if (currentTrustScore < 30) {
                riskLevel = "CRITICAL";
                riskIndicators.add("Trust score critically low");
            } else if (currentTrustScore < 50) {
                riskLevel = "HIGH";
                riskIndicators.add("Trust score below acceptable threshold");
            } else if (currentTrustScore < 70) {
                riskLevel = "MEDIUM";
                riskIndicators.add("Trust score needs improvement");
            } else {
                riskLevel = "LOW";
                positiveIndicators.add("Trust score within acceptable range");
            }
            
            // Check factor statuses
            Map<String, String> factorStatuses = analysis.getFactorStatuses();
            for (Map.Entry<String, String> entry : factorStatuses.entrySet()) {
                String factor = entry.getKey();
                String status = entry.getValue();
                
                switch (status) {
                    case "HIGH_RISK":
                        riskIndicators.add(formatFactorRisk(factor) + " - high risk detected");
                        break;
                    case "MEDIUM_RISK":
                        riskIndicators.add(formatFactorRisk(factor) + " - moderate concerns");
                        break;
                    case "LOW_RISK":
                        positiveIndicators.add(formatFactorRisk(factor) + " - performing well");
                        break;
                }
            }
            
            // Check trend
            if ("DEGRADING".equals(analysis.getTrendDirection())) {
                riskIndicators.add("Trust score trending downward");
                if ("LOW".equals(riskLevel)) riskLevel = "MEDIUM"; // Upgrade risk
            } else if ("IMPROVING".equals(analysis.getTrendDirection())) {
                positiveIndicators.add("Trust score trending upward");
            }
            
            analysis.setOverallRiskLevel(riskLevel);
            analysis.setRiskIndicators(riskIndicators);
            analysis.setPositiveIndicators(positiveIndicators);
            
        } catch (Exception e) {
            logger.error("Error performing risk assessment for device [{}]: {}", deviceId, e.getMessage());
        }
    }

    private void generateRecommendations(String deviceId, TrustAnalysisDto analysis) {
        List<String> recommendations = new ArrayList<>();
        
        try {
            // Risk level based recommendations
            switch (analysis.getOverallRiskLevel()) {
                case "CRITICAL":
                    recommendations.add("URGENT: Consider quarantining device immediately");
                    recommendations.add("Perform comprehensive security audit");
                    recommendations.add("Review all device access permissions");
                    break;
                    
                case "HIGH":
                    recommendations.add("Increase monitoring frequency");
                    recommendations.add("Restrict device access to essential services");
                    recommendations.add("Schedule security review within 24 hours");
                    break;
                    
                case "MEDIUM":
                    recommendations.add("Review and address identified risk factors");
                    recommendations.add("Monitor closely for next 48 hours");
                    recommendations.add("Verify firmware and security patches are current");
                    break;
                    
                default:
                    recommendations.add("Continue regular monitoring");
                    recommendations.add("Maintain current security policies");
                    break;
            }
            
            // Factor-specific recommendations
            Map<String, String> factorStatuses = analysis.getFactorStatuses();
            if ("HIGH_RISK".equals(factorStatuses.get("identity"))) {
                recommendations.add("Renew or reconfigure device certificates");
            }
            if ("HIGH_RISK".equals(factorStatuses.get("context"))) {
                recommendations.add("Investigate frequent location/network changes");
            }
            if ("HIGH_RISK".equals(factorStatuses.get("compliance"))) {
                recommendations.add("Enforce compliance through policy updates");
            }
            if ("HIGH_RISK".equals(factorStatuses.get("firmware"))) {
                recommendations.add("Update firmware to compliant version");
            }
            if ("HIGH_RISK".equals(factorStatuses.get("behavior"))) {
                recommendations.add("Investigate root cause of anomalous behavior");
            }
            
            analysis.setActionableRecommendations(recommendations);
            
            // Set next review date based on risk level
            int hoursUntilReview = switch (analysis.getOverallRiskLevel()) {
                case "CRITICAL" -> 4;
                case "HIGH" -> 12;
                case "MEDIUM" -> 48;
                default -> 168; // 1 week
            };
            
            analysis.setNextReviewDate(
                LocalDateTime.now().plusHours(hoursUntilReview).toString()
            );
            
        } catch (Exception e) {
            logger.error("Error generating recommendations for device [{}]: {}", deviceId, e.getMessage());
            recommendations.add("Error generating recommendations - manual review required");
        }
    }

    private void calculatePerformanceMetrics(String deviceId, TrustAnalysisDto analysis) {
        Map<String, Integer> complianceMetrics = new HashMap<>();
        Map<String, Integer> reliabilityMetrics = new HashMap<>();
        
        try {
            Instant cutoff = Instant.now().minusSeconds(7 * 24 * 3600); // 7 days
            
            // Compliance metrics
            var complianceLogs = complianceRepo.findByDeviceId(deviceId).stream()
                .filter(log -> log.getTimestamp().isAfter(cutoff))
                .collect(Collectors.toList());
            
            if (!complianceLogs.isEmpty()) {
                long compliantCount = complianceLogs.stream()
                    .mapToLong(log -> log.isCompliant() ? 1L : 0L)
                    .sum();
                int complianceRate = (int) Math.round((double) compliantCount / complianceLogs.size() * 100);
                complianceMetrics.put("policyCompliance", complianceRate);
            }
            
            // Identity reliability
            var identityLogs = identityRepo.findByDeviceId(deviceId).stream()
                .filter(log -> log.getTimestamp().isAfter(cutoff))
                .collect(Collectors.toList());
            
            if (!identityLogs.isEmpty()) {
                long successCount = identityLogs.stream()
                    .mapToLong(log -> log.isIdentityVerified() ? 1L : 0L)
                    .sum();
                int reliabilityRate = (int) Math.round((double) successCount / identityLogs.size() * 100);
                reliabilityMetrics.put("identityReliability", reliabilityRate);
            }
            
            // Behavioral stability
            var anomalyLogs = anomalyRepo.findByDeviceId(deviceId).stream()
                .filter(log -> log.getTimestamp().isAfter(cutoff))
                .collect(Collectors.toList());
            
            if (!anomalyLogs.isEmpty()) {
                long normalCount = anomalyLogs.stream()
                    .mapToLong(log -> log.isAnomalyDetected() ? 0L : 1L)
                    .sum();
                int stabilityRate = (int) Math.round((double) normalCount / anomalyLogs.size() * 100);
                reliabilityMetrics.put("behavioralStability", stabilityRate);
            }
            
            analysis.setComplianceMetrics(complianceMetrics);
            analysis.setReliabilityMetrics(reliabilityMetrics);
            
        } catch (Exception e) {
            logger.error("Error calculating performance metrics for device [{}]: {}", deviceId, e.getMessage());
        }
    }

    private String formatFactorRisk(String factor) {
        return switch (factor) {
            case "identity" -> "Identity verification";
            case "context" -> "Location/network context";
            case "behavior" -> "Behavioral analysis";
            case "compliance" -> "Policy compliance";
            case "firmware" -> "Firmware validation";
            default -> factor;
        };
    }
}