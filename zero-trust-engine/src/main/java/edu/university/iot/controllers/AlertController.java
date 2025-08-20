package edu.university.iot.controllers;

import edu.university.iot.model.dtoModel.AlertDto;
import edu.university.iot.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/system")
    public ResponseEntity<List<AlertDto>> getSystemAlerts() {
        try {
            List<AlertDto> alerts = alertService.generateSystemAlerts();
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            System.err.println("Error getting system alerts: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/device/{deviceId}")
    public ResponseEntity<List<AlertDto>> getDeviceAlerts(@PathVariable String deviceId) {
        try {
            List<AlertDto> alerts = alertService.generateDeviceAlerts(deviceId);
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            System.err.println("Error getting device alerts: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getAlertsSummary() {
        try {
            Map<String, Object> summary = alertService.getAlertsSummary();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            System.err.println("Error getting alerts summary: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "healthy");
        health.put("service", "AlertService");
        health.put("timestamp", java.time.Instant.now().toString());
        return ResponseEntity.ok(health);
    }
}