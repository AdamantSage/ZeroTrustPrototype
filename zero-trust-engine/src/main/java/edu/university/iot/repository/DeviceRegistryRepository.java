package edu.university.iot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import edu.university.iot.entity.DeviceRegistry;

import java.util.Optional;
import java.util.List;

@Repository
public interface DeviceRegistryRepository extends JpaRepository<DeviceRegistry, String> {
    /**
     * Check if a device exists by device ID
     */
    boolean existsByDeviceId(String deviceId);

    /**
     * Find device by device ID
     */
    Optional<DeviceRegistry> findByDeviceId(String deviceId);

    /**
     * Alternative custom query method
     */
    @Query("SELECT d FROM DeviceRegistry d WHERE d.deviceId = :deviceId")
    Optional<DeviceRegistry> findDeviceByDeviceId(@Param("deviceId") String deviceId);

    /**
     * Count devices by status
     */
    long countByStatus(String status);

    /**
     * Find all active devices
     */
    @Query("SELECT d FROM DeviceRegistry d WHERE d.status = 'ACTIVE'")
    List<DeviceRegistry> findAllActiveDevices();
}