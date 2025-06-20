// src/main/java/edu/university/iot/repository/DeviceMessageRepository.java
package edu.university.iot.repository;

import edu.university.iot.entity.DeviceMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceMessageRepository extends JpaRepository<DeviceMessage, Long> {
}
