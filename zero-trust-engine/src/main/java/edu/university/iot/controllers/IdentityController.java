package edu.university.iot.controllers;

import edu.university.iot.model.IdentityLog;
import edu.university.iot.service.IdentityVerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/identity")
@CrossOrigin(origins = "*") // Allow cross-origin requests (e.g., from React frontend)
public class IdentityController {

    private final IdentityVerificationService identityService;

    public IdentityController(IdentityVerificationService identityService) {
        this.identityService = identityService;
    }

    /**
     * POST /api/identity/verify
     * Verifies device identity.
     */
    @PostMapping("/verify")
    public ResponseEntity<Boolean> verifyIdentity(@RequestBody Map<String, Object> telemetry) {
        boolean verified = identityService.verifyIdentity(telemetry);
        return ResponseEntity.ok(verified);
    }

    /**
     * GET /api/identity/logs/{deviceId}
     * Returns all identity logs for a specific device.
     */
    @GetMapping("/logs/{deviceId}")
    public ResponseEntity<List<IdentityLog>> getLogs(@PathVariable String deviceId) {
        List<IdentityLog> logs = identityService.getLogs(deviceId);
        return ResponseEntity.ok(logs);
    }
}
