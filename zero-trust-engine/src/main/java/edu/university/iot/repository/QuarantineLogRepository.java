// src/main/java/edu/university/iot/repository/QuarantineLogRepository.java
package edu.university.iot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.university.iot.model.QuarantineLog;

public interface QuarantineLogRepository extends JpaRepository<QuarantineLog, Long> {
    List<QuarantineLog> findAllByOrderByTimestampDesc();

    List<QuarantineLog> findByDeviceIdOrderByTimestampDesc(String deviceId);

    /**
     * Count total number of quarantine logs for a specific device
     */
    @Query("SELECT COUNT(q) FROM QuarantineLog q WHERE q.deviceId = :deviceId")
    long countByDeviceId(@Param("deviceId") String deviceId);

    /**
     * Count distinct device IDs that have quarantine logs
     */
    @Query("SELECT COUNT(DISTINCT q.deviceId) FROM QuarantineLog q")
    long countDistinctDeviceIds();

}
