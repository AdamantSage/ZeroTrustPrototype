package edu.university.iot.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

import edu.university.iot.entity.DeviceRegistry;
import edu.university.iot.repository.DeviceRegistryRepository;

@Service
public class TelemetryProcessorService {

    private final LocationService contextService;
    private final SessionManagementService sessionService;
    private final IdentityVerificationService identityService;
    private final FirmwareService firmwareService;
    private final AnomalyDetectorService anomalyService;
    private final ComplianceService complianceService;
    private final TrustScoreService trustService;
    private final QuarantineService quarantineService;
    private final DeviceRegistryRepository registryRepo;

    public TelemetryProcessorService(
        LocationService contextService,
        SessionManagementService sessionService,
        IdentityVerificationService identityService,
        FirmwareService firmwareService,
        AnomalyDetectorService anomalyService,
        ComplianceService complianceService,
        TrustScoreService trustService,
        QuarantineService quarantineService,
        DeviceRegistryRepository registryRepo) {

        this.contextService = contextService;
        this.sessionService = sessionService;
        this.identityService = identityService;
        this.firmwareService = firmwareService;
        this.anomalyService = anomalyService;
        this.complianceService = complianceService;
        this.trustService = trustService;
        this.quarantineService = quarantineService;
        this.registryRepo = registryRepo;
    }

    @Transactional
    public void process(Map<String, Object> telemetry) {
        String deviceId = (String) telemetry.get("deviceId");

        try {
            // Set telemetry context for detailed trust tracking
            TrustScoreService.setCurrentTelemetryContext(telemetry);

            // 1) Start or refresh session
            String sessionId = sessionService.startOrRefreshSession(deviceId);

            DeviceRegistry device = registryRepo.findById(deviceId)
                .orElseThrow(() -> new IllegalStateException("Unknown device: " + deviceId));

            // 2) If already quarantined, terminate session and skip
            if (device.isQuarantined()) {
                sessionService.terminateSession(sessionId, "Device already quarantined");
                return;
            }

            // Get pre-processing trust score for comparison
            double preTrustScore = device.getTrustScore() != null ? device.getTrustScore() : 50.0;
            
            // 3) Context check via LocationService
            boolean contextUnchanged = contextService.validateContext(telemetry);
            if (!contextUnchanged) {
                sessionService.pauseSession(sessionId, "Location/IP change detected");
                logSecurityEvent(deviceId, "CONTEXT_CHANGE", 
                    "Location or network context changed", telemetry);
            }

            // 4) Identity verification
            boolean identityOk = identityService.verifyIdentity(telemetry);
            if (!identityOk) {
                logSecurityEvent(deviceId, "IDENTITY_FAILURE", 
                    "Device identity verification failed", telemetry);
            }

            // Combined identity + context check
            boolean overallIdentityOk = identityOk && contextUnchanged;

            // 5) Firmware & patch validation
            boolean firmwareOk = firmwareService.validateAndLogFirmware(telemetry);
            if (!firmwareOk) {
                logSecurityEvent(deviceId, "FIRMWARE_VIOLATION", 
                    "Firmware does not meet compliance requirements", telemetry);
            }

            // 6) Anomaly detection
            boolean anomalyDetected = anomalyService.checkAnomaly(telemetry);
            if (anomalyDetected) {
                logSecurityEvent(deviceId, "ANOMALY_DETECTED", 
                    "Behavioral or resource anomaly detected", telemetry);
            }

            // 7) Compliance evaluation
            boolean compliant = complianceService.evaluateCompliance(telemetry);
            if (!compliant) {
                logSecurityEvent(deviceId, "COMPLIANCE_VIOLATION", 
                    "Device does not meet policy compliance", telemetry);
            }

            // 8) Adjust trust score with enhanced tracking
            trustService.adjustTrustWithContext(deviceId, overallIdentityOk, contextUnchanged, 
                                              firmwareOk, anomalyDetected, compliant, telemetry);

            // 9) Reload device to get updated trust & quarantine flags
            device = registryRepo.findById(deviceId).get();
            double postTrustScore = device.getTrustScore() != null ? device.getTrustScore() : 0.0;

            // Log significant trust score changes
            if (Math.abs(postTrustScore - preTrustScore) > 5.0) {
                String changeType = postTrustScore > preTrustScore ? "IMPROVEMENT" : "DEGRADATION";
                logSecurityEvent(deviceId, "TRUST_CHANGE_" + changeType, 
                    String.format("Trust score changed from %.1f to %.1f", preTrustScore, postTrustScore), 
                    telemetry);
            }

            // 10) Handle trust-based actions
            if (!device.isTrusted()) {
                String reason = String.format("Trust score below threshold: %.1f < %.1f", 
                                            postTrustScore, 70.0);
                
                sessionService.terminateSession(sessionId, reason);
                quarantineService.quarantineDevice(deviceId, reason);
                
                logSecurityEvent(deviceId, "QUARANTINE_INITIATED", reason, telemetry);
            }

            // 11) Check for rapid trust degradation pattern
            if (postTrustScore < preTrustScore - 10) {
                logSecurityEvent(deviceId, "RAPID_TRUST_DEGRADATION", 
                    "Device trust score dropped significantly in single cycle", telemetry);
            }

            // 12) Additional proactive monitoring
            performProactiveMonitoring(deviceId, telemetry, device);

        } catch (Exception e) {
            logSecurityEvent(deviceId, "PROCESSING_ERROR", 
                "Error during telemetry processing: " + e.getMessage(), telemetry);
            throw e;
        } finally {
            // Always clear telemetry context
            TrustScoreService.clearCurrentTelemetryContext();
        }
    }

