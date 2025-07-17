// src/main/java/edu/university/iot/repository/LocationNetworkChangeRepository.java
package edu.university.iot.repository;

import edu.university.iot.model.LocationNetworkChange;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LocationNetworkChangeRepository extends JpaRepository<LocationNetworkChange, Long> {
    List<LocationNetworkChange> findByDeviceId(String deviceId);
}
