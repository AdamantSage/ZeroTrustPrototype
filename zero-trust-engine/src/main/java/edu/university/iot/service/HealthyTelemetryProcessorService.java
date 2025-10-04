package edu.university.iot.service;

import edu.university.iot.entity.DeviceRegistry;
import edu.university.iot.repository.DeviceRegistryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Specialized service for processing healthy device telemetry
 * to accelerate trust score recovery and reward good behavior
 */
@Service
public class HealthyTelemetryProcessorService {

    private static final Logger logger = LoggerFactory.getLogger(HealthyTelemetryProcessorService.class);

    private final DeviceRegistryRepository registryRepo;
    private final TrustScoreService trustService;
    private final TrustScoreHistoryService trustHistoryService;
    private final LocationService locationService;
    private final SessionManagementService sessionService;
    private final IdentityVerificationService identityService;
    private final FirmwareService firmwareService;
    private final ComplianceService complianceService;
    private final AnomalyDetectorService anomalyService;

    // Enhanced rewards for healthy behavior
    private static final double HEALTHY_BEHAVIOR_BONUS = 3.0;
    private static final double CONSECUTIVE_HEALTHY_MULTIPLIER = 0.1;
    private static final int STREAK_THRESHOLD_FOR_BONUS = 5;

    public HealthyTelemetryProcessorService(
            DeviceRegistryRepository registryRepo,
            TrustScoreService trustService,
            TrustScoreHistoryService trustHistoryService,
            LocationService locationService,
            SessionManagementService sessionService,
            IdentityVerificationService identityService,
            FirmwareService firmwareService,
            ComplianceService complianceService,
            AnomalyDetectorService anomalyService) {

        this.registryRepo = registryRepo;
        this.trustService = trustService;
        this.trustHistoryService = trustHistoryService;
        this.locationService = locationService;
        this.sessionService = sessionService;
        this.identityService = identityService;
        this.firmwareService = firmwareService;
        this.complianceService = complianceService;
        this.anomalyService = anomalyService;
    }

    /**
     * Process healthy telemetry with enhanced trust score improvements
     * AND persist to all relevant log tables
     */
    @Transactional
    public void processHealthyTelemetry(Map<String, Object> telemetry) {
        String deviceId = (String) telemetry.get("deviceId");

        // Verify this is actually healthy telemetry
        if (!isHealthyTelemetry(telemetry)) {
            logger.warn("Telemetry for device [{}] does not meet healthy criteria", deviceId);
            return;
        }

        try {
            // Set telemetry context for detailed trust tracking
            TrustScoreService.setCurrentTelemetryContext(telemetry);

            // 1) Start or refresh session
            String sessionId = sessionService.startOrRefreshSession(deviceId);

            DeviceRegistry device = registryRepo.findById(deviceId)
                    .orElseThrow(() -> new IllegalStateException("Unknown device: " + deviceId));

            // Get pre-processing trust score
            double preTrustScore = device.getTrustScore() != null ? device.getTrustScore() : 50.0;

            // 2) Process all validation checks - THIS PERSISTS TO LOG TABLES
            boolean contextUnchanged = locationService.validateContext(telemetry);
            boolean identityOk = identityService.verifyIdentity(telemetry);
            boolean firmwareOk = firmwareService.validateAndLogFirmware(telemetry);
            boolean complianceOk = complianceService.evaluateCompliance(telemetry);
            boolean anomalyDetected = anomalyService.checkAnomaly(telemetry);

            // 3) Apply standard trust adjustments (all positive for healthy telemetry)
            trustService.adjustTrustWithContext(
                    deviceId,
                    identityOk,
                    contextUnchanged,
                    firmwareOk,
                    anomalyDetected,
                    complianceOk,
                    telemetry);

            // 4) Apply healthy behavior bonus
            applyHealthyBehaviorBonus(deviceId, telemetry);

            // 5) Reload device to get updated trust score
            device = registryRepo.findById(deviceId).get();
            double postTrustScore = device.getTrustScore() != null ? device.getTrustScore() : 0.0;

            // Log improvement
            if (postTrustScore > preTrustScore) {
                logHealthyBehaviorEvent(deviceId, "TRUST_IMPROVEMENT",
                        String.format("Trust score improved from %.1f to %.1f through healthy behavior",
                                preTrustScore, postTrustScore),
                        telemetry);
            }

            // 6) Remove quarantine if trust is restored
            if (device.isQuarantined() && postTrustScore >= 70.0) {
                device.setQuarantined(false);
                device.setQuarantineReason(null);
                device.setQuarantineTimestamp(null);
                registryRepo.save(device);

                logHealthyBehaviorEvent(deviceId, "QUARANTINE_LIFTED",
                        "Device restored to trusted status through consistent healthy behavior", telemetry);
            }

            // 7) Track healthy behavior metrics
            trackHealthyBehaviorMetrics(deviceId, telemetry);

        } catch (Exception e) {
            logger.error("Error processing healthy telemetry for device [{}]: {}",
                    deviceId, e.getMessage(), e);
        } finally {
            TrustScoreService.clearCurrentTelemetryContext();
        }
    }

