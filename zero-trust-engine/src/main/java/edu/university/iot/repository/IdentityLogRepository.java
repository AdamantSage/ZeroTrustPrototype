package edu.university.iot.repository;

import edu.university.iot.model.IdentityLog;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IdentityLogRepository extends JpaRepository<IdentityLog, Long> {

        List<IdentityLog> findByDeviceId(String deviceId);

}
