package edu.university.iot.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.university.iot.model.AnomalyLog;
import edu.university.iot.service.AnomalyDetectorService;

@RestController
@RequestMapping("/api/anomaly")
public class AnomalyController {

    private final AnomalyDetectorService anomalyService;

    public AnomalyController(AnomalyDetectorService anomalyService) {
        this.anomalyService = anomalyService;
    }

    @PostMapping("/check")
    public ResponseEntity<Boolean> check(@RequestBody Map<String, Object> telemetry) {
        boolean result = anomalyService.checkAnomaly(telemetry);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/logs/{deviceId}")
    public ResponseEntity<List<AnomalyLog>> getLogs(@PathVariable String deviceId) {
        return ResponseEntity.ok(anomalyService.getLogs(deviceId));
    }
}
