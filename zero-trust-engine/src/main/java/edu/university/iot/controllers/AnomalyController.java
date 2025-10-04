package edu.university.iot.controllers;

import edu.university.iot.model.AnomalyLog;
import edu.university.iot.service.AnomalyDetectorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/anomaly")
public class AnomalyController {

    private final AnomalyDetectorService anomalyService;

    public AnomalyController(AnomalyDetectorService anomalyService) {
        this.anomalyService = anomalyService;
    }

    @PostMapping("/check")
    public ResponseEntity<Boolean> check(@RequestBody Map<String, Object> telemetry) {
        return ResponseEntity.ok(anomalyService.checkAnomaly(telemetry));
    }

    @GetMapping("/logs/{deviceId}")
    public ResponseEntity<List<AnomalyLog>> getLogs(@PathVariable String deviceId) {
        return ResponseEntity.ok(anomalyService.getLogs(deviceId));
    }
}