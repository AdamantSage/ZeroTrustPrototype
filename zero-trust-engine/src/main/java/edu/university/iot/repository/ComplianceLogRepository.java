package edu.university.iot.repository;

import edu.university.iot.model.ComplianceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplianceLogRepository extends JpaRepository<ComplianceLog, Long> {
    List<ComplianceLog> findByDeviceId(String deviceId);
    
    // Add this method to get the most recent compliance log
    Optional<ComplianceLog> findTopByDeviceIdOrderByTimestampDesc(String deviceId);
    
    // For all compliance logs ordered by timestamp
    List<ComplianceLog> findByDeviceIdOrderByTimestampDesc(String deviceId);
    
    // Get all compliance logs ordered by timestamp (for audit purposes)
    List<ComplianceLog> findAllByOrderByTimestampDesc();
}