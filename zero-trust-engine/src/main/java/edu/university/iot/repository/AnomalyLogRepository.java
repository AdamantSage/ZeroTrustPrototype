package edu.university.iot.repository;

import edu.university.iot.model.AnomalyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AnomalyLogRepository extends JpaRepository<AnomalyLog, Long> {
    List<AnomalyLog> findByDeviceId(String deviceId);
    
    // Add this method for recent anomalies
    List<AnomalyLog> findByDeviceIdAndTimestampAfter(String deviceId, Instant timestamp);
    
    // For getting anomalies ordered by timestamp
    List<AnomalyLog> findByDeviceIdOrderByTimestampDesc(String deviceId);
    
    // Count recent anomalies
    @Query("SELECT COUNT(a) FROM AnomalyLog a WHERE a.deviceId = :deviceId AND a.timestamp > :timestamp AND a.anomalyDetected = true")
    long countRecentAnomaliesByDevice(@Param("deviceId") String deviceId, @Param("timestamp") Instant timestamp);
}