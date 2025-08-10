package edu.university.iot.service;

import edu.university.iot.entity.DeviceRegistry;
import edu.university.iot.model.dtoModel.TrustAnalysisDto;
import edu.university.iot.model.dtoModel.TrustFactorDto;
import edu.university.iot.model.dtoModel.TrustHistoryDto;
import edu.university.iot.repository.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Enhanced service for detailed trust score analysis and drill-down capabilities
 */
@Service
public class TrustAnalysisService {

    private final DeviceRegistryRepository registryRepo;
    private final IdentityLogRepository identityLogRepo;
    private final LocationNetworkChangeRepository locationChangeRepo;
    private final FirmwareLogRepository firmwareLogRepo;
    private final AnomalyLogRepository anomalyLogRepo;
    private final ComplianceLogRepository complianceLogRepo;

    public TrustAnalysisService(
            DeviceRegistryRepository registryRepo,
            IdentityLogRepository identityLogRepo,
            LocationNetworkChangeRepository locationChangeRepo,
            FirmwareLogRepository firmwareLogRepo,
            AnomalyLogRepository anomalyLogRepo,
            ComplianceLogRepository complianceLogRepo) {
        
        this.registryRepo = registryRepo;
        this.identityLogRepo = identityLogRepo;
        this.locationChangeRepo = locationChangeRepo;
        this.firmwareLogRepo = firmwareLogRepo;
        this.anomalyLogRepo = anomalyLogRepo;
        this.complianceLogRepo = complianceLogRepo;
    }

    /**
     * Get detailed trust score analysis with breakdown of contributing factors
     */
    public TrustAnalysisDto getTrustAnalysis(String deviceId) {
        DeviceRegistry device = registryRepo.findById(deviceId)
            .orElseThrow(() -> new IllegalArgumentException("Device not found: " + deviceId));

        TrustAnalysisDto analysis = new TrustAnalysisDto();
        analysis.setDeviceId(deviceId);
        analysis.setCurrentTrustScore(device.getTrustScore() != null ? device.getTrustScore() : 0.0);
        analysis.setTrusted(device.isTrusted());
        analysis.setQuarantined(device.isQuarantined());
        
        // Analyze each trust factor
        List<TrustFactorDto> factors = analyzeTrustFactors(deviceId);
        analysis.setTrustFactors(factors);
        
        // Calculate overall health metrics
        analysis.setOverallHealth(calculateOverallHealth(factors));
        analysis.setRiskLevel(determineRiskLevel(device.getTrustScore(), factors));
        analysis.setRecommendations(generateRecommendations(factors));
        
        return analysis;
    }

    /**
     * Analyze individual trust factors contributing to the score
     */
    private List<TrustFactorDto> analyzeTrustFactors(String deviceId) {
        List<TrustFactorDto> factors = new ArrayList<>();
        
        // Identity Factor
        factors.add(analyzeIdentityFactor(deviceId));
        
        // Location/Context Factor
        factors.add(analyzeLocationFactor(deviceId));
        
        // Firmware Factor
        factors.add(analyzeFirmwareFactor(deviceId));
        
        // Anomaly Factor
        factors.add(analyzeAnomalyFactor(deviceId));
        
        // Compliance Factor
        factors.add(analyzeComplianceFactor(deviceId));
        
        return factors;
    }

    private TrustFactorDto analyzeIdentityFactor(String deviceId) {
        TrustFactorDto factor = new TrustFactorDto();
        factor.setFactorName("Identity Verification");
        factor.setCategory("SECURITY");
        
        try {
            // Get recent identity logs (last 24 hours)
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            var recentLogs = identityLogRepo.findByDeviceId(deviceId).stream()
                .filter(log -> log.getTimestamp().isAfter(yesterday.atZone(ZoneId.systemDefault()).toInstant()))
                .collect(Collectors.toList());
            
            if (recentLogs.isEmpty()) {
                factor.setScore(50.0);
                factor.setStatus("UNKNOWN");
                factor.setDescription("No recent identity verification data");
                factor.setImpact("MEDIUM");
                return factor;
            }
            
            long totalChecks = recentLogs.size();
            // Fixed: Use isIdentityVerified() instead of isVerified()
            long successfulChecks = recentLogs.stream()
                .mapToLong(log -> log.isIdentityVerified() ? 1L : 0L)
                .sum();
            
            double successRate = (double) successfulChecks / totalChecks;
            factor.setScore(successRate * 100);
            
            if (successRate >= 0.95) {
                factor.setStatus("EXCELLENT");
                factor.setImpact("POSITIVE");
                factor.setDescription("Identity consistently verified");
            } else if (successRate >= 0.8) {
                factor.setStatus("GOOD");
                factor.setImpact("NEUTRAL");
                factor.setDescription("Most identity checks pass");
            } else if (successRate >= 0.6) {
                factor.setStatus("CONCERNING");
                factor.setImpact("NEGATIVE");
                factor.setDescription("Frequent identity verification failures");
            } else {
                factor.setStatus("CRITICAL");
                factor.setImpact("CRITICAL");
                factor.setDescription("Identity verification consistently failing");
            }
            
            factor.setLastUpdated(recentLogs.get(0).getTimestamp());
            factor.setDataPoints((int) totalChecks);
            
        } catch (Exception e) {
            factor.setScore(0.0);
            factor.setStatus("ERROR");
            factor.setDescription("Error analyzing identity factor");
            factor.setImpact("CRITICAL");
        }
        
        return factor;
    }

