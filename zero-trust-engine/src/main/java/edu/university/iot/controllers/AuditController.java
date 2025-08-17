package edu.university.iot.controllers;

import edu.university.iot.model.LocationNetworkChange;
import edu.university.iot.model.QuarantineLog;
import edu.university.iot.model.dtoModel.AuditSummaryDto;
import edu.university.iot.model.dtoModel.FirmwareLogDto;
import edu.university.iot.service.LocationService;
import edu.university.iot.service.FirmwareService;
import edu.university.iot.service.AuditSummaryService;
import edu.university.iot.repository.QuarantineLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final LocationService locationService;
    private final QuarantineLogRepository quarantineLogRepo;
    private final FirmwareService firmwareService;
    private final AuditSummaryService auditSummaryService;

    public AuditController(
            LocationService locationService,
            QuarantineLogRepository quarantineLogRepo,
            FirmwareService firmwareService,
            AuditSummaryService auditSummaryService) {
        this.locationService = locationService;
        this.quarantineLogRepo = quarantineLogRepo;
        this.firmwareService = firmwareService;
        this.auditSummaryService = auditSummaryService;
    }

    /**
     * Get location/network changes for all devices or specific device
     */
    @GetMapping("/location-changes")
    public ResponseEntity<List<LocationNetworkChange>> getLocationChanges(
            @RequestParam(required = false) String deviceId) {
        
        try {
            if (deviceId != null && !deviceId.isEmpty()) {
                // Get changes for specific device
                List<LocationNetworkChange> changes = locationService.getChanges(deviceId);
                return ResponseEntity.ok(changes);
            } else {
                // Get all changes
                List<LocationNetworkChange> changes = locationService.getAllChanges();
                return ResponseEntity.ok(changes);
            }
        } catch (Exception e) {
            System.err.println("Error fetching location changes: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get quarantine history for all devices or specific device
     */
    @GetMapping("/quarantine-history")
    public ResponseEntity<List<QuarantineLog>> getQuarantineHistory(
            @RequestParam(required = false) String deviceId) {
        
        try {
            if (deviceId != null && !deviceId.isEmpty()) {
                // Get quarantine history for specific device
                List<QuarantineLog> logs = quarantineLogRepo.findByDeviceIdOrderByTimestampDesc(deviceId);
                return ResponseEntity.ok(logs);
            } else {
                // Get all quarantine history
                List<QuarantineLog> logs = quarantineLogRepo.findAllByOrderByTimestampDesc();
                return ResponseEntity.ok(logs);
            }
        } catch (Exception e) {
            System.err.println("Error fetching quarantine history: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get firmware logs for all devices or specific device
     */
    @GetMapping("/firmware-logs")
    public ResponseEntity<List<FirmwareLogDto>> getFirmwareLogs(
            @RequestParam(required = false) String deviceId) {
        
        try {
            if (deviceId != null && !deviceId.isEmpty()) {
                // Get firmware logs for specific device
                List<FirmwareLogDto> logs = firmwareService.getLogsDto(deviceId);
                return ResponseEntity.ok(logs);
            } else {
                // Get all firmware logs
                List<FirmwareLogDto> logs = firmwareService.getAllLogsDto();
                return ResponseEntity.ok(logs);
            }
        } catch (Exception e) {
            System.err.println("Error fetching firmware logs: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get summary statistics for audit dashboard
     */
    @GetMapping("/summary")
    public ResponseEntity<AuditSummaryDto> getAuditSummary(
            @RequestParam(required = false) String deviceId) {
        
        try {
            AuditSummaryDto summary;
            
            if (deviceId != null && !deviceId.isEmpty()) {
                // Get summary for specific device
                summary = auditSummaryService.generateDeviceAuditSummary(deviceId);
            } else {
                // Get overall summary
                summary = auditSummaryService.generateAuditSummary();
            }
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            System.err.println("Error generating audit summary: " + e.getMessage());
            // Return a summary with zero values instead of error
            AuditSummaryDto emptySummary = new AuditSummaryDto();
            return ResponseEntity.ok(emptySummary);
        }
    }
}