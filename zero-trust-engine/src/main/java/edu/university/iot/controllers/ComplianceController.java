package edu.university.iot.controllers;

import edu.university.iot.model.ComplianceLog;
import edu.university.iot.service.ComplianceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/compliance")
public class ComplianceController {

    private final ComplianceService complianceService;

    public ComplianceController(ComplianceService complianceService) {
        this.complianceService = complianceService;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<Boolean> evaluate(@RequestBody Map<String, Object> telemetry) {
        return ResponseEntity.ok(complianceService.evaluateCompliance(telemetry));
    }

    @GetMapping("/logs/{deviceId}")
    public ResponseEntity<List<ComplianceLog>> getLogs(@PathVariable String deviceId) {
        return ResponseEntity.ok(complianceService.getLogs(deviceId));
    }
}