    private TrustFactorDto analyzeLocationFactor(String deviceId) {
        TrustFactorDto factor = new TrustFactorDto();
        factor.setFactorName("Location Context");
        factor.setCategory("BEHAVIORAL");
        
        try {
            // Get recent location changes (last 24 hours)
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            var recentChanges = locationChangeRepo.findByDeviceIdOrderByTimestampDesc(deviceId).stream()
                .filter(change -> change.getTimestamp().isAfter(yesterday))
                .collect(Collectors.toList());
            
            int changeCount = recentChanges.size();
            
            if (changeCount == 0) {
                factor.setScore(90.0);
                factor.setStatus("STABLE");
                factor.setImpact("POSITIVE");
                factor.setDescription("No suspicious location changes");
            } else if (changeCount <= 2) {
                factor.setScore(75.0);
                factor.setStatus("NORMAL");
                factor.setImpact("NEUTRAL");
                factor.setDescription("Normal location mobility");
            } else if (changeCount <= 5) {
                factor.setScore(40.0);
                factor.setStatus("SUSPICIOUS");
                factor.setImpact("NEGATIVE");
                factor.setDescription("Frequent location changes detected");
            } else {
                factor.setScore(10.0);
                factor.setStatus("CRITICAL");
                factor.setImpact("CRITICAL");
                factor.setDescription("Abnormally high location mobility");
            }
            
            factor.setDataPoints(changeCount);
            if (!recentChanges.isEmpty()) {
                factor.setLastUpdated(recentChanges.get(0).getTimestamp()
                    .atZone(ZoneId.systemDefault()).toInstant());
            }
            
            // Add specific location patterns
            Map<String, Object> details = new HashMap<>();
            details.put("recentLocationChanges", changeCount);
            details.put("offCampusActivity", recentChanges.stream()
                .anyMatch(change -> change.getNewLocation().contains("Off-Campus")));
            factor.setDetails(details);
            
        } catch (Exception e) {
            factor.setScore(0.0);
            factor.setStatus("ERROR");
            factor.setDescription("Error analyzing location factor");
            factor.setImpact("CRITICAL");
        }
        
        return factor;
    }

    private TrustFactorDto analyzeFirmwareFactor(String deviceId) {
        TrustFactorDto factor = new TrustFactorDto();
        factor.setFactorName("Firmware Compliance");
        factor.setCategory("COMPLIANCE");
        
        try {
            // Get the most recent firmware log
            var recentLog = firmwareLogRepo.findTopByDeviceIdOrderByTimestampDesc(deviceId);
            
            if (recentLog.isEmpty()) {
                factor.setScore(50.0);
                factor.setStatus("UNKNOWN");
                factor.setDescription("No firmware validation data");
                factor.setImpact("MEDIUM");
                return factor;
            }
            
            var log = recentLog.get();
            boolean isValid = log.isFirmwareValid();
            
            if (isValid) {
                factor.setScore(95.0);
                factor.setStatus("COMPLIANT");
                factor.setImpact("POSITIVE");
                factor.setDescription("Firmware version is compliant");
            } else {
                factor.setScore(20.0);
                factor.setStatus("NON_COMPLIANT");
                factor.setImpact("NEGATIVE");
                factor.setDescription("Firmware version does not meet requirements");
            }
            
            factor.setLastUpdated(log.getTimestamp().atZone(ZoneId.systemDefault()).toInstant());
            factor.setDataPoints(1);
            
            Map<String, Object> details = new HashMap<>();
            details.put("currentVersion", log.getFirmwareVersion());
            details.put("patchStatus", log.getReportedPatchStatus());
            factor.setDetails(details);
            
        } catch (Exception e) {
            factor.setScore(0.0);
            factor.setStatus("ERROR");
            factor.setDescription("Error analyzing firmware factor");
            factor.setImpact("CRITICAL");
        }
        
        return factor;
    }

