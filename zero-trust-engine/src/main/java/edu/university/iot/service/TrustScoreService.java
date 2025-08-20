// src/main/java/edu/university/iot/service/TrustScoreService.java
package edu.university.iot.service;

import edu.university.iot.entity.DeviceRegistry;
import edu.university.iot.repository.DeviceRegistryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TrustScoreService {

    private static final Logger logger = LoggerFactory.getLogger(TrustScoreService.class);

    @Autowired
    private DeviceRegistryRepository registryRepo;

    @Autowired
    private TrustScoreHistoryService historyService;

    // Thresholds and weights
    private static final double TRUSTED_THRESHOLD = 70.0;
    private static final double IDENTITY_PENALTY = 5.0;
    private static final double IDENTITY_REWARD = 1.0;
    private static final double CONTEXT_PENALTY = 2.0;
    private static final double CONTEXT_REWARD = 0.5;
    private static final double FIRMWARE_PENALTY = 5.0;
    private static final double FIRMWARE_REWARD = 1.0;
    private static final double ANOMALY_PENALTY = 10.0;
    private static final double ANOMALY_REWARD = 2.0;
    private static final double COMPLIANCE_PENALTY = 10.0;
    private static final double COMPLIANCE_REWARD = 2.0;

    /**
     * Enhanced adjustTrust method with detailed history tracking and telemetry context
     */
    public void adjustTrust(String deviceId,
                            boolean identityPass,
                            boolean contextPass,
                            boolean firmwareValid,
                            boolean anomalyDetected,
                            boolean compliancePassed) {
        // Get current telemetry from thread-local storage or pass as parameter
        Map<String, Object> telemetryContext = getCurrentTelemetryContext();
        
        adjustTrustWithContext(deviceId, identityPass, contextPass, firmwareValid, 
                              anomalyDetected, compliancePassed, telemetryContext);
    }

    /**
     * Enhanced trust adjustment with telemetry context for detailed tracking
     */
    public void adjustTrustWithContext(String deviceId,
                                     boolean identityPass,
                                     boolean contextPass,
                                     boolean firmwareValid,
                                     boolean anomalyDetected,
                                     boolean compliancePassed,
                                     Map<String, Object> telemetryContext) {
        
        DeviceRegistry device = registryRepo.findById(deviceId).orElse(null);
        if (device == null) {
            logger.warn("Device [{}] not found for trust adjustment", deviceId);
            return;
        }

        double oldScore = device.getTrustScore() != null ? device.getTrustScore() : 50.0;
        double score = oldScore;

        // Store factor results for history tracking
        Map<String, Boolean> factorResults = new HashMap<>();
        factorResults.put("identity", identityPass);
        factorResults.put("context", contextPass);
        factorResults.put("firmware", firmwareValid);
        factorResults.put("anomaly", anomalyDetected);
        factorResults.put("compliance", compliancePassed);

        // Calculate adjustments with detailed logging
        double identityAdjustment = identityPass ? IDENTITY_REWARD : -IDENTITY_PENALTY;
        double contextAdjustment = contextPass ? CONTEXT_REWARD : -CONTEXT_PENALTY;
        double firmwareAdjustment = firmwareValid ? FIRMWARE_REWARD : -FIRMWARE_PENALTY;
        double anomalyAdjustment = anomalyDetected ? -ANOMALY_PENALTY : ANOMALY_REWARD;
        double complianceAdjustment = compliancePassed ? COMPLIANCE_REWARD : -COMPLIANCE_PENALTY;

        // Apply adjustments
        score += identityAdjustment + contextAdjustment + firmwareAdjustment + 
                anomalyAdjustment + complianceAdjustment;

        // Clamp 0-100
        score = Math.max(0, Math.min(score, 100));

        // Update device registry
        device.setTrustScore(score);
        boolean wasTrusted = device.isTrusted();
        boolean isTrusted = score >= TRUSTED_THRESHOLD;
        device.setTrusted(isTrusted);

        registryRepo.save(device);

        // Record detailed history if significant change
        if (Math.abs(score - oldScore) >= 0.5) {
            historyService.recordTrustScoreChange(deviceId, oldScore, score, 
                                                factorResults, telemetryContext);
        }

        // Enhanced logging
        logger.info("Device [{}] trust adjustment: {:.1f} -> {:.1f} " +
                   "(identity: {:.1f}, context: {:.1f}, firmware: {:.1f}, anomaly: {:.1f}, compliance: {:.1f})",
                   deviceId, oldScore, score, identityAdjustment, contextAdjustment, 
                   firmwareAdjustment, anomalyAdjustment, complianceAdjustment);

        // Log trust status changes
        if (wasTrusted && !isTrusted) {
            logger.warn("⚠️ Device [{}] lost trusted status: {:.1f} -> {:.1f}", 
                       deviceId, oldScore, score);
        } else if (!wasTrusted && isTrusted) {
            logger.info("✅ Device [{}] regained trusted status: {:.1f} -> {:.1f}", 
                       deviceId, oldScore, score);
        }
    }

    /**
     * Get current trust score for a device
     */
    public double getTrustScore(String deviceId) {
        DeviceRegistry device = registryRepo.findById(deviceId).orElse(null);
        return device != null && device.getTrustScore() != null 
             ? device.getTrustScore() 
             : 0.0;
    }

    /**
     * Check if device is currently trusted
     */
    public boolean isTrusted(String deviceId) {
        DeviceRegistry device = registryRepo.findById(deviceId).orElse(null);
        return device != null && device.isTrusted();
    }

    /**
     * Get trust score with breakdown of contributing factors
     */
    public Map<String, Object> getTrustScoreBreakdown(String deviceId) {
        Map<String, Object> breakdown = new HashMap<>();
        
        DeviceRegistry device = registryRepo.findById(deviceId).orElse(null);
        if (device == null) {
            breakdown.put("error", "Device not found");
            return breakdown;
        }

        breakdown.put("deviceId", deviceId);
        breakdown.put("currentScore", device.getTrustScore());
        breakdown.put("isTrusted", device.isTrusted());
        breakdown.put("threshold", TRUSTED_THRESHOLD);
        
        // Add factor weights for transparency
        Map<String, Object> factorWeights = new HashMap<>();
        factorWeights.put("identityReward", IDENTITY_REWARD);
        factorWeights.put("identityPenalty", IDENTITY_PENALTY);
        factorWeights.put("contextReward", CONTEXT_REWARD);
        factorWeights.put("contextPenalty", CONTEXT_PENALTY);
        factorWeights.put("firmwareReward", FIRMWARE_REWARD);
        factorWeights.put("firmwarePenalty", FIRMWARE_PENALTY);
        factorWeights.put("anomalyReward", ANOMALY_REWARD);
        factorWeights.put("anomalyPenalty", ANOMALY_PENALTY);
        factorWeights.put("complianceReward", COMPLIANCE_REWARD);
        factorWeights.put("compliancePenalty", COMPLIANCE_PENALTY);
        
        breakdown.put("factorWeights", factorWeights);
        breakdown.put("lastUpdated", System.currentTimeMillis());
        
        return breakdown;
    }

    /**
     * Simulate trust score change to predict impact
     */
    public double simulateTrustScoreChange(String deviceId,
                                         boolean identityPass,
                                         boolean contextPass,
                                         boolean firmwareValid,
                                         boolean anomalyDetected,
                                         boolean compliancePassed) {
        double currentScore = getTrustScore(deviceId);
        
        double identityAdjustment = identityPass ? IDENTITY_REWARD : -IDENTITY_PENALTY;
        double contextAdjustment = contextPass ? CONTEXT_REWARD : -CONTEXT_PENALTY;
        double firmwareAdjustment = firmwareValid ? FIRMWARE_REWARD : -FIRMWARE_PENALTY;
        double anomalyAdjustment = anomalyDetected ? -ANOMALY_PENALTY : ANOMALY_REWARD;
        double complianceAdjustment = compliancePassed ? COMPLIANCE_REWARD : -COMPLIANCE_PENALTY;

        double simulatedScore = currentScore + identityAdjustment + contextAdjustment + 
                               firmwareAdjustment + anomalyAdjustment + complianceAdjustment;

        return Math.max(0, Math.min(simulatedScore, 100));
    }

    /**
     * Reset trust score to baseline (for testing or emergency reset)
     */
    public void resetTrustScore(String deviceId, double baselineScore) {
        DeviceRegistry device = registryRepo.findById(deviceId).orElse(null);
        if (device == null) {
            logger.warn("Cannot reset trust score - device [{}] not found", deviceId);
            return;
        }

        double oldScore = device.getTrustScore() != null ? device.getTrustScore() : 0.0;
        
        device.setTrustScore(baselineScore);
        device.setTrusted(baselineScore >= TRUSTED_THRESHOLD);
        registryRepo.save(device);

        // Record the manual reset in history
        Map<String, Boolean> factorResults = new HashMap<>();
        factorResults.put("identity", true);
        factorResults.put("context", true);
        factorResults.put("firmware", true);
        factorResults.put("anomaly", false);
        factorResults.put("compliance", true);

        Map<String, Object> resetContext = new HashMap<>();
        resetContext.put("resetType", "MANUAL_BASELINE_RESET");
        resetContext.put("administrator", "SYSTEM");

        historyService.recordTrustScoreChange(deviceId, oldScore, baselineScore, 
                                            factorResults, resetContext);

        logger.info("Trust score reset for device [{}]: {:.1f} -> {:.1f}", 
                   deviceId, oldScore, baselineScore);
    }

    /**
     * Get trust score statistics across all devices
     */
    public Map<String, Object> getSystemTrustStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            var allDevices = registryRepo.findAll();
            
            double totalTrust = allDevices.stream()
                .mapToDouble(d -> d.getTrustScore() != null ? d.getTrustScore() : 0.0)
                .sum();
            
            long trustedDevices = allDevices.stream()
                .mapToLong(d -> d.isTrusted() ? 1L : 0L)
                .sum();

            stats.put("totalDevices", allDevices.size());
            stats.put("trustedDevices", trustedDevices);
            stats.put("untrustedDevices", allDevices.size() - trustedDevices);
            stats.put("averageTrustScore", allDevices.isEmpty() ? 0.0 : totalTrust / allDevices.size());
            stats.put("trustThreshold", TRUSTED_THRESHOLD);
            
        } catch (Exception e) {
            logger.error("Error calculating system trust statistics: {}", e.getMessage());
            stats.put("error", "Unable to calculate statistics");
        }
        
        return stats;
    }

    // Thread-local storage for telemetry context (to be set by TelemetryProcessorService)
    private static final ThreadLocal<Map<String, Object>> telemetryContext = new ThreadLocal<>();

    public static void setCurrentTelemetryContext(Map<String, Object> context) {
        telemetryContext.set(context);
    }

    public static Map<String, Object> getCurrentTelemetryContext() {
        return telemetryContext.get();
    }

    public static void clearCurrentTelemetryContext() {
        telemetryContext.remove();
    }
}