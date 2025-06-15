package edu.university.iot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import edu.university.iot.model.FirmwareLog;

public interface FirmwareLogRepository extends JpaRepository<FirmwareLog, Long> {}
