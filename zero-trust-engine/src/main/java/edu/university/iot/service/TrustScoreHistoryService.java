package edu.university.iot.service;

import edu.university.iot.entity.DeviceRegistry;
import edu.university.iot.model.TrustScoreHistory;
import edu.university.iot.model.dtoModel.TrustChangeAnalysisDto;
import edu.university.iot.model.dtoModel.TrustScoreTimelineDto;
import edu.university.iot.repository.DeviceRegistryRepository;
import edu.university.iot.repository.TrustScoreHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for tracking and analyzing trust score changes over time.
 * Provides detailed insights into what factors are affecting device trust
 * scores.
 */
@Service
public class TrustScoreHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(TrustScoreHistoryService.class);

    private final TrustScoreHistoryRepository historyRepo;
    private final DeviceRegistryRepository registryRepo;

    public TrustScoreHistoryService(TrustScoreHistoryRepository historyRepo,
            DeviceRegistryRepository registryRepo) {
        this.historyRepo = historyRepo;
        this.registryRepo = registryRepo;
    }

    /**
     * Records a trust score change with detailed reasoning
     */
    @Transactional
    public void recordTrustScoreChange(String deviceId,
            double oldScore,
            double newScore,
            Map<String, Boolean> factorResults,
            Map<String, Object> telemetryContext) {
        try {
            // Only record if there's a meaningful change (> 0.5 points)
            if (Math.abs(newScore - oldScore) < 0.5) {
                return;
            }

            TrustScoreHistory history = new TrustScoreHistory();
            history.setDeviceId(deviceId);
            history.setOldScore(oldScore);
            history.setNewScore(newScore);
            history.setScoreChange(newScore - oldScore);
            history.setTimestamp(Instant.now());

            // Extract factor results
            history.setIdentityPassed(factorResults.getOrDefault("identity", false));
            history.setContextPassed(factorResults.getOrDefault("context", false));
            history.setFirmwareValid(factorResults.getOrDefault("firmware", false));
            history.setAnomalyDetected(factorResults.getOrDefault("anomaly", false));
            history.setCompliancePassed(factorResults.getOrDefault("compliance", false));

            // Build change reason
            // Build change reason - check for healthy behavior context
            String changeReason;
            String eventCategory = telemetryContext != null ? (String) telemetryContext.get("eventCategory") : null;

            if ("HEALTHY_BEHAVIOR".equals(eventCategory)) {
                Double bonus = telemetryContext != null ? (Double) telemetryContext.get("healthyBehaviorBonus") : null;
                Integer streak = telemetryContext != null ? (Integer) telemetryContext.get("consecutiveHealthyReports")
                        : null;

                changeReason = String.format("Healthy behavior bonus applied (+%.1f)",
                        bonus != null ? bonus : (newScore - oldScore));
                if (streak != null && streak >= 5) {
                    changeReason += String.format(" | Streak: %d reports", streak);
                }
            } else {
                changeReason = buildChangeReason(factorResults, newScore > oldScore);
            }

            history.setChangeReason(changeReason);

            // Extract telemetry context
            if (telemetryContext != null) {
                history.setLocationAtChange((String) telemetryContext.get("location"));
                history.setIpAddressAtChange((String) telemetryContext.get("ipAddress"));

                Double cpu = (Double) telemetryContext.get("cpuUsage");
                Double memory = (Double) telemetryContext.get("memoryUsage");
                Double network = (Double) telemetryContext.get("networkTrafficVolume");

                history.setCpuUsageAtChange(cpu);
                history.setMemoryUsageAtChange(memory);
                history.setNetworkTrafficAtChange(network);
            }

            // Determine severity
            history.setSeverity(determineSeverity(newScore, oldScore));

            historyRepo.save(history);

            logger.info("Trust score change recorded for device [{}]: {} -> {} ({})",
                    deviceId, oldScore, newScore, changeReason);

        } catch (Exception e) {
            logger.error("Failed to record trust score change for device [{}]: {}",
                    deviceId, e.getMessage(), e);
        }
    }

    /**
     * Get detailed trust score analysis for a device over a time period
     */
    public TrustChangeAnalysisDto analyzeTrustChanges(String deviceId, int hours) {
        TrustChangeAnalysisDto analysis = new TrustChangeAnalysisDto();
        analysis.setDeviceId(deviceId);
        analysis.setAnalysisPeriodHours(hours);

        try {
            LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
            Instant cutoffInstant = cutoff.atZone(ZoneId.systemDefault()).toInstant();

            List<TrustScoreHistory> recentChanges = historyRepo
                    .findByDeviceIdAndTimestampAfterOrderByTimestampDesc(deviceId, cutoffInstant);

            if (recentChanges.isEmpty()) {
                analysis.setTotalChanges(0);
                analysis.setSummary("No significant trust score changes in the specified period");
                return analysis;
            }

            // Basic statistics
            analysis.setTotalChanges(recentChanges.size());

            double totalChange = recentChanges.stream()
                    .mapToDouble(TrustScoreHistory::getScoreChange)
                    .sum();
            analysis.setNetScoreChange(totalChange);

            long improvingChanges = recentChanges.stream()
                    .mapToLong(h -> h.getScoreChange() > 0 ? 1L : 0L)
                    .sum();
            analysis.setImprovingChanges((int) improvingChanges);
            analysis.setDegradingChanges(recentChanges.size() - (int) improvingChanges);

            // Factor analysis
            analysis.setFactorImpacts(analyzeFacorImpacts(recentChanges));

            // Trend analysis
            analysis.setTrend(determineTrend(recentChanges));

            // Recent critical events
            analysis.setCriticalEvents(findCriticalEvents(recentChanges));

            // Pattern detection
            analysis.setPatterns(detectPatterns(recentChanges));

            // Risk assessment
            analysis.setRiskLevel(assessCurrentRisk(deviceId, recentChanges));

            // Summary
            analysis.setSummary(generateSummary(analysis));

        } catch (Exception e) {
            logger.error("Failed to analyze trust changes for device [{}]: {}",
                    deviceId, e.getMessage(), e);
            analysis.setSummary("Error analyzing trust score changes");
        }

        return analysis;
    }

    /**
     * Get trust score timeline for visualization
     */
    public List<TrustScoreTimelineDto> getTrustScoreTimeline(String deviceId, int days) {
        List<TrustScoreTimelineDto> timeline = new ArrayList<>();

        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
            Instant cutoffInstant = cutoff.atZone(ZoneId.systemDefault()).toInstant();

            List<TrustScoreHistory> history = historyRepo
                    .findByDeviceIdAndTimestampAfterOrderByTimestampAsc(deviceId, cutoffInstant);

            // Get current score from registry
            DeviceRegistry device = registryRepo.findById(deviceId).orElse(null);
            double currentScore = device != null && device.getTrustScore() != null ? device.getTrustScore() : 50.0;

            if (history.isEmpty()) {
                // No history - create single point with current score
                TrustScoreTimelineDto point = new TrustScoreTimelineDto();
                point.setTimestamp(Instant.now());
                point.setTrustScore(currentScore);
                point.setEventType("CURRENT");
                point.setDescription("Current trust score");
                timeline.add(point);
                return timeline;
            }

            // Build timeline from history
            for (TrustScoreHistory record : history) {
                TrustScoreTimelineDto point = new TrustScoreTimelineDto();
                point.setTimestamp(record.getTimestamp());
                point.setTrustScore(record.getNewScore());
                point.setScoreChange(record.getScoreChange());
                point.setEventType(determineEventType(record));
                point.setDescription(record.getChangeReason());
                point.setSeverity(record.getSeverity());

                // Add context
                Map<String, Object> context = new HashMap<>();
                if (record.getLocationAtChange() != null) {
                    context.put("location", record.getLocationAtChange());
                }
                if (record.getCpuUsageAtChange() != null) {
                    context.put("cpuUsage", record.getCpuUsageAtChange());
                }
                point.setContext(context);

                timeline.add(point);
            }

            // Add current point if different from last recorded
            if (!timeline.isEmpty()) {
                TrustScoreTimelineDto lastPoint = timeline.get(timeline.size() - 1);
                if (Math.abs(lastPoint.getTrustScore() - currentScore) > 0.5) {
                    TrustScoreTimelineDto currentPoint = new TrustScoreTimelineDto();
                    currentPoint.setTimestamp(Instant.now());
                    currentPoint.setTrustScore(currentScore);
                    currentPoint.setEventType("CURRENT");
                    currentPoint.setDescription("Current trust score");
                    timeline.add(currentPoint);
                }
            }

        } catch (Exception e) {
            logger.error("Failed to get trust score timeline for device [{}]: {}",
                    deviceId, e.getMessage(), e);
        }

        return timeline;
    }

    /**
     * Get devices with recent concerning trust score changes
     */
    public List<String> getDevicesWithConcerningChanges(int hours) {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
            Instant cutoffInstant = cutoff.atZone(ZoneId.systemDefault()).toInstant();

            return historyRepo.findDevicesWithRecentDegradation(cutoffInstant);
        } catch (Exception e) {
            logger.error("Failed to get devices with concerning changes: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // === PRIVATE HELPER METHODS ===

    private String buildChangeReason(Map<String, Boolean> factors, boolean improved) {
        List<String> reasons = new ArrayList<>();

        if (improved) {
            if (factors.getOrDefault("identity", false))
                reasons.add("Identity verified");
            if (factors.getOrDefault("context", false))
                reasons.add("Context stable");
            if (factors.getOrDefault("firmware", false))
                reasons.add("Firmware compliant");
            if (!factors.getOrDefault("anomaly", true))
                reasons.add("No anomalies detected");
            if (factors.getOrDefault("compliance", false))
                reasons.add("Policy compliant");
        } else {
            if (!factors.getOrDefault("identity", true))
                reasons.add("Identity verification failed");
            if (!factors.getOrDefault("context", true))
                reasons.add("Context change detected");
            if (!factors.getOrDefault("firmware", true))
                reasons.add("Firmware non-compliant");
            if (factors.getOrDefault("anomaly", false))
                reasons.add("Anomalies detected");
            if (!factors.getOrDefault("compliance", true))
                reasons.add("Policy violations");
        }

        return reasons.isEmpty() ? "General trust adjustment" : String.join(", ", reasons);
    }

    private String determineSeverity(double newScore, double oldScore) {
        double change = Math.abs(newScore - oldScore);

        if (change >= 20)
            return "CRITICAL";
        if (change >= 10)
            return "HIGH";
        if (change >= 5)
            return "MEDIUM";
        return "LOW";
    }

    private Map<String, Integer> analyzeFacorImpacts(List<TrustScoreHistory> changes) {
        Map<String, Integer> impacts = new HashMap<>();
        impacts.put("identityFailures", 0);
        impacts.put("contextChanges", 0);
        impacts.put("firmwareIssues", 0);
        impacts.put("anomalies", 0);
        impacts.put("complianceViolations", 0);

        for (TrustScoreHistory change : changes) {
            if (!change.isIdentityPassed())
                impacts.merge("identityFailures", 1, Integer::sum);
            if (!change.isContextPassed())
                impacts.merge("contextChanges", 1, Integer::sum);
            if (!change.isFirmwareValid())
                impacts.merge("firmwareIssues", 1, Integer::sum);
            if (change.isAnomalyDetected())
                impacts.merge("anomalies", 1, Integer::sum);
            if (!change.isCompliancePassed())
                impacts.merge("complianceViolations", 1, Integer::sum);
        }

        return impacts;
    }

    private String determineTrend(List<TrustScoreHistory> changes) {
        if (changes.size() < 3)
            return "INSUFFICIENT_DATA";

        // Look at recent 5 changes
        List<TrustScoreHistory> recent = changes.stream()
                .limit(5)
                .collect(Collectors.toList());

        double avgChange = recent.stream()
                .mapToDouble(TrustScoreHistory::getScoreChange)
                .average()
                .orElse(0.0);

        if (avgChange > 2)
            return "IMPROVING";
        if (avgChange < -2)
            return "DEGRADING";
        return "STABLE";
    }

    private List<String> findCriticalEvents(List<TrustScoreHistory> changes) {
        return changes.stream()
                .filter(h -> "CRITICAL".equals(h.getSeverity()) || "HIGH".equals(h.getSeverity()))
                .limit(5)
                .map(h -> String.format("%s: %s (%.1f points)",
                        h.getTimestamp().toString().substring(0, 19),
                        h.getChangeReason(),
                        h.getScoreChange()))
                .collect(Collectors.toList());
    }

    private List<String> detectPatterns(List<TrustScoreHistory> changes) {
        List<String> patterns = new ArrayList<>();

        // Check for recurring location-based issues
        Map<String, Long> locationIssues = changes.stream()
                .filter(h -> h.getLocationAtChange() != null && h.getScoreChange() < 0)
                .collect(Collectors.groupingBy(
                        TrustScoreHistory::getLocationAtChange,
                        Collectors.counting()));

        locationIssues.entrySet().stream()
                .filter(entry -> entry.getValue() >= 3)
                .forEach(entry -> patterns.add(
                        String.format("Recurring issues at %s (%d occurrences)",
                                entry.getKey(), entry.getValue())));

        // Check for time-based patterns
        Map<Integer, Long> hourlyIssues = changes.stream()
                .filter(h -> h.getScoreChange() < 0)
                .collect(Collectors.groupingBy(
                        h -> h.getTimestamp().atZone(ZoneId.systemDefault()).getHour(),
                        Collectors.counting()));

        hourlyIssues.entrySet().stream()
                .filter(entry -> entry.getValue() >= 3)
                .forEach(entry -> patterns.add(
                        String.format("Issues frequently occur around %02d:00 (%d times)",
                                entry.getKey(), entry.getValue())));

        return patterns;
    }

    private String assessCurrentRisk(String deviceId, List<TrustScoreHistory> recentChanges) {
        try {
            DeviceRegistry device = registryRepo.findById(deviceId).orElse(null);
            if (device == null)
                return "UNKNOWN";

            Double currentScore = device.getTrustScore();
            if (currentScore == null)
                return "UNKNOWN";

            // Recent degradation factor
            long recentDegradations = recentChanges.stream()
                    .limit(5)
                    .mapToLong(h -> h.getScoreChange() < 0 ? 1L : 0L)
                    .sum();

            // Critical events factor
            long criticalEvents = recentChanges.stream()
                    .mapToLong(h -> "CRITICAL".equals(h.getSeverity()) ? 1L : 0L)
                    .sum();

            if (currentScore < 30 || criticalEvents > 0)
                return "CRITICAL";
            if (currentScore < 50 || recentDegradations >= 3)
                return "HIGH";
            if (currentScore < 70 || recentDegradations >= 2)
                return "MEDIUM";
            return "LOW";

        } catch (Exception e) {
            return "ERROR";
        }
    }

    private String generateSummary(TrustChangeAnalysisDto analysis) {
        StringBuilder summary = new StringBuilder();

        if (analysis.getTotalChanges() == 0) {
            return "Device trust score has been stable with no significant changes";
        }

        if (analysis.getNetScoreChange() > 10) {
            summary.append("Trust score has significantly improved. ");
        } else if (analysis.getNetScoreChange() < -10) {
            summary.append("Trust score has significantly degraded. ");
        } else {
            summary.append("Trust score has shown minor fluctuations. ");
        }

        // Add primary factors
        Map<String, Integer> factors = analysis.getFactorImpacts();
        int maxImpact = factors.values().stream().mapToInt(Integer::intValue).max().orElse(0);

        if (maxImpact > 0) {
            String primaryFactor = factors.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("");

            summary.append("Primary concern: ").append(formatFactorName(primaryFactor)).append(". ");
        }

        summary.append(String.format("Trend: %s. Risk Level: %s",
                analysis.getTrend().toLowerCase(),
                analysis.getRiskLevel().toLowerCase()));

        return summary.toString();
    }

    private String formatFactorName(String factor) {
        switch (factor) {
            case "identityFailures":
                return "identity verification issues";
            case "contextChanges":
                return "location/network changes";
            case "firmwareIssues":
                return "firmware compliance problems";
            case "anomalies":
                return "anomalous behavior";
            case "complianceViolations":
                return "policy violations";
            default:
                return factor;
        }
    }

    private String determineEventType(TrustScoreHistory record) {
        // Check change reason for healthy behavior indicator
        if (record.getChangeReason() != null &&
                record.getChangeReason().contains("Healthy behavior bonus")) {
            return "HEALTHY_IMPROVEMENT";
        }

        // Standard event type determination
        if (record.getScoreChange() > 10)
            return "MAJOR_IMPROVEMENT";
        if (record.getScoreChange() > 2)
            return "IMPROVEMENT";
        if (record.getScoreChange() < -10)
            return "MAJOR_DEGRADATION";
        if (record.getScoreChange() < -2)
            return "DEGRADATION";
        return "MINOR_CHANGE";
    }
}