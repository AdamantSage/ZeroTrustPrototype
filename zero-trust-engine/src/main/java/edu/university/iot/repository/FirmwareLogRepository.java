package edu.university.iot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import edu.university.iot.model.FirmwareLog;

public interface FirmwareLogRepository extends JpaRepository<FirmwareLog, Long> {
  List<FirmwareLog> findAllByOrderByTimestampDesc();
  List<FirmwareLog> findByDeviceIdOrderByTimestampDesc(String deviceId);
  Optional<FirmwareLog> findTopByDeviceIdOrderByTimestampDesc(String deviceId);
}

