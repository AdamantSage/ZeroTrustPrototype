// src/main/java/edu/university/iot/service/ComplianceService.java
package edu.university.iot.service;

import edu.university.iot.entity.DeviceRegistry;
import edu.university.iot.model.ComplianceLog;
import edu.university.iot.repository.ComplianceLogRepository;
import edu.university.iot.repository.DeviceRegistryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class ComplianceService {

    @Autowired
    private DeviceRegistryRepository deviceRepo;

    @Autowired
    private ComplianceLogRepository complianceRepo;

    /**
     * Evaluates compliance rules, logs the result,
     * and returns true if the device is compliant.
     */
    public boolean evaluateCompliance(Map<String, Object> telemetry) {
        String deviceId = (String) telemetry.get("deviceId");
        String patchStatus = (String) telemetry.get("patchStatus");
        String firmwareVersion = (String) telemetry.get("firmwareVersion");

        DeviceRegistry device = deviceRepo.findById(deviceId).orElse(null);

        boolean compliant = true;
        StringBuilder violations = new StringBuilder();

        if (device != null) {
            if (!device.isAllowOutdatedPatch() && !"Up-to-date".equalsIgnoreCase(patchStatus)) {
                compliant = false;
                violations.append("Patch outdated; ");
            }
            if (!firmwareVersion.equals(device.getExpectedFirmwareVersion())) {
                compliant = false;
                violations.append("Firmware mismatch; ");
            }
        }

        ComplianceLog log = new ComplianceLog();
        log.setDeviceId(deviceId);
        log.setCompliant(compliant);
        log.setViolations(violations.toString().trim());
        log.setTimestamp(Instant.now());

        complianceRepo.save(log);
        return compliant;
    }

    public List<ComplianceLog> getLogs(String deviceId) {
    return complianceRepo.findByDeviceId(deviceId);
}

}
