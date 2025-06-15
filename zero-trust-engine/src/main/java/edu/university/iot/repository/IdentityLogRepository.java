package edu.university.iot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import edu.university.iot.model.IdentityLog;

public interface IdentityLogRepository extends JpaRepository<IdentityLog, Long> {}