    private TrustFactorDto analyzeAnomalyFactor(String deviceId) {
        TrustFactorDto factor = new TrustFactorDto();
        factor.setFactorName("Anomaly Detection");
        factor.setCategory("SECURITY");
        
        try {
            // Get recent anomaly logs (last 24 hours)
            var recentLogs = anomalyLogRepo.findByDeviceId(deviceId).stream()
                .filter(log -> log.getTimestamp().isAfter(
                    Instant.now().minusSeconds(24 * 3600)))
                .collect(Collectors.toList());
            
            if (recentLogs.isEmpty()) {
                factor.setScore(85.0);
                factor.setStatus("NORMAL");
                factor.setDescription("No recent anomaly data");
                factor.setImpact("NEUTRAL");
                return factor;
            }
            
            long totalLogs = recentLogs.size();
            long anomaliesDetected = recentLogs.stream()
                .mapToLong(log -> log.isAnomalyDetected() ? 1L : 0L)
                .sum();
            
            double anomalyRate = (double) anomaliesDetected / totalLogs;
            
            if (anomalyRate == 0.0) {
                factor.setScore(95.0);
                factor.setStatus("CLEAN");
                factor.setImpact("POSITIVE");
                factor.setDescription("No anomalies detected");
            } else if (anomalyRate <= 0.1) {
                factor.setScore(75.0);
                factor.setStatus("LOW_RISK");
                factor.setImpact("NEUTRAL");
                factor.setDescription("Minimal anomalous behavior");
            } else if (anomalyRate <= 0.3) {
                factor.setScore(40.0);
                factor.setStatus("MODERATE_RISK");
                factor.setImpact("NEGATIVE");
                factor.setDescription("Concerning anomaly patterns");
            } else {
                factor.setScore(15.0);
                factor.setStatus("HIGH_RISK");
                factor.setImpact("CRITICAL");
                factor.setDescription("Frequent anomalous behavior detected");
            }
            
            factor.setDataPoints((int) totalLogs);
            factor.setLastUpdated(recentLogs.get(0).getTimestamp());
            
            Map<String, Object> details = new HashMap<>();
            details.put("anomalyCount", anomaliesDetected);
            details.put("anomalyRate", String.format("%.1f%%", anomalyRate * 100));
            factor.setDetails(details);
            
        } catch (Exception e) {
            factor.setScore(0.0);
            factor.setStatus("ERROR");
            factor.setDescription("Error analyzing anomaly factor");
            factor.setImpact("CRITICAL");
        }
        
        return factor;
    }

    private TrustFactorDto analyzeComplianceFactor(String deviceId) {
        TrustFactorDto factor = new TrustFactorDto();
        factor.setFactorName("Policy Compliance");
        factor.setCategory("COMPLIANCE");
        
        try {
            // Get recent compliance logs (last 24 hours)
            var recentLogs = complianceLogRepo.findByDeviceId(deviceId).stream()
                .filter(log -> log.getTimestamp().isAfter(
                    Instant.now().minusSeconds(24 * 3600)))
                .collect(Collectors.toList());
            
            if (recentLogs.isEmpty()) {
                factor.setScore(50.0);
                factor.setStatus("UNKNOWN");
                factor.setDescription("No recent compliance data");
                factor.setImpact("MEDIUM");
                return factor;
            }
            
            long totalChecks = recentLogs.size();
            long compliantChecks = recentLogs.stream()
                .mapToLong(log -> log.isCompliant() ? 1L : 0L)
                .sum();
            
            double complianceRate = (double) compliantChecks / totalChecks;
            
            if (complianceRate >= 0.95) {
                factor.setScore(95.0);
                factor.setStatus("FULLY_COMPLIANT");
                factor.setImpact("POSITIVE");
                factor.setDescription("Excellent policy compliance");
            } else if (complianceRate >= 0.8) {
                factor.setScore(80.0);
                factor.setStatus("MOSTLY_COMPLIANT");
                factor.setImpact("NEUTRAL");
                factor.setDescription("Good compliance with minor issues");
            } else if (complianceRate >= 0.6) {
                factor.setScore(50.0);
                factor.setStatus("PARTIALLY_COMPLIANT");
                factor.setImpact("NEGATIVE");
                factor.setDescription("Significant compliance violations");
            } else {
                factor.setScore(20.0);
                factor.setStatus("NON_COMPLIANT");
                factor.setImpact("CRITICAL");
                factor.setDescription("Major policy violations detected");
            }
            
            factor.setDataPoints((int) totalChecks);
            factor.setLastUpdated(recentLogs.get(0).getTimestamp());
            
            Map<String, Object> details = new HashMap<>();
            details.put("complianceRate", String.format("%.1f%%", complianceRate * 100));
            details.put("violationCount", totalChecks - compliantChecks);
            factor.setDetails(details);
            
        } catch (Exception e) {
            factor.setScore(0.0);
            factor.setStatus("ERROR");
            factor.setDescription("Error analyzing compliance factor");
            factor.setImpact("CRITICAL");
        }
        
        return factor;
    }

