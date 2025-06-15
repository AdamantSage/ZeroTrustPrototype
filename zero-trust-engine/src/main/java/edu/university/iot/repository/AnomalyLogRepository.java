package edu.university.iot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import edu.university.iot.model.AnomalyLog;

public interface AnomalyLogRepository extends JpaRepository<AnomalyLog, Long> {}
