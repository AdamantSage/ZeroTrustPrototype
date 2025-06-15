package edu.university.iot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import edu.university.iot.entity.DeviceMessage;

public interface DeviceMessageRepository extends JpaRepository<DeviceMessage, Long> {
}