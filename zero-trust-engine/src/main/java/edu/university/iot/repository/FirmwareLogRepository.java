package edu.university.iot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import edu.university.iot.model.FirmwareLog;

public interface FirmwareLogRepository extends JpaRepository<FirmwareLog, Long> {

    List<FirmwareLog> findByDeviceId(String deviceId);

}
