package edu.university.iot.service;

import edu.university.iot.model.dtoModel.AuditSummaryDto;
import edu.university.iot.repository.QuarantineLogRepository;
import edu.university.iot.service.LocationNetworkChangeService;
import edu.university.iot.service.FirmwareService;
import org.springframework.stereotype.Service;

@Service
public class AuditSummaryService {

    private final QuarantineLogRepository quarantineLogRepository;
    private final LocationNetworkChangeService locationNetworkChangeService;
    private final FirmwareService firmwareService;

    public AuditSummaryService(
            QuarantineLogRepository quarantineLogRepository,
            LocationNetworkChangeService locationNetworkChangeService,
            FirmwareService firmwareService) {
        this.quarantineLogRepository = quarantineLogRepository;
        this.locationNetworkChangeService = locationNetworkChangeService;
        this.firmwareService = firmwareService;
    }

    /**
     * Generate comprehensive audit summary with data from all relevant sources
     */
    public AuditSummaryDto generateAuditSummary() {
        AuditSummaryDto summary = new AuditSummaryDto();

        try {
            // Get total quarantine actions
            long totalQuarantines = quarantineLogRepository.count();
            summary.setTotalQuarantineActions(totalQuarantines);

            // Get total location/network changes
            long totalLocationChanges = locationNetworkChangeService.getAllChanges().size();
            summary.setTotalLocationChanges(totalLocationChanges);

            // Get total firmware checks/logs
            long totalFirmwareChecks = firmwareService.getAllLogsDto().size();
            summary.setTotalFirmwareChecks(totalFirmwareChecks);

            // Calculate devices with issues (devices that have quarantine logs or location changes)
            long devicesWithIssues = calculateDevicesWithIssues();
            summary.setDevicesWithIssues(devicesWithIssues);

        } catch (Exception e) {
            // Log the error and return summary with zero values
            System.err.println("Error generating audit summary: " + e.getMessage());
            e.printStackTrace();
        }

        return summary;
    }

    /**
     * Calculate the number of unique devices that have had issues
     * (devices with quarantine logs or location changes)
     */
    private long calculateDevicesWithIssues() {
        try {
            // Get unique device IDs from quarantine logs
            long devicesWithQuarantines = quarantineLogRepository.countDistinctDeviceIds();
            
            // Get unique device IDs from location changes
            long devicesWithLocationChanges = locationNetworkChangeService.getDeviceCountWithChanges();
            
            // Note: This is a simple addition - in a real scenario, you might want to
            // use a more sophisticated approach to avoid double-counting devices
            // that appear in both categories
            return Math.max(devicesWithQuarantines, devicesWithLocationChanges);
            
        } catch (Exception e) {
            System.err.println("Error calculating devices with issues: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Generate audit summary for a specific device
     */
    public AuditSummaryDto generateDeviceAuditSummary(String deviceId) {
        AuditSummaryDto summary = new AuditSummaryDto();

        try {
            // Get quarantine actions for specific device
            long deviceQuarantines = quarantineLogRepository.countByDeviceId(deviceId);
            summary.setTotalQuarantineActions(deviceQuarantines);

            // Get location changes for specific device
            long deviceLocationChanges = locationNetworkChangeService.getChanges(deviceId).size();
            summary.setTotalLocationChanges(deviceLocationChanges);

            // Get firmware checks for specific device
            long deviceFirmwareChecks = firmwareService.getLogsDto(deviceId).size();
            summary.setTotalFirmwareChecks(deviceFirmwareChecks);

            // For a specific device, devicesWithIssues is either 0 or 1
            long hasIssues = (deviceQuarantines > 0 || deviceLocationChanges > 0) ? 1 : 0;
            summary.setDevicesWithIssues(hasIssues);

        } catch (Exception e) {
            System.err.println("Error generating device audit summary for device " + deviceId + ": " + e.getMessage());
            e.printStackTrace();
        }

        return summary;
    }
}