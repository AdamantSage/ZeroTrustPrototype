package edu.university.iot.service;

import edu.university.iot.entity.DeviceRegistry;
import edu.university.iot.repository.DeviceRegistryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeviceRegistryService {

    private final DeviceRegistryRepository registryRepo;

    public DeviceRegistryService(DeviceRegistryRepository registryRepo) {
        this.registryRepo = registryRepo;
    }

    public List<DeviceRegistry> findAll() {
        return registryRepo.findAll();
    }

    public Optional<DeviceRegistry> findById(String deviceId) {
        return registryRepo.findById(deviceId);
    }

    public DeviceRegistry save(DeviceRegistry device) {
        return registryRepo.save(device);
    }
}