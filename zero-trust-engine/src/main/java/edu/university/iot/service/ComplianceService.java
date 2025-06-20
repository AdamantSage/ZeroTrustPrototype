// src/main/java/edu/university/iot/service/ComplianceService.java
package edu.university.iot.service;

import edu.university.iot.model.ComplianceLog;
import edu.university.iot.entity.DeviceRegistry;
import edu.university.iot.repository.ComplianceLogRepository;
import edu.university.iot.repository.DeviceRegistryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class ComplianceService {

    @Autowired
    private DeviceRegistryRepository deviceRepo;

    @Autowired
    private ComplianceLogRepository complianceRepo;

    public void evaluateCompliance(Map<String, Object> telemetry) {
        String deviceId = (String) telemetry.get("deviceId");
        String patchStatus = (String) telemetry.get("patchStatus");
        String firmwareVersion = (String) telemetry.get("firmwareVersion");

        DeviceRegistry device = deviceRepo.findById(deviceId).orElse(null);

        boolean compliant = true;
        String violations = "";

        if (device != null) {
            if (!device.isAllowOutdatedPatch() && !"Up-to-date".equalsIgnoreCase(patchStatus)) {
                compliant = false;
                violations += "Patch outdated; ";
            }

            if (!firmwareVersion.equals(device.getExpectedFirmwareVersion())) {
                compliant = false;
                violations += "Firmware mismatch; ";
            }
        }

        ComplianceLog log = new ComplianceLog();
        log.setDeviceId(deviceId);
        log.setCompliant(compliant);
        log.setViolations(violations.trim());
        log.setTimestamp(Instant.now());

        complianceRepo.save(log);
    }
}
