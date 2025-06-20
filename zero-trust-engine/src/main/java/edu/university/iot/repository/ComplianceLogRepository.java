// src/main/java/edu/university/iot/repository/ComplianceLogRepository.java
package edu.university.iot.repository;

import edu.university.iot.model.ComplianceLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplianceLogRepository extends JpaRepository<ComplianceLog, Long> {
}
