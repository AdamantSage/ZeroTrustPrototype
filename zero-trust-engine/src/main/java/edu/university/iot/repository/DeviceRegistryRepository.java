package edu.university.iot.repository;

import edu.university.iot.entity.DeviceRegistry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRegistryRepository extends JpaRepository<DeviceRegistry, String> { }
