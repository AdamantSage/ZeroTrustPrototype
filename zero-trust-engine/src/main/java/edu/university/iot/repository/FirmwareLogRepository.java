package edu.university.iot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import edu.university.iot.model.FirmwareLog;

public interface FirmwareLogRepository extends JpaRepository<FirmwareLog, Long> {
    // Your existing methods...
    List<FirmwareLog> findByDeviceIdOrderByTimestampDesc(String deviceId);
    Optional<FirmwareLog> findTopByDeviceIdOrderByTimestampDesc(String deviceId);
    
    // Add this method for getting all firmware logs
    List<FirmwareLog> findAllByOrderByTimestampDesc();
}

