// src/main/java/edu/university/iot/controllers/FirmwareController.java
package edu.university.iot.controllers;

import edu.university.iot.model.dtoModel.FirmwareLogDto;
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
    public ResponseEntity<List<FirmwareLogDto>> getLogs(@PathVariable String deviceId) {
        return ResponseEntity.ok(firmwareService.getLogsDto(deviceId));
    }

    @GetMapping("/summary/{deviceId}")
    public ResponseEntity<FirmwareLogDto> getLatestSummary(@PathVariable String deviceId) {
        return ResponseEntity.ok(firmwareService.getLatestLogDto(deviceId));
    }
}
