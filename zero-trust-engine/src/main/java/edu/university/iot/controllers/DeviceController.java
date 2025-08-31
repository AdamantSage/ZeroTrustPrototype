// src/main/java/edu/university/iot/controllers/DeviceController.java
package edu.university.iot.controllers;

import edu.university.iot.entity.DeviceMessage;
import edu.university.iot.entity.DeviceRegistry;
import edu.university.iot.model.dtoModel.DeviceSummaryDto;
import edu.university.iot.repository.DeviceMessageRepository;
import edu.university.iot.service.DeviceRegistryService;
import edu.university.iot.service.QuarantineService;
import edu.university.iot.service.TrustScoreService;
import edu.university.iot.service.TrustScoreHistoryService;
import edu.university.iot.service.TrustAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceRegistryService    registryService;
    private final QuarantineService        quarantineService;
    private final TrustScoreService        trustService;
    private final DeviceMessageRepository  messageRepo;
    private final TrustScoreHistoryService trustScoreHistoryService;
    private final TrustAnalysisService     trustAnalysisService;

    public DeviceController(DeviceRegistryService registryService,
                            QuarantineService quarantineService,
                            TrustScoreService trustService,
                            DeviceMessageRepository messageRepo,
                            TrustScoreHistoryService trustScoreHistoryService,
                            TrustAnalysisService trustAnalysisService) {
        this.registryService         = registryService;
        this.quarantineService       = quarantineService;
        this.trustService            = trustService;
        this.messageRepo             = messageRepo;
        this.trustScoreHistoryService = trustScoreHistoryService;
        this.trustAnalysisService    = trustAnalysisService;
    }

    @GetMapping
    public ResponseEntity<List<DeviceSummaryDto>> listDevices() {
        List<DeviceSummaryDto> list = registryService.findAll().stream()
            .map(dr -> {
                // Fetch most recent telemetry
                Optional<DeviceMessage> optMsg =
                    messageRepo.findTopByDeviceIdOrderByTimestampDesc(dr.getDeviceId());

                DeviceSummaryDto dto = new DeviceSummaryDto();
                dto.setDeviceId(dr.getDeviceId());
                dto.setTrusted(dr.isTrusted());
                dto.setTrustScore(trustService.getTrustScore(dr.getDeviceId()));
                dto.setQuarantined(dr.isQuarantined());

                // Populate lastSeen, location, ipAddress (or defaults)
                optMsg.ifPresentOrElse(msg -> {
                    dto.setLastSeen(msg.getTimestamp());
                    dto.setLocation(msg.getLocation());
                    dto.setIpAddress(msg.getIpAddress());
                }, () -> {
                    dto.setLastSeen(null);
                    dto.setLocation("Unknown");
                    dto.setIpAddress("Unknown");
                });

                return dto;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(list);
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

    // Add the missing trust analysis endpoints that the frontend is calling
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

// Add missing controller for analytics endpoints
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