    /**
     * Apply additional bonus for healthy behavior
     */
    private void applyHealthyBehaviorBonus(String deviceId, Map<String, Object> telemetry) {
        DeviceRegistry device = registryRepo.findById(deviceId).orElse(null);
        if (device == null)
            return;

        double currentScore = device.getTrustScore() != null ? device.getTrustScore() : 50.0;

        // Base healthy behavior bonus
        double bonus = HEALTHY_BEHAVIOR_BONUS;

        // Additional bonus for consecutive healthy reports
        Integer consecutiveHealthy = (Integer) telemetry.get("consecutiveHealthyReports");
        if (consecutiveHealthy != null && consecutiveHealthy >= STREAK_THRESHOLD_FOR_BONUS) {
            double streakBonus = Math.min(5.0, consecutiveHealthy * CONSECUTIVE_HEALTHY_MULTIPLIER);
            bonus += streakBonus;

            if (consecutiveHealthy % 10 == 0) {
                logger.info("Device [{}] healthy behavior streak: {} reports - bonus: {:.2f}",
                        deviceId, consecutiveHealthy, bonus);
            }
        }

        // Apply bonus (capped at 100)
        double newScore = Math.min(100.0, currentScore + bonus);
        device.setTrustScore(newScore);
        device.setTrusted(newScore >= 70.0);
        registryRepo.save(device);

        // Record in history
        Map<String, Boolean> factorResults = new HashMap<>();
        factorResults.put("identity", true);
        factorResults.put("context", true);
        factorResults.put("firmware", true);
        factorResults.put("anomaly", false);
        factorResults.put("compliance", true);

        trustHistoryService.recordTrustScoreChange(deviceId, currentScore, newScore,
                factorResults, telemetry);

        logger.debug("Applied healthy behavior bonus to device [{}]: {:.1f} -> {:.1f} (+{:.2f})",
                deviceId, currentScore, newScore, bonus);
    }

    /**
     * Verify telemetry meets healthy criteria
     */
    private boolean isHealthyTelemetry(Map<String, Object> telemetry) {
        // Check for healthy behavior indicator flag
        Boolean healthyIndicator = (Boolean) telemetry.get("healthyBehaviorIndicator");
        if (Boolean.TRUE.equals(healthyIndicator)) {
            return true;
        }

        // Fallback: verify all health criteria
        Boolean certValid = (Boolean) telemetry.get("certificateValid");
        Boolean malware = (Boolean) telemetry.get("malwareSignatureDetected");
        Double anomalyScore = (Double) telemetry.get("anomalyScore");
        String patchStatus = (String) telemetry.get("patchStatus");

        return Boolean.TRUE.equals(certValid) &&
                Boolean.FALSE.equals(malware) &&
                anomalyScore != null && anomalyScore < 0.1 &&
                "Up-to-date".equalsIgnoreCase(patchStatus);
    }

