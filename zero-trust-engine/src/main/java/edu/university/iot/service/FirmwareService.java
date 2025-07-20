// src/main/java/edu/university/iot/service/FirmwareService.java
package edu.university.iot.service;

import edu.university.iot.entity.DeviceRegistry;
import edu.university.iot.model.FirmwareLog;
import edu.university.iot.model.dtoModel.FirmwareLogDto;
import edu.university.iot.repository.DeviceRegistryRepository;
import edu.university.iot.repository.FirmwareLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
     * Validates firmware against minimum version & patch policy,
     * logs the result, and returns true if the device passes.
     */
    public boolean validateAndLogFirmware(Map<String, Object> telemetry) {
        String deviceId = (String) telemetry.get("deviceId");
        String reportedVersion = (String) telemetry.get("firmwareVersion");
        String reportedPatch = (String) telemetry.get("patchStatus");

        Optional<DeviceRegistry> opt = registryRepo.findById(deviceId);
        boolean valid = false;
        if (opt.isPresent()) {
            DeviceRegistry device = opt.get();
            // Semantic version check: reported >= expected
            boolean versionOk = compareVersions(reportedVersion, device.getExpectedFirmwareVersion()) >= 0;
            // Patch check: either patched or outdated allowed
            boolean patchOk = device.isAllowOutdatedPatch()
                    || reportedPatch.equalsIgnoreCase(device.getExpectedPatchStatus());
            valid = versionOk && patchOk;
        } else {
            logger.warn("Device [{}] not found in registry for firmware validation", deviceId);
        }

        // Persist the log
        FirmwareLog log = new FirmwareLog();
        log.setDeviceId(deviceId);
        log.setFirmwareVersion(reportedVersion);
        log.setReportedPatchStatus(reportedPatch);
        log.setFirmwareValid(valid);
        log.setTimestamp(LocalDateTime.now());

        firmwareLogRepo.save(log);

        logger.info("Logged firmware check for device {}: version={}, valid={}", deviceId, reportedVersion, valid);
        return valid;
    }

    /** Compare two semantic version strings “a.b.c” */
    private int compareVersions(String v1, String v2) {
        String[] p1 = v1.split("\\."), p2 = v2.split("\\.");
        int len = Math.max(p1.length, p2.length);
        for (int i = 0; i < len; i++) {
            int x = i < p1.length ? Integer.parseInt(p1[i]) : 0;
            int y = i < p2.length ? Integer.parseInt(p2[i]) : 0;
            if (x != y)
                return x - y;
        }
        return 0;
    }

    public List<FirmwareLogDto> getLogsDto(String deviceId) {
        return firmwareLogRepo.findByDeviceIdOrderByTimestampDesc(deviceId).stream()
                .map(log -> new FirmwareLogDto(
                        log.getDeviceId(),
                        log.getFirmwareVersion(),
                        // pull expected from registry if available, else blank
                        registryRepo.findById(deviceId)
                                .map(DeviceRegistry::getExpectedFirmwareVersion)
                                .orElse("Unknown"),
                        log.getReportedPatchStatus(),
                        log.isFirmwareValid(),
                        log.getTimestamp().atZone(ZoneId.systemDefault()).toInstant()))
                .collect(Collectors.toList());
    }

    public FirmwareLogDto getLatestLogDto(String deviceId) {
        FirmwareLogDto defaultDto = new FirmwareLogDto(
                deviceId, "Unknown", "Unknown", "Unknown", true, Instant.now());
        return firmwareLogRepo.findTopByDeviceIdOrderByTimestampDesc(deviceId)
                .map(log -> new FirmwareLogDto(
                        log.getDeviceId(),
                        log.getFirmwareVersion(),
                        registryRepo.findById(deviceId)
                                .map(DeviceRegistry::getExpectedFirmwareVersion)
                                .orElse("Unknown"),
                        log.getReportedPatchStatus(),
                        log.isFirmwareValid(),
                        log.getTimestamp().atZone(ZoneId.systemDefault()).toInstant()))
                .orElse(defaultDto);
    }
    public List<FirmwareLogDto> getAllLogsDto() {
    return firmwareLogRepo.findAllByOrderByTimestampDesc().stream()
            .map(log -> new FirmwareLogDto(
                    log.getDeviceId(),
                    log.getFirmwareVersion(),
                    // pull expected from registry if available, else blank
                    registryRepo.findById(log.getDeviceId())
                            .map(DeviceRegistry::getExpectedFirmwareVersion)
                            .orElse("Unknown"),
                    log.getReportedPatchStatus(),
                    log.isFirmwareValid(),
                    log.getTimestamp().atZone(ZoneId.systemDefault()).toInstant()))
            .collect(Collectors.toList());
}
}