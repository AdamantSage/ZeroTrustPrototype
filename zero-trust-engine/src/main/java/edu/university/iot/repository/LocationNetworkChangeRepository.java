// src/main/java/edu/university/iot/repository/LocationNetworkChangeRepository.java
package edu.university.iot.repository;

import edu.university.iot.model.LocationNetworkChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LocationNetworkChangeRepository 
    extends JpaRepository<LocationNetworkChange, Long> {
  List<LocationNetworkChange> findAllByOrderByTimestampDesc();
  List<LocationNetworkChange> findByDeviceIdOrderByTimestampDesc(String deviceId);
  @Query("SELECT COUNT(DISTINCT l.deviceId) FROM LocationNetworkChange l")
  long countDistinctDeviceIds();
}
