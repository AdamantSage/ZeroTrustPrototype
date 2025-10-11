package edu.university.iot.controllers;

import edu.university.iot.entity.DeviceMessage;
import edu.university.iot.entity.DeviceRegistry;
import edu.university.iot.model.dtoModel.DeviceSummaryDto;
import edu.university.iot.model.dtoModel.FirmwareLogDto;
import edu.university.iot.model.ComplianceLog;
import edu.university.iot.model.AnomalyLog;
import edu.university.iot.repository.DeviceMessageRepository;
import edu.university.iot.repository.ComplianceLogRepository;
import edu.university.iot.repository.AnomalyLogRepository;
import edu.university.iot.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceRegistryService registryService;
    private final QuarantineService quarantineService;
    private final TrustScoreService trustService;
    private final DeviceMessageRepository messageRepo;
    private final TrustScoreHistoryService trustScoreHistoryService;
    private final TrustAnalysisService trustAnalysisService;
    private final FirmwareService firmwareService;
    private final ComplianceLogRepository complianceRepo;
    private final AnomalyLogRepository anomalyRepo;
    private final ComplianceService complianceService;
    private final AnomalyDetectorService anomalyService;

    public DeviceController(DeviceRegistryService registryService,
            QuarantineService quarantineService,
            TrustScoreService trustService,
            DeviceMessageRepository messageRepo,
            TrustScoreHistoryService trustScoreHistoryService,
            TrustAnalysisService trustAnalysisService,
            FirmwareService firmwareService,
            ComplianceLogRepository complianceRepo,
            AnomalyLogRepository anomalyRepo,
            ComplianceService complianceService,
            AnomalyDetectorService anomalyService) {
        this.registryService = registryService;
        this.quarantineService = quarantineService;
        this.trustService = trustService;
        this.messageRepo = messageRepo;
        this.trustScoreHistoryService = trustScoreHistoryService;
        this.trustAnalysisService = trustAnalysisService;
        this.firmwareService = firmwareService;
        this.complianceRepo = complianceRepo;
        this.anomalyRepo = anomalyRepo;
        this.complianceService = complianceService;
        this.anomalyService = anomalyService;
    }

    @GetMapping
    public ResponseEntity<List<DeviceSummaryDto>> listDevices() {
        List<DeviceSummaryDto> list = registryService.findAll().stream()
                .map(this::buildEnhancedDeviceSummary)
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    private DeviceSummaryDto buildEnhancedDeviceSummary(DeviceRegistry device) {
        String deviceId = device.getDeviceId();

        // Fetch most recent telemetry
        Optional<DeviceMessage> optMsg = messageRepo.findTopByDeviceIdOrderByTimestampDesc(deviceId);

        DeviceSummaryDto dto = new DeviceSummaryDto();
        dto.setDeviceId(deviceId);
        dto.setTrusted(device.isTrusted());
        dto.setTrustScore(trustService.getTrustScore(deviceId));
        dto.setQuarantined(device.isQuarantined());

        // Populate basic info from telemetry or defaults
        optMsg.ifPresentOrElse(msg -> {
            dto.setLastSeen(msg.getTimestamp());
            dto.setLocation(msg.getLocation());
            dto.setIpAddress(msg.getIpAddress());
        }, () -> {
            dto.setLastSeen(null);
            dto.setLocation("Unknown");
            dto.setIpAddress("Unknown");
        });

        // **FIX 1: Add firmware information**
        try {
            FirmwareLogDto latestFirmware = firmwareService.getLatestLogDto(deviceId);
            dto.setFirmwareVersion(latestFirmware.getReportedFirmwareVersion());
            dto.setFirmwareCompliant(latestFirmware.isFirmwareValid());
        } catch (Exception e) {
            dto.setFirmwareVersion("Unknown");
            dto.setFirmwareCompliant(false);
        }

        // **FIX 2: Add compliance information**
        try {
            Optional<ComplianceLog> latestCompliance = complianceRepo
                    .findTopByDeviceIdOrderByTimestampDesc(deviceId);
            dto.setCompliant(latestCompliance.map(ComplianceLog::isCompliant).orElse(false));
        } catch (Exception e) {
            dto.setCompliant(false);
        }

        // **FIX 3: Add recent anomalies count (last 24 hours)**
        try {
            Instant cutoff = Instant.now().minus(24, ChronoUnit.HOURS);
            List<AnomalyLog> recentAnomalies = anomalyRepo.findByDeviceIdAndTimestampAfter(deviceId, cutoff);
            long anomalyCount = recentAnomalies.stream()
                    .mapToLong(log -> log.isAnomalyDetected() ? 1L : 0L)
                    .sum();
            dto.setRecentAnomalies((int) anomalyCount);
        } catch (Exception e) {
            dto.setRecentAnomalies(0);
        }

        // **FIX 4: Add trust factors**
        try {
            Map<String, String> trustFactors = buildTrustFactors(deviceId);
            dto.setTrustFactors(trustFactors);
        } catch (Exception e) {
            dto.setTrustFactors(new HashMap<>());
        }

        return dto;
    }

    /**
     * Build trust factors map based on recent device activity
     */
    private Map<String, String> buildTrustFactors(String deviceId) {
        Map<String, String> factors = new HashMap<>();

        try {
            // Get recent data (last 24 hours)
            Instant cutoff = Instant.now().minus(24, ChronoUnit.HOURS);

            // Identity factor - based on recent identity logs
            // Since you don't seem to have IdentityLogRepository in your provided code,
            // I'll use the device registry trusted status
            DeviceRegistry device = registryService.findById(deviceId).orElse(null);
            if (device != null) {
                factors.put("identity", device.isTrusted() ? "LOW_RISK" : "HIGH_RISK");
            } else {
                factors.put("identity", "NO_DATA");
            }

            // Firmware factor
            try {
                FirmwareLogDto firmware = firmwareService.getLatestLogDto(deviceId);
                factors.put("firmware", firmware.isFirmwareValid() ? "LOW_RISK" : "HIGH_RISK");
            } catch (Exception e) {
                factors.put("firmware", "NO_DATA");
            }

            // Compliance factor
            try {
                Optional<ComplianceLog> compliance = complianceRepo
                        .findTopByDeviceIdOrderByTimestampDesc(deviceId);
                if (compliance.isPresent()) {
                    factors.put("compliance", compliance.get().isCompliant() ? "LOW_RISK" : "HIGH_RISK");
                } else {
                    factors.put("compliance", "NO_DATA");
                }
            } catch (Exception e) {
                factors.put("compliance", "NO_DATA");
            }

            // Behavioral factor (anomalies)
            try {
                List<AnomalyLog> recentAnomalies = anomalyRepo.findByDeviceIdAndTimestampAfter(deviceId, cutoff);
                long anomalyCount = recentAnomalies.stream()
                        .mapToLong(log -> log.isAnomalyDetected() ? 1L : 0L)
                        .sum();

                if (anomalyCount == 0) {
                    factors.put("behavior", "LOW_RISK");
                } else if (anomalyCount <= 2) {
                    factors.put("behavior", "MEDIUM_RISK");
                } else {
                    factors.put("behavior", "HIGH_RISK");
                }
            } catch (Exception e) {
                factors.put("behavior", "NO_DATA");
            }

            // Location factor - based on location changes
            try {
                // Check if device has moved locations recently
                List<DeviceMessage> recentMessages = messageRepo
                        .findByDeviceIdAndTimestampAfterOrderByTimestampDesc(deviceId, cutoff);

                if (recentMessages.size() > 1) {
                    String currentLocation = recentMessages.get(0).getLocation();
                    boolean locationChanged = recentMessages.stream()
                            .anyMatch(msg -> !currentLocation.equals(msg.getLocation()));
                    factors.put("location", locationChanged ? "MEDIUM_RISK" : "LOW_RISK");
                } else {
                    factors.put("location", "LOW_RISK");
                }
            } catch (Exception e) {
                factors.put("location", "NO_DATA");
            }

        } catch (Exception e) {
            // If all else fails, return empty factors
            factors.put("identity", "NO_DATA");
            factors.put("firmware", "NO_DATA");
            factors.put("compliance", "NO_DATA");
            factors.put("behavior", "NO_DATA");
            factors.put("location", "NO_DATA");
        }

        return factors;
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<DeviceRegistry> getDevice(@PathVariable String deviceId) {
        return registryService.findById(deviceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{deviceId}/quarantine")
    public ResponseEntity<Void> quarantineDevice(@PathVariable String deviceId,
            @RequestParam String reason) {
        quarantineService.quarantineDevice(deviceId, reason);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{deviceId}/trust-score")
    public ResponseEntity<Double> getTrustScore(@PathVariable String deviceId) {
        return ResponseEntity.ok(trustService.getTrustScore(deviceId));
    }

    @GetMapping("/{deviceId}/trust-breakdown")
    public ResponseEntity<?> getTrustBreakdown(@PathVariable String deviceId) {
        return ResponseEntity.ok(trustService.getTrustScoreBreakdown(deviceId));
    }

    @PostMapping("/{deviceId}/reset-trust-score")
    public ResponseEntity<?> resetTrustScore(@PathVariable String deviceId,
            @RequestParam(defaultValue = "50.0") double baselineScore) {
        trustService.resetTrustScore(deviceId, baselineScore);
        return ResponseEntity.ok().build();
    }
}

// Enhanced Analytics Controller
@RestController
@RequestMapping("/api/analytics")
class AnalyticsController {

    private final TrustAnalysisService trustAnalysisService;
    private final TrustScoreHistoryService trustScoreHistoryService;

    public AnalyticsController(TrustAnalysisService trustAnalysisService,
            TrustScoreHistoryService trustScoreHistoryService) {
        this.trustAnalysisService = trustAnalysisService;
        this.trustScoreHistoryService = trustScoreHistoryService;
    }

    @GetMapping("/trust-analysis/{deviceId}")
    public ResponseEntity<?> getTrustAnalysis(@PathVariable String deviceId) {
        return ResponseEntity.ok(trustAnalysisService.getTrustAnalysis(deviceId));
    }

    @GetMapping("/trust-timeline/{deviceId}")
    public ResponseEntity<?> getTrustTimeline(@PathVariable String deviceId,
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(trustScoreHistoryService.getTrustScoreTimeline(deviceId, days));
    }
}