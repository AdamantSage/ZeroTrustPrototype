package edu.university.iot.service;

import edu.university.iot.entity.DeviceRegistry;
import edu.university.iot.model.dtoModel.*;
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
 * Comprehensive device risk assessment service that provides detailed insights
 * for proactive security management and frontend dashboard integration.
 */
@Service
public class DeviceRiskAssessmentService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceRiskAssessmentService.class);

    private final DeviceRegistryRepository registryRepo;
    private final TrustScoreHistoryService trustHistoryService;
    private final LocationService locationService;
    private final AnomalyLogRepository anomalyRepo;
    private final ComplianceLogRepository complianceRepo;
    private final FirmwareLogRepository firmwareRepo;
    private final IdentityLogRepository identityRepo;

    public DeviceRiskAssessmentService(
            DeviceRegistryRepository registryRepo,
            TrustScoreHistoryService trustHistoryService,
            LocationService locationService,
            AnomalyLogRepository anomalyRepo,
            ComplianceLogRepository complianceRepo,
            FirmwareLogRepository firmwareRepo,
            IdentityLogRepository identityRepo) {
        
        this.registryRepo = registryRepo;
        this.trustHistoryService = trustHistoryService;
        this.locationService = locationService;
        this.anomalyRepo = anomalyRepo;
        this.complianceRepo = complianceRepo;
        this.firmwareRepo = firmwareRepo;
        this.identityRepo = identityRepo;
    }

    /**
     * Get comprehensive risk assessment for a specific device
     */
    public DeviceRiskAssessmentDto getDeviceRiskAssessment(String deviceId) {
        DeviceRiskAssessmentDto assessment = new DeviceRiskAssessmentDto();
        assessment.setDeviceId(deviceId);
        assessment.setLastAssessment(Instant.now());

        try {
            DeviceRegistry device = registryRepo.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found: " + deviceId));

            // Basic trust information
            double currentTrustScore = device.getTrustScore() != null ? device.getTrustScore() : 50.0;
            assessment.setCurrentTrustScore(currentTrustScore);
            
            // Risk level assessment
            assessment.setRiskLevel(determineRiskLevel(currentTrustScore, device));
            
            // Trust trend analysis (24 hours)
            assessment.setRiskTrend(analyzeTrustTrend(deviceId));
            
            // Detailed risk factors
            assessment.setRiskFactors(analyzeRiskFactors(deviceId));
            
            // Active threats identification
            assessment.setActiveThreats(identifyActiveThreats(deviceId));
            
            // Recent activity indicators
            assessRecentActivity(deviceId, assessment);
            
            // Predictive analysis
            performPredictiveAnalysis(deviceId, assessment, currentTrustScore);
            
            // Generate actionable recommendations
            assessment.setRecommendations(generateRecommendations(deviceId, assessment));

        } catch (Exception e) {
            logger.error("Error assessing risk for device [{}]: {}", deviceId, e.getMessage(), e);
            assessment.setRiskLevel("ERROR");
            assessment.setActiveThreats(List.of("Risk assessment failed: " + e.getMessage()));
        }

        return assessment;
    }

    /**
     * Get risk assessment for all devices with summary statistics
     */
    public Map<String, Object> getSystemRiskOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        try {
            List<DeviceRegistry> allDevices = registryRepo.findAll();
            
            // Basic statistics
            overview.put("totalDevices", allDevices.size());
            
            Map<String, Long> riskDistribution = allDevices.stream()
                .collect(Collectors.groupingBy(
                    device -> determineRiskLevel(
                        device.getTrustScore() != null ? device.getTrustScore() : 50.0, device),
                    Collectors.counting()
                ));
            
            overview.put("riskDistribution", riskDistribution);
            
            // High-risk devices requiring immediate attention
            List<String> highRiskDevices = allDevices.stream()
                .filter(device -> {
                    String risk = determineRiskLevel(
                        device.getTrustScore() != null ? device.getTrustScore() : 50.0, device);
                    return "CRITICAL".equals(risk) || "HIGH".equals(risk);
                })
                .map(DeviceRegistry::getDeviceId)
                .collect(Collectors.toList());
            
            overview.put("highRiskDevices", highRiskDevices);
            
            // Recent concerning changes
            overview.put("devicesWithRecentIssues", 
                trustHistoryService.getDevicesWithConcerningChanges(24));
            
            // System health score
            double systemHealthScore = calculateSystemHealthScore(allDevices);
            overview.put("systemHealthScore", Math.round(systemHealthScore * 100.0) / 100.0);
            
            overview.put("lastUpdated", Instant.now());
            
        } catch (Exception e) {
            logger.error("Error generating system risk overview: {}", e.getMessage(), e);
            overview.put("error", "Unable to generate risk overview");
        }
        
        return overview;
    }

    /**
     * Get devices that need immediate attention
     */
    public List<Map<String, Object>> getDevicesRequiringAttention() {
        List<Map<String, Object>> attentionList = new ArrayList<>();
        
        try {
            List<DeviceRegistry> allDevices = registryRepo.findAll();
            
            for (DeviceRegistry device : allDevices) {
                Map<String, Object> deviceInfo = new HashMap<>();
                String deviceId = device.getDeviceId();
                double trustScore = device.getTrustScore() != null ? device.getTrustScore() : 50.0;
                String riskLevel = determineRiskLevel(trustScore, device);
                
                // Only include devices needing attention
                if (!"LOW".equals(riskLevel)) {
                    deviceInfo.put("deviceId", deviceId);
                    deviceInfo.put("trustScore", trustScore);
                    deviceInfo.put("riskLevel", riskLevel);
                    deviceInfo.put("isQuarantined", device.isQuarantined());
                    deviceInfo.put("isTrusted", device.isTrusted());
                    
                    // Get primary issues
                    List<String> issues = identifyPrimaryIssues(deviceId);
                    deviceInfo.put("primaryIssues", issues);
                    
                    // Get urgency level
                    deviceInfo.put("urgency", determineUrgency(riskLevel, issues.size()));
                    
                    attentionList.add(deviceInfo);
                }
            }
            
            // Sort by urgency and risk level
            attentionList.sort((a, b) -> {
                String urgencyA = (String) a.get("urgency");
                String urgencyB = (String) b.get("urgency");
                return compareUrgency(urgencyB, urgencyA); // Reverse order for descending
            });
            
        } catch (Exception e) {
            logger.error("Error getting devices requiring attention: {}", e.getMessage(), e);
        }
        
        return attentionList;
    }

    // === PRIVATE HELPER METHODS ===

    private String determineRiskLevel(double trustScore, DeviceRegistry device) {
        if (device.isQuarantined()) return "CRITICAL";
        
        if (trustScore < 30) return "CRITICAL";
        if (trustScore < 50) return "HIGH";
        if (trustScore < 70) return "MEDIUM";
        return "LOW";
    }

    private String analyzeTrustTrend(String deviceId) {
        try {
            TrustChangeAnalysisDto analysis = trustHistoryService.analyzeTrustChanges(deviceId, 24);
            return analysis.getTrend();
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    private Map<String, String> analyzeRiskFactors(String deviceId) {
        Map<String, String> factors = new HashMap<>();
        
        try {
            Instant cutoff = Instant.now().minusSeconds(24 * 3600); // 24 hours ago
            
            // Identity factor
            var identityLogs = identityRepo.findByDeviceId(deviceId).stream()
                .filter(log -> log.getTimestamp().isAfter(cutoff))
                .collect(Collectors.toList());
            
            if (!identityLogs.isEmpty()) {
                long failures = identityLogs.stream()
                    .mapToLong(log -> log.isIdentityVerified() ? 0L : 1L)
                    .sum();
                double failureRate = (double) failures / identityLogs.size();
                
                if (failureRate > 0.3) factors.put("identity", "HIGH_RISK");
                else if (failureRate > 0.1) factors.put("identity", "MEDIUM_RISK");
                else factors.put("identity", "LOW_RISK");
            } else {
                factors.put("identity", "NO_DATA");
            }
            
            // Location factor
            var locationChanges = locationService.getLocationHistory(deviceId, 24);
            if (locationChanges.size() > 5) {
                factors.put("location", "HIGH_RISK");
            } else if (locationChanges.size() > 2) {
                factors.put("location", "MEDIUM_RISK");
            } else {
                factors.put("location", "LOW_RISK");
            }
            
            // Anomaly factor
            var anomalyLogs = anomalyRepo.findByDeviceId(deviceId).stream()
                .filter(log -> log.getTimestamp().isAfter(cutoff))
                .collect(Collectors.toList());
            
            if (!anomalyLogs.isEmpty()) {
                long anomalies = anomalyLogs.stream()
                    .mapToLong(log -> log.isAnomalyDetected() ? 1L : 0L)
                    .sum();
                double anomalyRate = (double) anomalies / anomalyLogs.size();
                
                if (anomalyRate > 0.3) factors.put("behavior", "HIGH_RISK");
                else if (anomalyRate > 0.1) factors.put("behavior", "MEDIUM_RISK");
                else factors.put("behavior", "LOW_RISK");
            } else {
                factors.put("behavior", "NO_DATA");
            }
            
            // Compliance factor
            var complianceLogs = complianceRepo.findByDeviceId(deviceId).stream()
                .filter(log -> log.getTimestamp().isAfter(cutoff))
                .collect(Collectors.toList());
            
            if (!complianceLogs.isEmpty()) {
                long violations = complianceLogs.stream()
                    .mapToLong(log -> log.isCompliant() ? 0L : 1L)
                    .sum();
                double violationRate = (double) violations / complianceLogs.size();
                
                if (violationRate > 0.3) factors.put("compliance", "HIGH_RISK");
                else if (violationRate > 0.1) factors.put("compliance", "MEDIUM_RISK");
                else factors.put("compliance", "LOW_RISK");
            } else {
                factors.put("compliance", "NO_DATA");
            }
            
            // Firmware factor
            var firmwareLog = firmwareRepo.findTopByDeviceIdOrderByTimestampDesc(deviceId);
            if (firmwareLog.isPresent()) {
                if (!firmwareLog.get().isFirmwareValid()) {
                    factors.put("firmware", "HIGH_RISK");
                } else {
                    factors.put("firmware", "LOW_RISK");
                }
            } else {
                factors.put("firmware", "NO_DATA");
            }
            
        } catch (Exception e) {
            logger.error("Error analyzing risk factors for device [{}]: {}", deviceId, e.getMessage());
        }
        
        return factors;
    }

    private List<String> identifyActiveThreats(String deviceId) {
        List<String> threats = new ArrayList<>();
        
        try {
            Instant cutoff = Instant.now().minusSeconds(3600); // 1 hour ago
            
            // Check for recent anomalies
            var recentAnomalies = anomalyRepo.findByDeviceId(deviceId).stream()
                .filter(log -> log.getTimestamp().isAfter(cutoff) && log.isAnomalyDetected())
                .collect(Collectors.toList());
            
            if (!recentAnomalies.isEmpty()) {
                threats.add("Recent anomalous behavior detected");
            }
            
            // Check for identity issues
            var recentIdentityFailures = identityRepo.findByDeviceId(deviceId).stream()
                .filter(log -> log.getTimestamp().isAfter(cutoff) && !log.isIdentityVerified())
                .collect(Collectors.toList());
            
            if (!recentIdentityFailures.isEmpty()) {
                threats.add("Identity verification failures");
            }
            
            // Check for rapid location changes
            var recentLocationChanges = locationService.getLocationHistory(deviceId, 1);
            if (recentLocationChanges.size() > 3) {
                threats.add("Excessive location mobility");
            }
            
            // Check for compliance violations
            var recentCompliance = complianceRepo.findByDeviceId(deviceId).stream()
                .filter(log -> log.getTimestamp().isAfter(cutoff) && !log.isCompliant())
                .collect(Collectors.toList());
            
            if (!recentCompliance.isEmpty()) {
                threats.add("Policy compliance violations");
            }
            
        } catch (Exception e) {
            logger.error("Error identifying active threats for device [{}]: {}", deviceId, e.getMessage());
            threats.add("Error assessing active threats");
        }
        
        return threats;
    }

    private void assessRecentActivity(String deviceId, DeviceRiskAssessmentDto assessment) {
        try {
            Instant cutoff = Instant.now().minusSeconds(24 * 3600); // 24 hours
            
            // Count recent anomalies
            long recentAnomalies = anomalyRepo.findByDeviceId(deviceId).stream()
                .filter(log -> log.getTimestamp().isAfter(cutoff) && log.isAnomalyDetected())
                .count();
            assessment.setRecentAnomalies((int) recentAnomalies);
            
            // Count location changes
            int locationChanges = locationService.getLocationHistory(deviceId, 24).size();
            assessment.setLocationChanges(locationChanges);
            
            // Check for compliance issues
            boolean complianceIssues = complianceRepo.findByDeviceId(deviceId).stream()
                .anyMatch(log -> log.getTimestamp().isAfter(cutoff) && !log.isCompliant());
            assessment.setComplianceIssues(complianceIssues);
            
            // Check for identity issues
            boolean identityIssues = identityRepo.findByDeviceId(deviceId).stream()
                .anyMatch(log -> log.getTimestamp().isAfter(cutoff) && !log.isIdentityVerified());
            assessment.setIdentityIssues(identityIssues);
            
        } catch (Exception e) {
            logger.error("Error assessing recent activity for device [{}]: {}", deviceId, e.getMessage());
        }
    }

    private void performPredictiveAnalysis(String deviceId, DeviceRiskAssessmentDto assessment, 
                                         double currentTrustScore) {
        try {
            // Simple predictive model based on recent trends
            TrustChangeAnalysisDto analysis = trustHistoryService.analyzeTrustChanges(deviceId, 24);
            
            double predictedChange = 0.0;
            double confidence = 0.5; // Default confidence
            
            if (analysis.getTotalChanges() > 0) {
                double avgChange = analysis.getNetScoreChange() / analysis.getTotalChanges();
                
                // Project 24 hours ahead
                predictedChange = avgChange * 2; // Assuming similar pattern continues
                confidence = Math.min(0.9, 0.3 + (analysis.getTotalChanges() * 0.1));
            }
            
            double predictedScore = Math.max(0, Math.min(100, currentTrustScore + predictedChange));
            
            assessment.setPredictedTrustScore24h(predictedScore);
            assessment.setConfidenceLevel(confidence);
            
            // Predicted risk level
            DeviceRegistry device = registryRepo.findById(deviceId).orElse(null);
            if (device != null) {
                device.setTrustScore(predictedScore); // Temporary for assessment
                assessment.setPredictedRisk(determineRiskLevel(predictedScore, device));
                device.setTrustScore(currentTrustScore); // Restore original
            }
            
        } catch (Exception e) {
            logger.error("Error performing predictive analysis for device [{}]: {}", deviceId, e.getMessage());
            assessment.setPredictedTrustScore24h(currentTrustScore);
            assessment.setConfidenceLevel(0.1);
        }
    }

    private List<String> generateRecommendations(String deviceId, DeviceRiskAssessmentDto assessment) {
        List<String> recommendations = new ArrayList<>();
        
        try {
            // Risk level based recommendations
            switch (assessment.getRiskLevel()) {
                case "CRITICAL":
                    recommendations.add("URGENT: Immediately quarantine device if not already done");
                    recommendations.add("Conduct thorough security investigation");
                    recommendations.add("Review all recent device activity logs");
                    break;
                    
                case "HIGH":
                    recommendations.add("Increase monitoring frequency to every minute");
                    recommendations.add("Restrict device access to essential services only");
                    recommendations.add("Schedule immediate security assessment");
                    break;
                    
                case "MEDIUM":
                    recommendations.add("Review security policies with device owner");
                    recommendations.add("Verify firmware and patch status");
                    recommendations.add("Monitor for 48 hours");
                    break;
                    
                default:
                    recommendations.add("Continue regular monitoring");
                    break;
            }
            
            // Factor-specific recommendations
            Map<String, String> riskFactors = assessment.getRiskFactors();
            if ("HIGH_RISK".equals(riskFactors.get("identity"))) {
                recommendations.add("Renew device certificates immediately");
            }
            if ("HIGH_RISK".equals(riskFactors.get("location"))) {
                recommendations.add("Verify legitimate reasons for frequent location changes");
            }
            if ("HIGH_RISK".equals(riskFactors.get("compliance"))) {
                recommendations.add("Enforce policy compliance through configuration management");
            }
            if ("HIGH_RISK".equals(riskFactors.get("firmware"))) {
                recommendations.add("Force firmware update to compliant version");
            }
            
            // Activity-based recommendations
            if (assessment.getRecentAnomalies() > 5) {
                recommendations.add("Investigate root cause of persistent anomalies");
            }
            if (assessment.getLocationChanges() > 10) {
                recommendations.add("Consider implementing geo-fencing restrictions");
            }
            
        } catch (Exception e) {
            logger.error("Error generating recommendations for device [{}]: {}", deviceId, e.getMessage());
            recommendations.add("Error generating recommendations - manual review required");
        }
        
        return recommendations;
    }

    private double calculateSystemHealthScore(List<DeviceRegistry> devices) {
        if (devices.isEmpty()) return 0.0;
        
        return devices.stream()
            .mapToDouble(device -> device.getTrustScore() != null ? device.getTrustScore() : 50.0)
            .average()
            .orElse(0.0);
    }

    private List<String> identifyPrimaryIssues(String deviceId) {
        List<String> issues = new ArrayList<>();
        
        try {
            Map<String, String> factors = analyzeRiskFactors(deviceId);
            factors.entrySet().stream()
                .filter(entry -> "HIGH_RISK".equals(entry.getValue()))
                .forEach(entry -> issues.add(formatIssue(entry.getKey())));
                
        } catch (Exception e) {
            issues.add("Assessment error");
        }
        
        return issues;
    }

    private String formatIssue(String factor) {
        switch (factor) {
            case "identity": return "Identity verification failures";
            case "location": return "Suspicious location patterns";
            case "behavior": return "Anomalous behavior";
            case "compliance": return "Policy violations";
            case "firmware": return "Firmware non-compliance";
            default: return factor + " issues";
        }
    }

    private String determineUrgency(String riskLevel, int issueCount) {
        if ("CRITICAL".equals(riskLevel)) return "URGENT";
        if ("HIGH".equals(riskLevel) || issueCount > 2) return "HIGH";
        if ("MEDIUM".equals(riskLevel) || issueCount > 0) return "MEDIUM";
        return "LOW";
    }

    private int compareUrgency(String urgency1, String urgency2) {
        Map<String, Integer> urgencyOrder = Map.of(
            "URGENT", 4,
            "HIGH", 3,
            "MEDIUM", 2,
            "LOW", 1
        );
        
        return Integer.compare(
            urgencyOrder.getOrDefault(urgency1, 0),
            urgencyOrder.getOrDefault(urgency2, 0)
        );
    }
}