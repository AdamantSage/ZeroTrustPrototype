package edu.university.iot.service;

import edu.university.iot.entity.DeviceRegistry;
import edu.university.iot.model.TrustScoreHistory;
import edu.university.iot.model.dtoModel.TrustAnalysisDto;
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
 * Enhanced Service for comprehensive trust analysis using trust_score_history data.
 */
@Service
public class TrustAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(TrustAnalysisService.class);

    private final DeviceRegistryRepository registryRepo;
    private final TrustScoreHistoryRepository trustHistoryRepo;
    private final TrustScoreService trustScoreService;

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
            TrustScoreHistoryRepository trustHistoryRepo,
            TrustScoreService trustScoreService) {
        
        this.registryRepo = registryRepo;
        this.trustHistoryRepo = trustHistoryRepo;
        this.trustScoreService = trustScoreService;
    }

    /**
     * Get comprehensive trust analysis for a device using trust_score_history data
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

            // Analyze using trust_score_history data
            analyzeTrustFactorsFromHistory(deviceId, analysis);
            
            // Historical context
            analyzeHistoricalContext(deviceId, analysis);

            // Risk assessment
            performRiskAssessment(deviceId, analysis, currentTrustScore);

            // Generate recommendations
            generateRecommendations(deviceId, analysis);

            // Performance metrics from history
            calculatePerformanceMetricsFromHistory(deviceId, analysis);

        } catch (Exception e) {
            logger.error("Error performing trust analysis for device [{}]: {}", deviceId, e.getMessage(), e);
            analysis.setOverallRiskLevel("ERROR");
            analysis.setRiskIndicators(List.of("Analysis failed: " + e.getMessage()));
        }

        return analysis;
    }

    // === PRIVATE HELPER METHODS ===

    private void analyzeTrustFactorsFromHistory(String deviceId, TrustAnalysisDto analysis) {
        Map<String, Object> trustFactors = new HashMap<>();
        Map<String, String> factorStatuses = new HashMap<>();
        
        try {
            Instant cutoff = Instant.now().minusSeconds(24 * 3600); // 24 hours
            
            // Get recent trust score history
            List<TrustScoreHistory> recentHistory = trustHistoryRepo
                .findByDeviceIdAndTimestampAfterOrderByTimestampDesc(deviceId, cutoff);
            
            if (recentHistory.isEmpty()) {
                // No recent history - check older data
                List<TrustScoreHistory> allHistory = trustHistoryRepo
                    .findByDeviceIdOrderByTimestampDesc(deviceId);
                if (!allHistory.isEmpty()) {
                    recentHistory = allHistory.stream().limit(5).collect(Collectors.toList());
                }
            }

            // Identity factor analysis from history
            Map<String, Object> identityAnalysis = analyzeIdentityFromHistory(recentHistory);
            trustFactors.put("identity", identityAnalysis);
            factorStatuses.put("identity", determineFactorStatus(identityAnalysis, "identity"));
            
            // Context factor analysis from history
            Map<String, Object> contextAnalysis = analyzeContextFromHistory(recentHistory);
            trustFactors.put("context", contextAnalysis);
            factorStatuses.put("context", determineFactorStatus(contextAnalysis, "context"));
            
            // Firmware factor analysis from history
            Map<String, Object> firmwareAnalysis = analyzeFirmwareFromHistory(recentHistory);
            trustFactors.put("firmware", firmwareAnalysis);
            factorStatuses.put("firmware", determineFactorStatus(firmwareAnalysis, "firmware"));
            
            // Anomaly factor analysis from history
            Map<String, Object> anomalyAnalysis = analyzeAnomalyFromHistory(recentHistory);
            trustFactors.put("behavior", anomalyAnalysis);
            factorStatuses.put("behavior", determineFactorStatus(anomalyAnalysis, "anomaly"));
            
            // Compliance factor analysis from history
            Map<String, Object> complianceAnalysis = analyzeComplianceFromHistory(recentHistory);
            trustFactors.put("compliance", complianceAnalysis);
            factorStatuses.put("compliance", determineFactorStatus(complianceAnalysis, "compliance"));
            
            analysis.setTrustFactors(trustFactors);
            analysis.setFactorStatuses(factorStatuses);
            analysis.setFactorWeights(FACTOR_WEIGHTS);
            
        } catch (Exception e) {
            logger.error("Error analyzing trust factors from history for device [{}]: {}", deviceId, e.getMessage());
        }
    }

    private Map<String, Object> analyzeIdentityFromHistory(List<TrustScoreHistory> history) {
        Map<String, Object> analysis = new HashMap<>();
        
        if (history.isEmpty()) {
            analysis.put("status", "NO_DATA");
            return analysis;
        }
        
        long totalChecks = history.size();
        long failures = history.stream()
            .mapToLong(h -> h.isIdentityPassed() ? 0L : 1L)
            .sum();
        double failureRate = totalChecks > 0 ? (double) failures / totalChecks : 0.0;
        
        analysis.put("totalChecks", totalChecks);
        analysis.put("failures", failures);
        analysis.put("failureRate", Math.round(failureRate * 1000.0) / 10.0);
        analysis.put("lastCheck", history.get(0).getTimestamp());
        
        return analysis;
    }
    
    private Map<String, Object> analyzeContextFromHistory(List<TrustScoreHistory> history) {
        Map<String, Object> analysis = new HashMap<>();
        
        if (history.isEmpty()) {
            analysis.put("status", "NO_DATA");
            return analysis;
        }
        
        // Count location changes and context failures
        Set<String> uniqueLocations = history.stream()
            .map(TrustScoreHistory::getLocationAtChange)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
            
        long contextFailures = history.stream()
            .mapToLong(h -> h.isContextPassed() ? 0L : 1L)
            .sum();
            
        analysis.put("locationChanges", uniqueLocations.size());
        analysis.put("uniqueLocations", uniqueLocations);
        analysis.put("contextFailures", contextFailures);
        
        return analysis;
    }
    
    private Map<String, Object> analyzeFirmwareFromHistory(List<TrustScoreHistory> history) {
        Map<String, Object> analysis = new HashMap<>();
        
        if (history.isEmpty()) {
            analysis.put("status", "NO_DATA");
            return analysis;
        }
        
        long totalChecks = history.size();
        long failures = history.stream()
            .mapToLong(h -> h.isFirmwareValid() ? 0L : 1L)
            .sum();
            
        analysis.put("totalChecks", totalChecks);
        analysis.put("failures", failures);
        analysis.put("isValid", failures == 0);
        analysis.put("lastCheck", history.get(0).getTimestamp());
        
        return analysis;
    }
    
    private Map<String, Object> analyzeAnomalyFromHistory(List<TrustScoreHistory> history) {
        Map<String, Object> analysis = new HashMap<>();
        
        if (history.isEmpty()) {
            analysis.put("status", "NO_DATA");
            return analysis;
        }
        
        long totalChecks = history.size();
        long anomalies = history.stream()
            .mapToLong(h -> h.isAnomalyDetected() ? 1L : 0L)
            .sum();
        double anomalyRate = totalChecks > 0 ? (double) anomalies / totalChecks : 0.0;
        
        analysis.put("totalChecks", totalChecks);
        analysis.put("anomaliesDetected", anomalies);
        analysis.put("anomalyRate", Math.round(anomalyRate * 1000.0) / 10.0);
        
        return analysis;
    }
    
    private Map<String, Object> analyzeComplianceFromHistory(List<TrustScoreHistory> history) {
        Map<String, Object> analysis = new HashMap<>();
        
        if (history.isEmpty()) {
            analysis.put("status", "NO_DATA");
            return analysis;
        }
        
        long totalChecks = history.size();
        long violations = history.stream()
            .mapToLong(h -> h.isCompliancePassed() ? 0L : 1L)
            .sum();
        double violationRate = totalChecks > 0 ? (double) violations / totalChecks : 0.0;
        
        analysis.put("totalChecks", totalChecks);
        analysis.put("violations", violations);
        analysis.put("violationRate", Math.round(violationRate * 1000.0) / 10.0);
        
        return analysis;
    }

    private String determineFactorStatus(Map<String, Object> factorData, String factorType) {
        if (factorData.containsKey("status") && "NO_DATA".equals(factorData.get("status"))) {
            return "NO_DATA";
        }
        
        switch (factorType) {
            case "identity":
            case "compliance":
                Double failureRate = (Double) factorData.get(
                    factorType.equals("identity") ? "failureRate" : "violationRate"
                );
                if (failureRate == null) return "NO_DATA";
                if (failureRate > 30.0) return "HIGH_RISK";
                if (failureRate > 10.0) return "MEDIUM_RISK";
                return "LOW_RISK";
                
            case "context":
                Long locationChanges = (Long) factorData.get("locationChanges");
                Long contextFailures = (Long) factorData.get("contextFailures");
                if (locationChanges == null || contextFailures == null) return "NO_DATA";
                if (locationChanges > 5 || contextFailures > 2) return "HIGH_RISK";
                if (locationChanges > 2 || contextFailures > 0) return "MEDIUM_RISK";
                return "LOW_RISK";
                
            case "firmware":
                Boolean isValid = (Boolean) factorData.get("isValid");
                if (isValid == null) return "NO_DATA";
                return isValid ? "LOW_RISK" : "HIGH_RISK";
                
            case "anomaly":
                Double anomalyRate = (Double) factorData.get("anomalyRate");
                if (anomalyRate == null) return "NO_DATA";
                if (anomalyRate > 30.0) return "HIGH_RISK";
                if (anomalyRate > 10.0) return "MEDIUM_RISK";
                return "LOW_RISK";
                
            default:
                return "NO_DATA";
        }
    }

    private void analyzeHistoricalContext(String deviceId, TrustAnalysisDto analysis) {
        try {
            // Get 7-day and 30-day history for averages
            Instant cutoff7d = Instant.now().minusSeconds(7 * 24 * 3600);
            Instant cutoff30d = Instant.now().minusSeconds(30 * 24 * 3600);
            
            List<TrustScoreHistory> history7d = trustHistoryRepo
                .findByDeviceIdAndTimestampAfterOrderByTimestampDesc(deviceId, cutoff7d);
            List<TrustScoreHistory> history30d = trustHistoryRepo
                .findByDeviceIdAndTimestampAfterOrderByTimestampDesc(deviceId, cutoff30d);
            
            if (!history7d.isEmpty()) {
                double avg7d = history7d.stream()
                    .mapToDouble(TrustScoreHistory::getNewScore)
                    .average()
                    .orElse(analysis.getCurrentTrustScore());
                analysis.setAverageTrustScore7Days(Math.round(avg7d * 100.0) / 100.0);
            }
            
            if (!history30d.isEmpty()) {
                double avg30d = history30d.stream()
                    .mapToDouble(TrustScoreHistory::getNewScore)
                    .average()
                    .orElse(analysis.getCurrentTrustScore());
                analysis.setAverageTrustScore30Days(Math.round(avg30d * 100.0) / 100.0);
            }
            
            // Determine trend from recent history
            if (!history7d.isEmpty()) {
                List<TrustScoreHistory> recent5 = history7d.stream().limit(5).collect(Collectors.toList());
                double avgChange = recent5.stream()
                    .mapToDouble(TrustScoreHistory::getScoreChange)
                    .average()
                    .orElse(0.0);
                
                if (avgChange > 2) analysis.setTrendDirection("IMPROVING");
                else if (avgChange < -2) analysis.setTrendDirection("DEGRADING");
                else analysis.setTrendDirection("STABLE");
                
                analysis.setTrustScoreChanges24h(recent5.size());
            }
            
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
                riskIndicators.add("Trust score critically low (" + currentTrustScore + "/100)");
            } else if (currentTrustScore < 50) {
                riskLevel = "HIGH";
                riskIndicators.add("Trust score below acceptable threshold (" + currentTrustScore + "/100)");
            } else if (currentTrustScore < 70) {
                riskLevel = "MEDIUM";
                riskIndicators.add("Trust score needs improvement (" + currentTrustScore + "/100)");
            } else {
                riskLevel = "LOW";
                positiveIndicators.add("Trust score within acceptable range (" + currentTrustScore + "/100)");
            }
            
            // Check factor statuses for additional risk indicators
            Map<String, String> factorStatuses = analysis.getFactorStatuses();
            if (factorStatuses != null) {
                for (Map.Entry<String, String> entry : factorStatuses.entrySet()) {
                    String factor = entry.getKey();
                    String status = entry.getValue();
                    
                    switch (status) {
                        case "HIGH_RISK":
                            riskIndicators.add(formatFactorRisk(factor) + " - high risk detected");
                            if ("LOW".equals(riskLevel)) riskLevel = "MEDIUM"; // Escalate risk
                            break;
                        case "MEDIUM_RISK":
                            riskIndicators.add(formatFactorRisk(factor) + " - moderate concerns");
                            break;
                        case "LOW_RISK":
                            positiveIndicators.add(formatFactorRisk(factor) + " - performing well");
                            break;
                    }
                }
            }
            
            // Check trend
            if ("DEGRADING".equals(analysis.getTrendDirection())) {
                riskIndicators.add("Trust score trending downward over recent period");
                if ("LOW".equals(riskLevel)) riskLevel = "MEDIUM";
            } else if ("IMPROVING".equals(analysis.getTrendDirection())) {
                positiveIndicators.add("Trust score trending upward over recent period");
            }
            
            // Check for recent critical events
            Instant cutoff = Instant.now().minusSeconds(24 * 3600);
            List<TrustScoreHistory> criticalEvents = trustHistoryRepo
                .findByDeviceIdAndTimestampAfterOrderByTimestampDesc(deviceId, cutoff)
                .stream()
                .filter(h -> "CRITICAL".equals(h.getSeverity()) || "HIGH".equals(h.getSeverity()))
                .collect(Collectors.toList());
                
            if (!criticalEvents.isEmpty()) {
                riskIndicators.add("Recent critical trust score events detected (" + criticalEvents.size() + ")");
                if ("LOW".equals(riskLevel) || "MEDIUM".equals(riskLevel)) {
                    riskLevel = "HIGH";
                }
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
                    recommendations.add("Investigate recent critical trust score changes");
                    break;
                    
                case "HIGH":
                    recommendations.add("Increase monitoring frequency for this device");
                    recommendations.add("Restrict device access to essential services");
                    recommendations.add("Schedule security review within 24 hours");
                    recommendations.add("Analyze recent trust score degradation patterns");
                    break;
                    
                case "MEDIUM":
                    recommendations.add("Review and address identified risk factors");
                    recommendations.add("Monitor closely for next 48 hours");
                    recommendations.add("Verify firmware and security patches are current");
                    break;
                    
                default:
                    recommendations.add("Continue regular monitoring schedule");
                    recommendations.add("Maintain current security policies");
                    break;
            }
            
            // Factor-specific recommendations from trust history
            Map<String, String> factorStatuses = analysis.getFactorStatuses();
            if (factorStatuses != null) {
                if ("HIGH_RISK".equals(factorStatuses.get("identity"))) {
                    recommendations.add("Renew or reconfigure device certificates - identity verification failing");
                }
                if ("HIGH_RISK".equals(factorStatuses.get("context"))) {
                    recommendations.add("Investigate frequent location/network changes - unusual mobility pattern");
                }
                if ("HIGH_RISK".equals(factorStatuses.get("compliance"))) {
                    recommendations.add("Enforce compliance through policy updates - violations detected");
                }
                if ("HIGH_RISK".equals(factorStatuses.get("firmware"))) {
                    recommendations.add("Update firmware to compliant version - validation failures");
                }
                if ("HIGH_RISK".equals(factorStatuses.get("behavior"))) {
                    recommendations.add("Investigate anomalous behavior patterns - unusual activity detected");
                }
            }
            
            analysis.setActionableRecommendations(recommendations);
            
            // Set next review date based on risk level
            int hoursUntilReview = switch (analysis.getOverallRiskLevel()) {
                case "CRITICAL" -> 2;
                case "HIGH" -> 8;
                case "MEDIUM" -> 24;
                default -> 72;
            };
            
            analysis.setNextReviewDate(
                LocalDateTime.now().plusHours(hoursUntilReview).toString()
            );
            
        } catch (Exception e) {
            logger.error("Error generating recommendations for device [{}]: {}", deviceId, e.getMessage());
            recommendations.add("Error generating recommendations - manual review required");
            analysis.setActionableRecommendations(recommendations);
        }
    }

    private void calculatePerformanceMetricsFromHistory(String deviceId, TrustAnalysisDto analysis) {
        Map<String, Integer> complianceMetrics = new HashMap<>();
        Map<String, Integer> reliabilityMetrics = new HashMap<>();
        
        try {
            Instant cutoff = Instant.now().minusSeconds(7 * 24 * 3600); // 7 days
            
            List<TrustScoreHistory> weekHistory = trustHistoryRepo
                .findByDeviceIdAndTimestampAfterOrderByTimestampDesc(deviceId, cutoff);
            
            if (!weekHistory.isEmpty()) {
                // Compliance metrics from history
                long compliantCount = weekHistory.stream()
                    .mapToLong(h -> h.isCompliancePassed() ? 1L : 0L)
                    .sum();
                int complianceRate = (int) Math.round((double) compliantCount / weekHistory.size() * 100);
                complianceMetrics.put("policyCompliance", complianceRate);
                
                // Identity reliability
                long identitySuccessCount = weekHistory.stream()
                    .mapToLong(h -> h.isIdentityPassed() ? 1L : 0L)
                    .sum();
                int identityReliability = (int) Math.round((double) identitySuccessCount / weekHistory.size() * 100);
                reliabilityMetrics.put("identityReliability", identityReliability);
                
                // Behavioral stability (inverse of anomaly detection)
                long normalBehaviorCount = weekHistory.stream()
                    .mapToLong(h -> h.isAnomalyDetected() ? 0L : 1L)
                    .sum();
                int behavioralStability = (int) Math.round((double) normalBehaviorCount / weekHistory.size() * 100);
                reliabilityMetrics.put("behavioralStability", behavioralStability);
                
                // Context stability
                long contextStableCount = weekHistory.stream()
                    .mapToLong(h -> h.isContextPassed() ? 1L : 0L)
                    .sum();
                int contextStability = (int) Math.round((double) contextStableCount / weekHistory.size() * 100);
                reliabilityMetrics.put("contextStability", contextStability);
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