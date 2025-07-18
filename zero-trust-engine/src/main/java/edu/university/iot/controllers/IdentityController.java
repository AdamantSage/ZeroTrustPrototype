package edu.university.iot.controllers;

import edu.university.iot.model.IdentityLog;
import edu.university.iot.service.IdentityVerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/identity")
public class IdentityController {

    private final IdentityVerificationService identityService;

    public IdentityController(IdentityVerificationService identityService) {
        this.identityService = identityService;
    }

    @PostMapping("/verify")
    public ResponseEntity<Boolean> verify(@RequestBody Map<String, Object> telemetry) {
        return ResponseEntity.ok(identityService.verifyIdentity(telemetry));
    }

    @GetMapping("/logs/{deviceId}")
    public ResponseEntity<List<IdentityLog>> getLogs(@PathVariable String deviceId) {
        return ResponseEntity.ok(identityService.getLogs(deviceId));
    }
}
