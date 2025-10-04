package edu.university.iot.controllers;

import edu.university.iot.service.HealthyTelemetryProcessorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for healthy behavior monitoring and statistics
 */
@RestController
@RequestMapping("/api/healthy-behavior")
@CrossOrigin(origins = "*")
public class HealthyBehaviorController {

    private final HealthyTelemetryProcessorService healthyTelemetryService;

    public HealthyBehaviorController(HealthyTelemetryProcessorService healthyTelemetryService) {
        this.healthyTelemetryService = healthyTelemetryService;
    }

    /**
     * GET /api/healthy-behavior/device/{deviceId}
     * Get healthy behavior statistics for a specific device
     */
    @GetMapping("/device/{deviceId}")
    public ResponseEntity<Map<String, Object>> getDeviceHealthyBehaviorStats(
            @PathVariable String deviceId) {
        try {
            Map<String, Object> stats = healthyTelemetryService.getHealthyBehaviorStatistics(deviceId);

            if (stats.containsKey("error")) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/healthy-behavior/system
     * Get system-wide healthy behavior metrics
     */
    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> getSystemHealthyBehaviorMetrics() {
        try {
            Map<String, Object> metrics = healthyTelemetryService.getSystemHealthyBehaviorMetrics();

            if (metrics.containsKey("error")) {
                return ResponseEntity.internalServerError().build();
            }

            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/healthy-behavior/summary
     * Get summary of healthy behavior across all devices
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getHealthyBehaviorSummary() {
        try {
            Map<String, Object> metrics = healthyTelemetryService.getSystemHealthyBehaviorMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}