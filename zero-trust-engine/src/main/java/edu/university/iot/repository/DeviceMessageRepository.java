// src/main/java/edu/university/iot/repository/DeviceMessageRepository.java
package edu.university.iot.repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.university.iot.entity.DeviceMessage;

public interface DeviceMessageRepository extends JpaRepository<DeviceMessage, Long> {
    // Your existing methods...
    Optional<DeviceMessage> findTopByDeviceIdOrderByTimestampDesc(String deviceId);
    
    // Add these methods for enhanced functionality
    List<DeviceMessage> findByDeviceIdAndTimestampAfterOrderByTimestampDesc(String deviceId, Instant timestamp);
    
    List<DeviceMessage> findByDeviceIdOrderByTimestampDesc(String deviceId);
    
    // For location change detection
    @Query("SELECT dm FROM DeviceMessage dm WHERE dm.deviceId = :deviceId AND dm.timestamp > :timestamp ORDER BY dm.timestamp DESC")
    List<DeviceMessage> findRecentMessages(@Param("deviceId") String deviceId, @Param("timestamp") Instant timestamp);
}