    /**
     * Enhanced security event logging with detailed context
     */
    private void logSecurityEvent(String deviceId, String eventType, String description, 
                                Map<String, Object> telemetry) {
        try {
            String location = (String) telemetry.get("location");
            String ipAddress = (String) telemetry.get("ipAddress");
            Double anomalyScore = (Double) telemetry.get("anomalyScore");
            
            System.out.println(String.format(
                "[SECURITY EVENT] Device: %s | Type: %s | Location: %s | IP: %s | Anomaly: %.3f | Description: %s",
                deviceId, eventType, location, ipAddress, 
                anomalyScore != null ? anomalyScore : 0.0, description));
                
        } catch (Exception e) {
            System.err.println("Failed to log security event: " + e.getMessage());
        }
    }

    /**
     * Proactive monitoring for early threat detection
     */
    private void performProactiveMonitoring(String deviceId, Map<String, Object> telemetry, 
                                          DeviceRegistry device) {
        try {
            // Check for suspicious activity patterns
            Integer suspiciousScore = (Integer) telemetry.get("suspiciousActivityScore");
            Integer consecutiveAnomalies = (Integer) telemetry.get("consecutiveAnomalies");
            
            if (suspiciousScore != null && suspiciousScore > 70) {
                logSecurityEvent(deviceId, "HIGH_SUSPICIOUS_ACTIVITY", 
                    "Suspicious activity score exceeded threshold: " + suspiciousScore, telemetry);
            }
            
            if (consecutiveAnomalies != null && consecutiveAnomalies > 5) {
                logSecurityEvent(deviceId, "PERSISTENT_ANOMALIES", 
                    "Device showing persistent anomalous behavior: " + consecutiveAnomalies + " consecutive anomalies", 
                    telemetry);
            }

            // Check for resource consumption patterns
            Double cpuUsage = (Double) telemetry.get("cpuUsage");
            Double memoryUsage = (Double) telemetry.get("memoryUsage");
            Double networkTraffic = (Double) telemetry.get("networkTrafficVolume");

            if (cpuUsage != null && cpuUsage > 95) {
                logSecurityEvent(deviceId, "RESOURCE_EXHAUSTION", 
                    "Extreme CPU usage detected: " + cpuUsage + "%", telemetry);
            }

            if (memoryUsage != null && memoryUsage > 95) {
                logSecurityEvent(deviceId, "MEMORY_EXHAUSTION", 
                    "Extreme memory usage detected: " + memoryUsage + "%", telemetry);
            }

            // Check for potential malware indicators
            Boolean malwareDetected = (Boolean) telemetry.get("malwareSignatureDetected");
            if (Boolean.TRUE.equals(malwareDetected)) {
                logSecurityEvent(deviceId, "MALWARE_SIGNATURE", 
                    "Malware signature detected on device", telemetry);
            }

            // Check for off-hours activity (potential indicator of compromise)
            String deviceProfile = (String) telemetry.get("deviceProfile");
            if ("ENTERPRISE_MANAGED".equals(deviceProfile)) {
                int hour = java.time.LocalTime.now().getHour();
                if (hour < 6 || hour > 22) { // Outside typical business hours
                    Double anomalyScore = (Double) telemetry.get("anomalyScore");
                    if (anomalyScore != null && anomalyScore > 0.3) {
                        logSecurityEvent(deviceId, "OFF_HOURS_ANOMALY", 
                            "Anomalous activity during off-hours", telemetry);
                    }
                }
            }

            // Geographic anomaly detection
            String location = (String) telemetry.get("location");
            if (location != null && location.contains("Off-Campus") && 
                "ENTERPRISE_MANAGED".equals(deviceProfile)) {
                Double trustScore = device.getTrustScore();
                if (trustScore != null && trustScore < 60) {
                    logSecurityEvent(deviceId, "GEOGRAPHIC_RISK", 
                        "Enterprise device with low trust score detected off-campus", telemetry);
                }
            }

        } catch (Exception e) {
            System.err.println("Error in proactive monitoring for device " + deviceId + ": " + e.getMessage());
        }
    }

