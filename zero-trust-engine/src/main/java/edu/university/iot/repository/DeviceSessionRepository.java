package edu.university.iot.repository;

import edu.university.iot.model.DeviceSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceSessionRepository extends JpaRepository<DeviceSession, Long> {
    Optional<DeviceSession> findByDeviceId(String deviceId);
    Optional<DeviceSession> findBySessionId(String sessionId);
}