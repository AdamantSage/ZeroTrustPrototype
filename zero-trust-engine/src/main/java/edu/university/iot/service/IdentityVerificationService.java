package edu.university.iot.service;

import edu.university.iot.model.IdentityLog;
import edu.university.iot.repository.DeviceRegistryRepository;
import edu.university.iot.repository.IdentityLogRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class IdentityVerificationService {

    private final DeviceRegistryRepository registryRepo;
    private final IdentityLogRepository logRepo;

    public IdentityVerificationService(DeviceRegistryRepository registryRepo, IdentityLogRepository logRepo) {
        this.registryRepo = registryRepo;
        this.logRepo = logRepo;
    }

    /**
     * Verifies device identity and persists an IdentityLog entry.
     */
    public boolean verifyIdentity(Map<String, Object> telemetry) {
    String deviceId = (String) telemetry.get("deviceId");
    boolean certificateValid = Boolean.TRUE.equals(telemetry.get("certificateValid"));
    boolean knownDevice = registryRepo.existsById(deviceId);
    boolean verified = certificateValid && knownDevice;

    IdentityLog log = new IdentityLog(deviceId, certificateValid, verified, Instant.now());
    logRepo.save(log);

    return verified;
    }

    public List<IdentityLog> getLogs(String deviceId) {
    return logRepo.findByDeviceId(deviceId);
}
}



//for my database update

// -- Migration script to add coordinate fields to location_network_changes table
// -- Run this in your database to add the new columns

// ALTER TABLE location_network_changes 
// ADD COLUMN old_latitude DOUBLE PRECISION,
// ADD COLUMN old_longitude DOUBLE PRECISION,
// ADD COLUMN new_latitude DOUBLE PRECISION,
// ADD COLUMN new_longitude DOUBLE PRECISION;

// -- Add comments to document the new columns
// COMMENT ON COLUMN location_network_changes.old_latitude IS 'Latitude coordinate of the previous location';
// COMMENT ON COLUMN location_network_changes.old_longitude IS 'Longitude coordinate of the previous location';  
// COMMENT ON COLUMN location_network_changes.new_latitude IS 'Latitude coordinate of the new location';
// COMMENT ON COLUMN location_network_changes.new_longitude IS 'Longitude coordinate of the new location';

// -- Optional: Add indexes for better query performance on coordinate-based searches
// CREATE INDEX idx_location_network_changes_new_coordinates 
// ON location_network_changes (new_latitude, new_longitude);

// CREATE INDEX idx_location_network_changes_old_coordinates 
// ON location_network_changes (old_latitude, old_longitude);