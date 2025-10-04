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
     * Generate comprehensive alerts for all devices
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
                    return severityOrder != 0 ? severityOrder : 
                           b.getTimestamp().compareTo(a.getTimestamp());
                })
                .limit(100) // Limit to most recent/severe 100 alerts
                .collect(Collectors.toList());
    }

    /**
     * Generate alerts for a specific device
     */
    public List<AlertDto> generateDeviceAlerts(String deviceId) {
        List<AlertDto> alerts = new ArrayList<>();
        
        try {
            DeviceRegistry device = deviceRepo.findById(deviceId).orElse(null);
            if (device == null) return alerts;

            // 1. Trust Score Alerts
            generateTrustScoreAlerts(deviceId, alerts);

            // 2. Anomaly-based Alerts
            generateAnomalyAlerts(deviceId, alerts);

            // 3. Compliance Alerts
            generateComplianceAlerts(deviceId, alerts);

            // 4. Location-based Alerts
            generateLocationAlerts(deviceId, alerts);

            // 5. Device Status Alerts
            generateDeviceStatusAlerts(device, alerts);

        } catch (Exception e) {
            System.err.println("Error generating alerts for device " + deviceId + ": " + e.getMessage());
        }

        return alerts;
    }

    private void generateTrustScoreAlerts(String deviceId, List<AlertDto> alerts) {
        try {
            // Get current trust score from device registry or calculate it
            DeviceRegistry device = deviceRepo.findById(deviceId).orElse(null);
            if (device == null) return;

            Double currentTrustScore = device.getTrustScore();
            if (currentTrustScore == null) {
                // Fallback: try to get from trust score service if it has a different method
                currentTrustScore = 50.0; // Default value or call a different method
            }

            if (currentTrustScore < 30) {
                alerts.add(createAlert(deviceId, "TRUST_SCORE", "CRITICAL",
                        String.format("Very low trust score (%.1f)", currentTrustScore),
                        "security"));
            } else if (currentTrustScore < 50) {
                alerts.add(createAlert(deviceId, "TRUST_SCORE", "HIGH",
                        String.format("Low trust score (%.1f)", currentTrustScore),
                        "security"));
            }

            // Check for rapid trust score degradation using TrustScoreHistory
            List<TrustScoreHistory> recentHistory = trustScoreHistoryRepo
                    .findByDeviceIdAndTimestampAfterOrderByTimestampDesc(
                            deviceId, 
                            Instant.now().minusSeconds(6 * 3600) // Last 6 hours
                    );

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

            // Alert for frequent anomalies
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
            
            // Alert for excessive location changes
            if (recentChanges.size() >= 5) {
                alerts.add(createAlert(deviceId, "EXCESSIVE_MOVEMENT", "HIGH",
                        String.format("%d location changes in 24h", recentChanges.size()),
                        "behavioral"));
            }

            // Alert for suspicious location patterns
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
        // Quarantine status
        if (device.isQuarantined()) {
            alerts.add(createAlert(device.getDeviceId(), "QUARANTINE", "HIGH",
                    "Device is currently quarantined",
                    "security"));
        }

        // Firmware alerts
        if (!device.isFirmwareValid()) {
            alerts.add(createAlert(device.getDeviceId(), "FIRMWARE", "MEDIUM",
                    "Firmware validation failed or outdated",
                    "compliance"));
        }

        // Certificate alerts
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
        
        // Define high-risk patterns
        return (oldLoc != null && oldLoc.contains("Admin") && newLoc != null && newLoc.contains("Off-Campus")) ||
               (oldLoc != null && oldLoc.contains("Lab") && newLoc != null && newLoc.contains("Off-Campus")) ||
               (newLoc != null && newLoc.contains("Admin-Building"));
    }

    private int getSeverityOrder(String severity) {
        switch (severity.toUpperCase()) {
            case "CRITICAL": return 0;
            case "HIGH": return 1;
            case "MEDIUM": return 2;
            case "LOW": return 3;
            default: return 4;
        }
    }

    /**
     * Get alerts summary for dashboard
     */
    public Map<String, Object> getAlertsSummary() {
        List<AlertDto> allAlerts = generateSystemAlerts();
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalAlerts", allAlerts.size());
        summary.put("criticalAlerts", allAlerts.stream().mapToInt(a -> "CRITICAL".equals(a.getSeverity()) ? 1 : 0).sum());
        summary.put("highAlerts", allAlerts.stream().mapToInt(a -> "HIGH".equals(a.getSeverity()) ? 1 : 0).sum());
        summary.put("mediumAlerts", allAlerts.stream().mapToInt(a -> "MEDIUM".equals(a.getSeverity()) ? 1 : 0).sum());
        
        // Group by category
        Map<String, Long> byCategory = allAlerts.stream()
                .collect(Collectors.groupingBy(AlertDto::getCategory, Collectors.counting()));
        summary.put("alertsByCategory", byCategory);
        
        return summary;
    }
}