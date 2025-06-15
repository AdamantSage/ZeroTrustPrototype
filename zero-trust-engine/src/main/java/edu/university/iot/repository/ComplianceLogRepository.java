package edu.university.iot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import edu.university.iot.model.ComplianceLog;

public interface ComplianceLogRepository extends JpaRepository<ComplianceLog, Long> {}
