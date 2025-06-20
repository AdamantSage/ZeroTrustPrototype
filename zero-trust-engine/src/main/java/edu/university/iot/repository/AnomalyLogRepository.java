// src/main/java/edu/university/iot/repository/AnomalyLogRepository.java
package edu.university.iot.repository;

import edu.university.iot.model.AnomalyLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnomalyLogRepository extends JpaRepository<AnomalyLog, Long> {
}
