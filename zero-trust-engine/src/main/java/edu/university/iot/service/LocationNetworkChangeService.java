package edu.university.iot.service;

import edu.university.iot.model.LocationNetworkChange;
import edu.university.iot.repository.LocationNetworkChangeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LocationNetworkChangeService {

    private final LocationNetworkChangeRepository changeRepo;

    // Holds last known context per device
    private final Map<String, Context> lastContext = new ConcurrentHashMap<>();

    public LocationNetworkChangeService(LocationNetworkChangeRepository changeRepo) {
        this.changeRepo = changeRepo;
    }

    /**
     * Validates that location and IP are unchanged.
     * Returns true if unchanged; logs and returns false if changed.
     */
    public boolean validateContext(Map<String, Object> telemetry) {
        String deviceId = (String) telemetry.get("deviceId");
        String newLoc = (String) telemetry.get("location");
        String newIp = (String) telemetry.get("ipAddress");

        Context prev = lastContext.get(deviceId);
        boolean unchanged = true;

        if (prev != null && (!prev.location.equals(newLoc) || !prev.ip.equals(newIp))) {
            LocationNetworkChange record = new LocationNetworkChange();
            record.setDeviceId(deviceId);
            record.setOldLocation(prev.location);
            record.setNewLocation(newLoc);
            record.setOldIpAddress(prev.ip);
            record.setNewIpAddress(newIp);
            record.setTimestamp(LocalDateTime.now());
            changeRepo.save(record);
            unchanged = false;
        }

        lastContext.put(deviceId, new Context(newLoc, newIp));
        return unchanged;
    }

    /**
     * Get location changes for a specific device
     */
    public List<LocationNetworkChange> getChanges(String deviceId) {
        return changeRepo.findByDeviceIdOrderByTimestampDesc(deviceId);
    }

    /**
     * Get all location changes ordered by timestamp
     */
    public List<LocationNetworkChange> getAllChanges() {
        return changeRepo.findAllByOrderByTimestampDesc();
    }

    /**
     * Get count of unique devices that have location/network changes
     */
    public long getDeviceCountWithChanges() {
        try {
            return changeRepo.countDistinctDeviceIds();
        } catch (Exception e) {
            System.err.println("Error getting device count with changes: " + e.getMessage());
            return 0;
        }
    }

    // Internal holder for last context
    private static class Context {
        final String location, ip;
        Context(String location, String ip) { 
            this.location = location; 
            this.ip = ip; 
        }
    }
}