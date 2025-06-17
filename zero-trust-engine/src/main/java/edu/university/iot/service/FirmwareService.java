package edu.university.iot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.university.iot.entity.DeviceRegistry;
import edu.university.iot.repository.DeviceRegistryRepository;

import java.util.Optional;

@Service
public class FirmwareService {
    private static final Logger logger = LoggerFactory.getLogger(FirmwareService.class);

    @Autowired
    private DeviceRegistryRepository repo;

    /**
     * returns true if reported version matches expected version in registry
     */
    public boolean isValid(String deviceId, String reportedVersion) {
        Optional<DeviceRegistry> opt = repo.findByDeviceId(deviceId);
        if (opt.isEmpty()) {
            logger.warn("Device [{}] not found for firmware validation", deviceId);
            return false;
        }
        String expected = opt.get().getFirmwareVersion();
        boolean match = expected.equals(reportedVersion);
        logger.info("Device [{}]: Expected = [{}], Reported = [{}], Valid = {}", deviceId, expected, reportedVersion, match);
        return match;
    }
}
