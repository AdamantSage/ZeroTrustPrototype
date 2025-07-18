package edu.university.iot.controllers;

import edu.university.iot.model.LocationNetworkChange;
import edu.university.iot.service.LocationNetworkChangeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/location")
public class LocationChangeController {

    private final LocationNetworkChangeService contextService;

    public LocationChangeController(LocationNetworkChangeService contextService) {
        this.contextService = contextService;
    }

    @GetMapping("/changes/{deviceId}")
    public ResponseEntity<List<LocationNetworkChange>> getChanges(@PathVariable String deviceId) {
        return ResponseEntity.ok(contextService.getChanges(deviceId));
    }
}