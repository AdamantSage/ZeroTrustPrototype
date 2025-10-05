package edu.university.iot.service;

import edu.university.iot.entity.DeviceRegistry;
import edu.university.iot.model.AnomalyLog;
import edu.university.iot.model.ComplianceLog;
import edu.university.iot.model.LocationNetworkChange;
import edu.university.iot.model.TrustScoreHistory;
import edu.university.iot.model.dtoModel.AlertDto;
import edu.university.iot.repository.DeviceRegistryRepository;
import edu.university.iot.repository.TrustScoreHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AlertService {

    private final AnomalyDetectorService anomalyService;
    private final ComplianceService complianceService;
    private final LocationService locationService;
    private final DeviceRegistryRepository deviceRepo;
    private final TrustScoreService trustScoreService;
    private final TrustScoreHistoryRepository trustScoreHistoryRepo;

    public AlertService(
            AnomalyDetectorService anomalyService,
            ComplianceService complianceService,
            LocationService locationService,
            DeviceRegistryRepository deviceRepo,
            TrustScoreService trustScoreService,
            TrustScoreHistoryRepository trustScoreHistoryRepo) {
        this.anomalyService = anomalyService;
        this.complianceService = complianceService;
        this.locationService = locationService;
        this.deviceRepo = deviceRepo;
        this.trustScoreService = trustScoreService;
        this.trustScoreHistoryRepo = trustScoreHistoryRepo;
    }

    /**
     * Generate comprehensive alerts for all devices (including positive alerts)
     */
    public List<AlertDto> generateSystemAlerts() {
        List<AlertDto> alerts = new ArrayList<>();
        List<DeviceRegistry> allDevices = deviceRepo.findAll();

        for (DeviceRegistry device : allDevices) {
            alerts.addAll(generateDeviceAlerts(device.getDeviceId()));
        }

        // Sort by severity and timestamp
        return alerts.stream()
                .sorted((a, b) -> {
                    int severityOrder = getSeverityOrder(a.getSeverity()) - getSeverityOrder(b.getSeverity());
                    return severityOrder != 0 ? severityOrder : b.getTimestamp().compareTo(a.getTimestamp());
                })
                .limit(100) // Limit to most recent/severe 100 alerts
                .collect(Collectors.toList());
    }

    /**
     * Generate alerts for a specific device (including positive healthy behavior
     * alerts)
     */
    public List<AlertDto> generateDeviceAlerts(String deviceId) {
        List<AlertDto> alerts = new ArrayList<>();

        try {
            DeviceRegistry device = deviceRepo.findById(deviceId).orElse(null);
            if (device == null)
                return alerts;

            // 1. Trust Score Alerts (including positive improvements)
            generateTrustScoreAlerts(deviceId, alerts);

            // 2. Healthy Behavior Positive Alerts
            generateHealthyBehaviorAlerts(deviceId, alerts);

            // 3. Anomaly-based Alerts
            generateAnomalyAlerts(deviceId, alerts);

            // 4. Compliance Alerts
            generateComplianceAlerts(deviceId, alerts);

            // 5. Location-based Alerts
            generateLocationAlerts(deviceId, alerts);

            // 6. Device Status Alerts
            generateDeviceStatusAlerts(device, alerts);

        } catch (Exception e) {
            System.err.println("Error generating alerts for device " + deviceId + ": " + e.getMessage());
        }

        return alerts;
    }

    /**
     * NEW: Generate positive alerts for healthy behavior
     */
    private void generateHealthyBehaviorAlerts(String deviceId, List<AlertDto> alerts) {
        try {
            Instant cutoff = Instant.now().minusSeconds(30 * 60); // Last 30 minutes

            List<TrustScoreHistory> recentImprovements = trustScoreHistoryRepo
                    .findByDeviceIdAndTimestampAfterOrderByTimestampDesc(deviceId, cutoff)
                    .stream()
                    .filter(h -> h.getScoreChange() > 0)
                    .collect(Collectors.toList());

            if (!recentImprovements.isEmpty()) {
                // Calculate total improvement
                double totalImprovement = recentImprovements.stream()
                        .mapToDouble(TrustScoreHistory::getScoreChange)
                        .sum();

                double currentScore = recentImprovements.get(0).getNewScore();

                // Alert for significant improvement
                if (totalImprovement >= 10) {
                    alerts.add(createAlert(deviceId, "TRUST_IMPROVEMENT", "POSITIVE",
                            String.format("Trust score improved by %.1f points (now: %.1f)",
                                    totalImprovement, currentScore),
                            "healthy"));
                }

                // Alert for reaching trusted status
                if (currentScore >= 70.0 && recentImprovements.stream()
                        .anyMatch(h -> h.getOldScore() < 70.0 && h.getNewScore() >= 70.0)) {
                    alerts.add(createAlert(deviceId, "TRUSTED_STATUS", "POSITIVE",
                            String.format("Device achieved trusted status (%.1f)", currentScore),
                            "healthy"));
                }

                // Alert for excellent health
                if (currentScore >= 90.0) {
                    alerts.add(createAlert(deviceId, "EXCELLENT_HEALTH", "INFO",
                            String.format("Device maintaining excellent health (%.1f)", currentScore),
                            "healthy"));
                }

                // Alert for consistent healthy behavior
                if (recentImprovements.size() >= 5) {
                    long consecutiveHealthy = recentImprovements.stream()
                            .mapToLong(h -> h.isCompliancePassed() && !h.isAnomalyDetected() &&
                                    h.isIdentityPassed() && h.isFirmwareValid() ? 1L : 0L)
                            .sum();

                    if (consecutiveHealthy >= 5) {
                        alerts.add(createAlert(deviceId, "CONSISTENT_HEALTH", "POSITIVE",
                                "Device showing consistent healthy behavior",
                                "healthy"));
                    }
                }
            }

            // Alert for recovery from quarantine
            DeviceRegistry device = deviceRepo.findById(deviceId).orElse(null);
            if (device != null && !device.isQuarantined() && device.getTrustScore() >= 70.0) {
                // Check if was recently quarantined
                List<TrustScoreHistory> veryRecent = trustScoreHistoryRepo
                        .findByDeviceIdAndTimestampAfterOrderByTimestampDesc(
                                deviceId,
                                Instant.now().minusSeconds(60 * 60) // Last hour
                        );

                if (veryRecent.stream().anyMatch(h -> h.getOldScore() < 70.0)) {
                    alerts.add(createAlert(deviceId, "RECOVERY", "POSITIVE",
                            "Device recovered from untrusted status",
                            "healthy"));
                }
            }

        } catch (Exception e) {
            System.err.println("Error generating healthy behavior alerts: " + e.getMessage());
        }
    }

    private void generateTrustScoreAlerts(String deviceId, List<AlertDto> alerts) {
        try {
            DeviceRegistry device = deviceRepo.findById(deviceId).orElse(null);
            if (device == null)
                return;

            Double currentTrustScore = device.getTrustScore();
            if (currentTrustScore == null) {
                currentTrustScore = 50.0;
            }

            // Negative alerts
            if (currentTrustScore < 30) {
                alerts.add(createAlert(deviceId, "TRUST_SCORE", "CRITICAL",
                        String.format("Very low trust score (%.1f)", currentTrustScore),
                        "security"));
            } else if (currentTrustScore < 50) {
                alerts.add(createAlert(deviceId, "TRUST_SCORE", "HIGH",
                        String.format("Low trust score (%.1f)", currentTrustScore),
                        "security"));
            }

            // Check for rapid trust score degradation
            List<TrustScoreHistory> recentHistory = trustScoreHistoryRepo
                    .findByDeviceIdAndTimestampAfterOrderByTimestampDesc(
                            deviceId,
                            Instant.now().minusSeconds(6 * 3600));

            if (recentHistory.size() >= 2) {
                double latestScore = recentHistory.get(0).getNewScore();
                double oldestScore = recentHistory.get(recentHistory.size() - 1).getNewScore();
                double scoreDrop = oldestScore - latestScore;

                if (scoreDrop > 20) {
                    alerts.add(createAlert(deviceId, "TRUST_DEGRADATION", "HIGH",
                            String.format("Rapid trust score drop (%.1f points)", scoreDrop),
                            "security"));
                }
            }
        } catch (Exception e) {
            System.err.println("Error generating trust score alerts: " + e.getMessage());
        }
    }

    private void generateAnomalyAlerts(String deviceId, List<AlertDto> alerts) {
        try {
            List<AnomalyLog> recentAnomalies = anomalyService.getLogs(deviceId).stream()
                    .filter(log -> log.getTimestamp().isAfter(
                            LocalDateTime.now().minusHours(24).atZone(ZoneId.systemDefault()).toInstant()))
                    .filter(AnomalyLog::isAnomalyDetected)
                    .collect(Collectors.toList());

            for (AnomalyLog anomaly : recentAnomalies.stream().limit(5).collect(Collectors.toList())) {
                String severity = anomaly.getReason().toLowerCase().contains("malware") ? "CRITICAL" : "HIGH";
                alerts.add(createAlert(deviceId, "ANOMALY", severity,
                        "Anomaly detected: " + anomaly.getReason(),
                        "security"));
            }

            if (recentAnomalies.size() >= 3) {
                alerts.add(createAlert(deviceId, "FREQUENT_ANOMALIES", "HIGH",
                        String.format("%d anomalies detected in 24h", recentAnomalies.size()),
                        "security"));
            }
        } catch (Exception e) {
            System.err.println("Error generating anomaly alerts: " + e.getMessage());
        }
    }

    private void generateComplianceAlerts(String deviceId, List<AlertDto> alerts) {
        try {
            List<ComplianceLog> recentViolations = complianceService.getLogs(deviceId).stream()
                    .filter(log -> log.getTimestamp().isAfter(
                            LocalDateTime.now().minusHours(24).atZone(ZoneId.systemDefault()).toInstant()))
                    .filter(log -> !log.isCompliant())
                    .collect(Collectors.toList());

            for (ComplianceLog violation : recentViolations.stream().limit(3).collect(Collectors.toList())) {
                alerts.add(createAlert(deviceId, "COMPLIANCE", "MEDIUM",
                        "Compliance violation: " + violation.getViolations(),
                        "compliance"));
            }
        } catch (Exception e) {
            System.err.println("Error generating compliance alerts: " + e.getMessage());
        }
    }

    private void generateLocationAlerts(String deviceId, List<AlertDto> alerts) {
        try {
            List<LocationNetworkChange> recentChanges = locationService.getLocationHistory(deviceId, 24);

            if (recentChanges.size() >= 5) {
                alerts.add(createAlert(deviceId, "EXCESSIVE_MOVEMENT", "HIGH",
                        String.format("%d location changes in 24h", recentChanges.size()),
                        "behavioral"));
            }

            for (LocationNetworkChange change : recentChanges.stream().limit(3).collect(Collectors.toList())) {
                if (isHighRiskLocationChange(change)) {
                    alerts.add(createAlert(deviceId, "SUSPICIOUS_LOCATION", "HIGH",
                            String.format("Moved from %s to %s", change.getOldLocation(), change.getNewLocation()),
                            "security"));
                }
            }
        } catch (Exception e) {
            System.err.println("Error generating location alerts: " + e.getMessage());
        }
    }

    private void generateDeviceStatusAlerts(DeviceRegistry device, List<AlertDto> alerts) {
        if (device.isQuarantined()) {
            alerts.add(createAlert(device.getDeviceId(), "QUARANTINE", "HIGH",
                    "Device is currently quarantined",
                    "security"));
        }

        if (!device.isFirmwareValid()) {
            alerts.add(createAlert(device.getDeviceId(), "FIRMWARE", "MEDIUM",
                    "Firmware validation failed or outdated",
                    "compliance"));
        }

        if (!device.isCertificateValid()) {
            alerts.add(createAlert(device.getDeviceId(), "CERTIFICATE", "HIGH",
                    "Device certificate invalid or expired",
                    "security"));
        }
    }

    private AlertDto createAlert(String deviceId, String type, String severity, String message, String category) {
        AlertDto alert = new AlertDto();
        alert.setDeviceId(deviceId);
        alert.setType(type);
        alert.setSeverity(severity);
        alert.setMessage(message);
        alert.setCategory(category);
        alert.setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        return alert;
    }

    private boolean isHighRiskLocationChange(LocationNetworkChange change) {
        String oldLoc = change.getOldLocation();
        String newLoc = change.getNewLocation();

        return (oldLoc != null && oldLoc.contains("Admin") && newLoc != null && newLoc.contains("Off-Campus")) ||
                (oldLoc != null && oldLoc.contains("Lab") && newLoc != null && newLoc.contains("Off-Campus")) ||
                (newLoc != null && newLoc.contains("Admin-Building"));
    }

    private int getSeverityOrder(String severity) {
        switch (severity.toUpperCase()) {
            case "CRITICAL":
                return 0;
            case "HIGH":
                return 1;
            case "MEDIUM":
                return 2;
            case "LOW":
                return 3;
            case "POSITIVE":
                return 4; // Positive alerts at the end
            case "INFO":
                return 5;
            default:
                return 6;
        }
    }

    public Map<String, Object> getAlertsSummary() {
        List<AlertDto> allAlerts = generateSystemAlerts();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalAlerts", allAlerts.size());
        summary.put("criticalAlerts",
                allAlerts.stream().mapToInt(a -> "CRITICAL".equals(a.getSeverity()) ? 1 : 0).sum());
        summary.put("highAlerts", allAlerts.stream().mapToInt(a -> "HIGH".equals(a.getSeverity()) ? 1 : 0).sum());
        summary.put("mediumAlerts", allAlerts.stream().mapToInt(a -> "MEDIUM".equals(a.getSeverity()) ? 1 : 0).sum());
        summary.put("positiveAlerts",
                allAlerts.stream().mapToInt(a -> "POSITIVE".equals(a.getSeverity()) ? 1 : 0).sum());

        Map<String, Long> byCategory = allAlerts.stream()
                .collect(Collectors.groupingBy(AlertDto::getCategory, Collectors.counting()));
        summary.put("alertsByCategory", byCategory);

        return summary;
    }
}