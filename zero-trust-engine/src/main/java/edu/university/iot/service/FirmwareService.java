package edu.university.iot.service;

import edu.university.iot.entity.DeviceRegistry;
import edu.university.iot.model.FirmwareLog;
import edu.university.iot.repository.DeviceRegistryRepository;
import edu.university.iot.repository.FirmwareLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class FirmwareService {
    private static final Logger logger = LoggerFactory.getLogger(FirmwareService.class);

    private final DeviceRegistryRepository registryRepo;
    private final FirmwareLogRepository firmwareLogRepo;

    public FirmwareService(DeviceRegistryRepository registryRepo,
                           FirmwareLogRepository firmwareLogRepo) {
        this.registryRepo = registryRepo;
        this.firmwareLogRepo = firmwareLogRepo;
    }

    /**
     * Validates firmware against a minimum version and patch policy, then logs the result.
     */
    public void validateAndLogFirmware(Map<String, Object> telemetry) {
        String deviceId = (String) telemetry.get("deviceId");
        String reportedVersion = (String) telemetry.get("firmwareVersion");
        String reportedPatch = (String) telemetry.get("patchStatus");

        Optional<DeviceRegistry> opt = registryRepo.findById(deviceId);
        boolean valid = false;
        if (opt.isPresent()) {
            DeviceRegistry device = opt.get();
            // Semantic version check: reported >= minimum
            boolean versionOk = compareVersions(reportedVersion, device.getExpectedFirmwareVersion()) >= 0;
            // Patch check: either patched or outdated allowed
            boolean patchOk = device.isAllowOutdatedPatch() || reportedPatch.equalsIgnoreCase(device.getExpectedPatchStatus());
            valid = versionOk && patchOk;
        } else {
            logger.warn("Device [{}] not found in registry for firmware validation", deviceId);
        }

        FirmwareLog log = new FirmwareLog(deviceId, reportedVersion, valid, LocalDateTime.now());
        firmwareLogRepo.save(log);
        logger.info("Logged firmware check for device {}: version={}, valid={}", deviceId, reportedVersion, valid);
    }

    /**
     * Compare two semantic version strings: "1.2.3".
     * Returns <0 if v1<v2, 0 if equal, >0 if v1>v2.
     */
    private int compareVersions(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");
        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int p1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int p2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            if (p1 != p2) {
                return p1 - p2;
            }
        }
        return 0;
    }
}
