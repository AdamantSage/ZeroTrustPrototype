package edu.university.iot.controllers;

import edu.university.iot.model.FirmwareLog;
import edu.university.iot.service.FirmwareService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/firmware")
public class FirmwareController {

    private final FirmwareService firmwareService;

    public FirmwareController(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
    }

    @PostMapping("/validate")
    public ResponseEntity<Boolean> validate(@RequestBody Map<String, Object> telemetry) {
        return ResponseEntity.ok(firmwareService.validateAndLogFirmware(telemetry));
    }

    @GetMapping("/logs/{deviceId}")
    public ResponseEntity<List<FirmwareLog>> getLogs(@PathVariable String deviceId) {
        return ResponseEntity.ok(firmwareService.getLogs(deviceId));
    }
}