    /**
     * Get processing statistics for monitoring dashboard
     */
    public Map<String, Object> getProcessingStatistics() {
        Map<String, Object> stats = new java.util.HashMap<>();
        
        try {
            // Get basic device statistics
            var allDevices = registryRepo.findAll();
            
            long totalDevices = allDevices.size();
            long trustedDevices = allDevices.stream().mapToLong(d -> d.isTrusted() ? 1L : 0L).sum();
            long quarantinedDevices = allDevices.stream().mapToLong(d -> d.isQuarantined() ? 1L : 0L).sum();
            
            double avgTrustScore = allDevices.stream()
                .mapToDouble(d -> d.getTrustScore() != null ? d.getTrustScore() : 0.0)
                .average().orElse(0.0);

            stats.put("totalDevices", totalDevices);
            stats.put("trustedDevices", trustedDevices);
            stats.put("untrustedDevices", totalDevices - trustedDevices);
            stats.put("quarantinedDevices", quarantinedDevices);
            stats.put("averageTrustScore", Math.round(avgTrustScore * 100.0) / 100.0);
            
            // Trust score distribution
            Map<String, Long> trustDistribution = new java.util.HashMap<>();
            trustDistribution.put("excellent", allDevices.stream().mapToLong(d -> 
                d.getTrustScore() != null && d.getTrustScore() >= 85 ? 1L : 0L).sum());
            trustDistribution.put("good", allDevices.stream().mapToLong(d -> 
                d.getTrustScore() != null && d.getTrustScore() >= 70 && d.getTrustScore() < 85 ? 1L : 0L).sum());
            trustDistribution.put("concerning", allDevices.stream().mapToLong(d -> 
                d.getTrustScore() != null && d.getTrustScore() >= 50 && d.getTrustScore() < 70 ? 1L : 0L).sum());
            trustDistribution.put("critical", allDevices.stream().mapToLong(d -> 
                d.getTrustScore() != null && d.getTrustScore() < 50 ? 1L : 0L).sum());
                
            stats.put("trustDistribution", trustDistribution);
            stats.put("lastUpdated", System.currentTimeMillis());
            
        } catch (Exception e) {
            stats.put("error", "Unable to calculate processing statistics");
        }
        
        return stats;
    }
}