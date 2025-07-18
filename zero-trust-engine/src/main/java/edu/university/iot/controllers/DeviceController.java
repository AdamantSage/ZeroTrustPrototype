// src/main/java/edu/university/iot/controllers/DeviceController.java
package edu.university.iot.controllers;

import edu.university.iot.entity.DeviceRegistry;
import edu.university.iot.model.dtoModel.DeviceSummaryDto;
import edu.university.iot.service.DeviceRegistryService;
import edu.university.iot.service.QuarantineService;
import edu.university.iot.service.TrustScoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceRegistryService registryService;
    private final QuarantineService quarantineService;
    private final TrustScoreService trustService;

    public DeviceController(DeviceRegistryService registryService,
                            QuarantineService quarantineService,
                            TrustScoreService trustService) {
        this.registryService = registryService;
        this.quarantineService = quarantineService;
        this.trustService = trustService;
    }

    // ← Updated to return summary DTO
    @GetMapping
    public ResponseEntity<List<DeviceSummaryDto>> listDevices() {
        List<DeviceSummaryDto> list = registryService.findAll().stream()
            .map(dr -> new DeviceSummaryDto(
                dr.getDeviceId(),
                dr.isTrusted(),
                trustService.getTrustScore(dr.getDeviceId()),
                dr.isQuarantined()
            ))
            .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    // No changes to detail/quarantine/trust-score endpoints
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
}
