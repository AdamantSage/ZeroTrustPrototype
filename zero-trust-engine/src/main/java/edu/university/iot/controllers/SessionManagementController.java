package edu.university.iot.controllers;

import edu.university.iot.model.DeviceSession;
import edu.university.iot.service.SessionManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/sessions")
public class SessionManagementController {

    private final SessionManagementService sessionService;

    public SessionManagementController(SessionManagementService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<DeviceSession> getSession(@PathVariable String deviceId) {
        Optional<DeviceSession> session = sessionService.getSessionByDevice(deviceId);
        return session.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.noContent().build());
    }
}
