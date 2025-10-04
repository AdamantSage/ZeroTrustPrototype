package edu.university.iot.controllers;

import edu.university.iot.model.LocationNetworkChange;
import edu.university.iot.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/location")
public class LocationChangeController {

    private final LocationService contextService;

    public LocationChangeController(LocationService contextService) {
        this.contextService = contextService;
    }

    @GetMapping("/changes/{deviceId}")
    public ResponseEntity<List<LocationNetworkChange>> getChanges(@PathVariable String deviceId) {
        return ResponseEntity.ok(contextService.getChanges(deviceId));
    }
}