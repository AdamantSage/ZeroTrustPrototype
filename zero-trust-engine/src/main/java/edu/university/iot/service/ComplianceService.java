package edu.university.iot.service;

import edu.university.iot.entity.DeviceRegistry;
import edu.university.iot.model.ComplianceLog;
import edu.university.iot.repository.ComplianceLogRepository;
import edu.university.iot.repository.DeviceRegistryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class ComplianceService {

    private static final Logger logger = LoggerFactory.getLogger(ComplianceService.class);

    @Autowired
    private DeviceRegistryRepository deviceRepo;

    @Autowired
    private ComplianceLogRepository complianceRepo;

    /**
     * Evaluates compliance rules, logs the result,
     * and returns true if the device is compliant.
     * Updated to handle null values gracefully.
     */
    public boolean evaluateCompliance(Map<String, Object> telemetry) {
        String deviceId = (String) telemetry.get("deviceId");
        String patchStatus = (String) telemetry.get("patchStatus");
        String firmwareVersion = (String) telemetry.get("firmwareVersion");

        // Handle null values
        if (patchStatus == null) {
            patchStatus = "Unknown";
        }
        if (firmwareVersion == null) {
            firmwareVersion = "Unknown";
        }

        DeviceRegistry device = deviceRepo.findById(deviceId).orElse(null);

        boolean compliant = true;
        StringBuilder violations = new StringBuilder();

        if (device != null) {
            // Check patch status compliance
            if (!device.isAllowOutdatedPatch() && !"Up-to-date".equalsIgnoreCase(patchStatus)) {
                compliant = false;
                violations.append("Patch status: ").append(patchStatus).append("; ");
            }

            // Check firmware version compliance
            String expectedVersion = device.getExpectedFirmwareVersion();
            if (expectedVersion != null && !firmwareVersion.equals(expectedVersion)) {
                compliant = false;
                violations.append("Firmware version mismatch (expected: ")
                        .append(expectedVersion)
                        .append(", actual: ")
                        .append(firmwareVersion)
                        .append("); ");
            }
        } else {
            // Device not found in registry
            compliant = false;
            violations.append("Device not registered; ");
        }

        // Create and save compliance log
        ComplianceLog log = new ComplianceLog();
        log.setDeviceId(deviceId);
        log.setCompliant(compliant);
        log.setViolations(compliant ? "" : violations.toString().trim());
        log.setTimestamp(Instant.now());

        complianceRepo.save(log);

        if (!compliant) {
            logger.warn("Compliance violation for device [{}]: {}", deviceId, log.getViolations());
        } else {
            logger.debug("Device [{}] is compliant", deviceId);
        }

        return compliant;
    }

    public List<ComplianceLog> getLogs(String deviceId) {
        return complianceRepo.findByDeviceId(deviceId);
    }
}