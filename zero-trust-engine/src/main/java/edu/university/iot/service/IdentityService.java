package edu.university.iot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.university.iot.entity.DeviceRegistry;
import edu.university.iot.repository.DeviceRegistryRepository;

@Service
public class IdentityService {

    @Autowired
    private DeviceRegistryRepository repo;

    /** returns true if deviceId exists & is registered */
    public boolean isTrusted(String deviceId) {
        return repo.findById(deviceId)
                   .map(device -> device.isRegistered())  // Changed this line
                   .orElse(false);
    }
}