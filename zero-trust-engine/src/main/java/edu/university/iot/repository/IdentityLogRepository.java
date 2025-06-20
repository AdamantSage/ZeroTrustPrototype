package edu.university.iot.repository;

import edu.university.iot.model.IdentityLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdentityLogRepository extends JpaRepository<IdentityLog, Long> {
}