    /**
     * Calculate overall device health based on trust factors
     */
    private String calculateOverallHealth(List<TrustFactorDto> factors) {
        double avgScore = factors.stream()
            .mapToDouble(TrustFactorDto::getScore)
            .average()
            .orElse(0.0);
        
        long criticalIssues = factors.stream()
            .mapToLong(f -> "CRITICAL".equals(f.getImpact()) ? 1L : 0L)
            .sum();
        
        if (criticalIssues > 0) {
            return "CRITICAL";
        } else if (avgScore >= 80) {
            return "EXCELLENT";
        } else if (avgScore >= 60) {
            return "GOOD";
        } else if (avgScore >= 40) {
            return "CONCERNING";
        } else {
            return "POOR";
        }
    }

    /**
     * Determine risk level based on trust score and factors
     */
    private String determineRiskLevel(Double trustScore, List<TrustFactorDto> factors) {
        if (trustScore == null) trustScore = 0.0;
        
        long criticalFactors = factors.stream()
            .mapToLong(f -> "CRITICAL".equals(f.getImpact()) ? 1L : 0L)
            .sum();
        
        if (criticalFactors > 2 || trustScore < 30) {
            return "CRITICAL";
        } else if (criticalFactors > 0 || trustScore < 50) {
            return "HIGH";
        } else if (trustScore < 70) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * Generate actionable recommendations based on trust analysis
     */
    private List<String> generateRecommendations(List<TrustFactorDto> factors) {
        List<String> recommendations = new ArrayList<>();
        
        for (TrustFactorDto factor : factors) {
            switch (factor.getStatus()) {
                case "CRITICAL":
                case "NON_COMPLIANT":
                    if ("Identity Verification".equals(factor.getFactorName())) {
                        recommendations.add("Immediate certificate renewal required");
                        recommendations.add("Verify device physical security");
                    } else if ("Location Context".equals(factor.getFactorName())) {
                        recommendations.add("Investigate suspicious location patterns");
                        recommendations.add("Consider temporary access restrictions");
                    } else if ("Firmware Compliance".equals(factor.getFactorName())) {
                        recommendations.add("Force firmware update immediately");
                        recommendations.add("Check for unauthorized modifications");
                    } else if ("Anomaly Detection".equals(factor.getFactorName())) {
                        recommendations.add("Conduct full security scan");
                        recommendations.add("Monitor network traffic closely");
                    } else if ("Policy Compliance".equals(factor.getFactorName())) {
                        recommendations.add("Review and enforce security policies");
                        recommendations.add("Provide compliance training");
                    }
                    break;
                case "SUSPICIOUS":
                case "CONCERNING":
                case "MODERATE_RISK":
                    recommendations.add("Increase monitoring frequency for " + factor.getFactorName().toLowerCase());
                    recommendations.add("Schedule security assessment");
                    break;
            }
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("Device appears healthy - continue regular monitoring");
        }
        
        return recommendations;
    }

    /**
     * Get trust score history for trend analysis
     */
    public List<TrustHistoryDto> getTrustHistory(String deviceId, int days) {
        // This would require a new TrustHistory table to track score changes over time
        // For now, return a mock implementation
        List<TrustHistoryDto> history = new ArrayList<>();
        
        DeviceRegistry device = registryRepo.findById(deviceId).orElse(null);
        if (device != null) {
            // Create sample history points (in real implementation, fetch from database)
            LocalDateTime now = LocalDateTime.now();
            for (int i = days; i >= 0; i--) {
                TrustHistoryDto point = new TrustHistoryDto();
                point.setTimestamp(now.minusDays(i).atZone(ZoneId.systemDefault()).toInstant());
                point.setTrustScore(device.getTrustScore() != null ? device.getTrustScore() : 50.0);
                point.setEventType("ROUTINE_CHECK");
                history.add(point);
            }
        }
        
        return history;
    }

    /**
     * Get trust comparison across all devices
     */
    public Map<String, Double> getTrustComparison() {
        return registryRepo.findAll().stream()
            .collect(Collectors.toMap(
                DeviceRegistry::getDeviceId,
                device -> device.getTrustScore() != null ? device.getTrustScore() : 0.0
            ));
    }
}