    /**
     * Log healthy behavior events
     */
    private void logHealthyBehaviorEvent(String deviceId, String eventType,
            String description, Map<String, Object> telemetry) {
        String location = (String) telemetry.get("location");
        Double trustScore = getTrustScoreFromRegistry(deviceId);

        logger.info("[HEALTHY BEHAVIOR] Device: {} | Type: {} | Location: {} | Trust: {:.1f} | Description: {}",
                deviceId, eventType, location, trustScore, description);
    }

    /**
     * Track healthy behavior metrics for analytics
     */
    private void trackHealthyBehaviorMetrics(String deviceId, Map<String, Object> telemetry) {
        Integer consecutiveHealthy = (Integer) telemetry.get("consecutiveHealthyReports");

        if (consecutiveHealthy != null && consecutiveHealthy % 50 == 0) {
            logger.info("Device [{}] milestone: {} consecutive healthy reports",
                    deviceId, consecutiveHealthy);
        }
    }

    /**
     * Get current trust score from registry
     */
    private double getTrustScoreFromRegistry(String deviceId) {
        return registryRepo.findById(deviceId)
                .map(d -> d.getTrustScore() != null ? d.getTrustScore() : 50.0)
                .orElse(50.0);
    }

    /**
     * Get healthy behavior statistics for a device
     */
    public Map<String, Object> getHealthyBehaviorStatistics(String deviceId) {
        Map<String, Object> stats = new HashMap<>();

        try {
            DeviceRegistry device = registryRepo.findById(deviceId).orElse(null);
            if (device == null) {
                stats.put("error", "Device not found");
                return stats;
            }

            stats.put("deviceId", deviceId);
            stats.put("currentTrustScore", device.getTrustScore());
            stats.put("isTrusted", device.isTrusted());
            stats.put("isQuarantined", device.isQuarantined());

            // Get improvement trend from history
            double improvement = calculateRecentImprovement(deviceId, 24);
            stats.put("trustImprovementLast24h", Math.round(improvement * 100.0) / 100.0);

            stats.put("lastUpdated", Instant.now());

        } catch (Exception e) {
            logger.error("Error getting healthy behavior statistics for device [{}]: {}",
                    deviceId, e.getMessage());
            stats.put("error", "Unable to calculate statistics");
        }

        return stats;
    }

    /**
     * Calculate trust score improvement over time period
     */
    private double calculateRecentImprovement(String deviceId, int hours) {
        try {
            var analysis = trustHistoryService.analyzeTrustChanges(deviceId, hours);
            return analysis.getNetScoreChange();
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Get system-wide healthy behavior metrics
     */
    public Map<String, Object> getSystemHealthyBehaviorMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        try {
            var allDevices = registryRepo.findAll();

            long healthyDevices = allDevices.stream()
                    .filter(d -> d.getTrustScore() != null && d.getTrustScore() >= 85.0)
                    .count();

            long improvingDevices = allDevices.stream()
                    .filter(d -> d.getTrustScore() != null && d.getTrustScore() >= 70.0 && !d.isQuarantined())
                    .count();

            double avgTrustScore = allDevices.stream()
                    .mapToDouble(d -> d.getTrustScore() != null ? d.getTrustScore() : 0.0)
                    .average()
                    .orElse(0.0);

            metrics.put("totalDevices", allDevices.size());
            metrics.put("healthyDevices", healthyDevices);
            metrics.put("improvingDevices", improvingDevices);
            metrics.put("averageTrustScore", Math.round(avgTrustScore * 100.0) / 100.0);
            metrics.put("lastUpdated", Instant.now());

        } catch (Exception e) {
            logger.error("Error calculating system healthy behavior metrics: {}", e.getMessage());
            metrics.put("error", "Unable to calculate metrics");
        }

        return metrics;
    }
}