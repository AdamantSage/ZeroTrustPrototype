// Package: src/main/java/edu/university/iot/controller

package edu.university.iot.controllers;

import edu.university.iot.entity.DeviceRegistry;
import edu.university.iot.model.IdentityLog;
import edu.university.iot.model.FirmwareLog;
import edu.university.iot.model.AnomalyLog;
import edu.university.iot.model.ComplianceLog;
import edu.university.iot.model.LocationNetworkChange;
import edu.university.iot.model.DeviceSession;
import edu.university.iot.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceRegistryService deviceRegistryService;
    private final IdentityVerificationService identityService;
    private final FirmwareService firmwareService;
    private final AnomalyDetectorService anomalyService;
    private final ComplianceService complianceService;
    private final LocationNetworkChangeService contextService;
    private final SessionManagementService sessionService;
    private final TrustScoreService trustService;
    private final QuarantineService quarantineService;

    public DeviceController(
            DeviceRegistryService deviceRegistryService,
            IdentityVerificationService identityService,
            FirmwareService firmwareService,
            AnomalyDetectorService anomalyService,
            ComplianceService complianceService,
            LocationNetworkChangeService contextService,
            SessionManagementService sessionService,
            TrustScoreService trustService,
            QuarantineService quarantineService) {
        this.deviceRegistryService = deviceRegistryService;
        this.identityService = identityService;
        this.firmwareService = firmwareService;
        this.anomalyService = anomalyService;
        this.complianceService = complianceService;
        this.contextService = contextService;
        this.sessionService = sessionService;
        this.trustService = trustService;
        this.quarantineService = quarantineService;
    }

    // List all registered devices
    @GetMapping
    public ResponseEntity<List<DeviceRegistry>> listDevices() {
        return ResponseEntity.ok(deviceRegistryService.findAll());
    }

    // Get a single device
    @GetMapping("/{deviceId}")
    public ResponseEntity<DeviceRegistry> getDevice(@PathVariable String deviceId) {
        return deviceRegistryService.findById(deviceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Force quarantine of a device
    @PostMapping("/{deviceId}/quarantine")
    public ResponseEntity<Void> quarantineDevice(
            @PathVariable String deviceId,
            @RequestParam String reason) {
        quarantineService.quarantineDevice(deviceId, reason);
        return ResponseEntity.ok().build();
    }

    // Get trust score
    @GetMapping("/{deviceId}/trust-score")
    public ResponseEntity<Double> getTrustScore(@PathVariable String deviceId) {
        double score = trustService.getTrustScore(deviceId);
        return ResponseEntity.ok(score);
    }

    // Get session info
    @GetMapping("/{deviceId}/session")
    public ResponseEntity<DeviceSession> getSession(@PathVariable String deviceId) {
        return sessionService.getSessionByDevice(deviceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    // Expose logs endpoints
    @GetMapping("/{deviceId}/identity-logs")
    public ResponseEntity<List<IdentityLog>> getIdentityLogs(@PathVariable String deviceId) {
        return ResponseEntity.ok(identityService.getLogs(deviceId));
    }

    @GetMapping("/{deviceId}/firmware-logs")
    public ResponseEntity<List<FirmwareLog>> getFirmwareLogs(@PathVariable String deviceId) {
        return ResponseEntity.ok(firmwareService.getLogs(deviceId));
    }

    @GetMapping("/{deviceId}/anomaly-logs")
    public ResponseEntity<List<AnomalyLog>> getAnomalyLogs(@PathVariable String deviceId) {
        return ResponseEntity.ok(anomalyService.getLogs(deviceId));
    }

    @GetMapping("/{deviceId}/compliance-logs")
    public ResponseEntity<List<ComplianceLog>> getComplianceLogs(@PathVariable String deviceId) {
        return ResponseEntity.ok(complianceService.getLogs(deviceId));
    }

    @GetMapping("/{deviceId}/context-logs")
    public ResponseEntity<List<LocationNetworkChange>> getContextLogs(@PathVariable String deviceId) {
        return ResponseEntity.ok(contextService.getChanges(deviceId));
    }
}

// You may implement DeviceRegistryService to delegate to repository, and similar getLogs methods in each service.
