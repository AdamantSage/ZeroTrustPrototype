// src/main/java/edu/university/iot/repository/QuarantineLogRepository.java
package edu.university.iot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.university.iot.model.QuarantineLog;

public interface QuarantineLogRepository extends JpaRepository<QuarantineLog, Long> {}
