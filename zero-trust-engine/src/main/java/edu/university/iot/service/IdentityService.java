package edu.university.iot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.university.iot.entity.DeviceRegistry;
import edu.university.iot.repository.DeviceRegistryRepository;

@Service
public class IdentityService {
    private static final Logger logger = LoggerFactory.getLogger(IdentityService.class);

    @Autowired
    private DeviceRegistryRepository repo;

    /**
     * returns true if a registry entry exists AND its status is "ACTIVE"
     */
    public boolean isTrusted(String deviceId) {
        return repo.findByDeviceId(deviceId)
                   .map(reg -> {
                       boolean trusted = "ACTIVE".equalsIgnoreCase(reg.getStatus());
                       logger.info("Device [{}] found. Status = [{}]. Trusted = {}", 
                                   deviceId, reg.getStatus(), trusted);
                       return trusted;
                   })
                   .orElseGet(() -> {
                       logger.warn("Device [{}] not found in registry", deviceId);
                       return false;
                   });
    }
}
