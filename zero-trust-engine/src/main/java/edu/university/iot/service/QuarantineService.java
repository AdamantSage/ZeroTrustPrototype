package edu.university.iot.service;

import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.DeviceStatus;
import edu.university.iot.model.QuarantineLog;
import edu.university.iot.repository.DeviceRegistryRepository;
import edu.university.iot.repository.QuarantineLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;

/**
 * Service for quarantining (disabling) devices in Azure IoT Hub
 * and marking them quarantined at the application level,
 * as well as logging the action locally.
 */
@Service
public class QuarantineService {

    private static final Logger log = LoggerFactory.getLogger(QuarantineService.class);

    private final RegistryManager registryManager;
    private final DeviceRegistryRepository registryRepo;
    private final QuarantineLogRepository logRepo;

    public QuarantineService(
            RegistryManager registryManager,
            DeviceRegistryRepository registryRepo,
            QuarantineLogRepository logRepo) {
        this.registryManager = registryManager;
        this.registryRepo = registryRepo;
        this.logRepo = logRepo;
    }

    /**
     * Quarantines a device by:
     *  1) Disabling its identity in Azure IoT Hub
     *  2) Marking it quarantined in the DeviceRegistry entity
     *  3) Logging the quarantine attempt locally
     *
     * @param deviceId the ID of the device to quarantine
     * @param reason human-readable reason for quarantining
     */
    @Transactional
    public void quarantineDevice(String deviceId, String reason) {
        // 1) Disable in IoT Hub
        try {
            Device device = registryManager.getDevice(deviceId);
            if (device == null) {
                log.warn("Device [{}] not found in IoT Hub. Skipping Azure disable.", deviceId);
            } else {
                device.setStatus(DeviceStatus.Disabled);
                registryManager.updateDevice(device);
                log.info("Device [{}] disabled in IoT Hub. Reason: {}", deviceId, reason);
            }
        } catch (IotHubException | IOException e) {
            log.error("Failed to disable device [{}] in IoT Hub: {}", deviceId, e.getMessage(), e);
        }

        // 2) Mark quarantined in application-level registry
        registryRepo.findById(deviceId).ifPresent(dr -> {
            dr.setQuarantined(true);
            dr.setQuarantineReason(reason);
            dr.setQuarantineTimestamp(Instant.now().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
            registryRepo.save(dr);
            log.info("Device [{}] marked quarantined in registry. Reason: {}", deviceId, reason);
        });

        // 3) Log locally
        QuarantineLog entry = new QuarantineLog();
        entry.setDeviceId(deviceId);
        entry.setReason(reason);
        entry.setTimestamp(Instant.now());
        logRepo.save(entry);
    }
}
