package edu.university.iot.service;

import edu.university.iot.model.LocationNetworkChange;
import edu.university.iot.model.dtoModel.LocationMapDto;
import edu.university.iot.model.dtoModel.DeviceLocationDto;
import edu.university.iot.model.dtoModel.LocationAlertDto;
import edu.university.iot.repository.LocationNetworkChangeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Enhanced location monitoring service with campus mapping and real-time tracking
 */
@Service
public class LocationMonitoringService {

    private final LocationNetworkChangeRepository changeRepo;
    
    // Fixed: Use traditional HashMap initialization for Java 8 compatibility
    private static final Map<String, LocationMapDto> CAMPUS_LOCATIONS;
    
    static {
        CAMPUS_LOCATIONS = new HashMap<>();
        CAMPUS_LOCATIONS.put("Library-Floor1", new LocationMapDto("Library-Floor1", "Library Floor 1", -26.6876, 27.0936, "ACADEMIC", "10.1.1.0/24"));
        CAMPUS_LOCATIONS.put("Library-Floor2", new LocationMapDto("Library-Floor2", "Library Floor 2", -26.6877, 27.0937, "ACADEMIC", "10.1.2.0/24"));
        CAMPUS_LOCATIONS.put("Lecture-Hall-A", new LocationMapDto("Lecture-Hall-A", "Lecture Hall A", -26.6880, 27.0940, "ACADEMIC", "10.1.3.0/24"));
        CAMPUS_LOCATIONS.put("Lecture-Hall-B", new LocationMapDto("Lecture-Hall-B", "Lecture Hall B", -26.6882, 27.0942, "ACADEMIC", "10.1.4.0/24"));
        CAMPUS_LOCATIONS.put("Computer-Lab-1", new LocationMapDto("Computer-Lab-1", "Computer Lab 1", -26.6875, 27.0935, "LAB", "10.1.5.0/24"));
        CAMPUS_LOCATIONS.put("Computer-Lab-2", new LocationMapDto("Computer-Lab-2", "Computer Lab 2", -26.6873, 27.0933, "LAB", "10.1.6.0/24"));
        CAMPUS_LOCATIONS.put("Student-Center", new LocationMapDto("Student-Center", "Student Center", -26.6878, 27.0938, "SOCIAL", "10.1.7.0/24"));
        CAMPUS_LOCATIONS.put("Admin-Building", new LocationMapDto("Admin-Building", "Administration Building", -26.6885, 27.0945, "RESTRICTED", "10.1.8.0/24"));
        CAMPUS_LOCATIONS.put("Cafeteria", new LocationMapDto("Cafeteria", "Cafeteria", -26.6879, 27.0939, "SOCIAL", "10.1.9.0/24"));
        CAMPUS_LOCATIONS.put("Off-Campus-Home", new LocationMapDto("Off-Campus-Home", "Off-Campus Home", -26.7000, 27.1000, "EXTERNAL", "192.168.1.0/24"));
        CAMPUS_LOCATIONS.put("Off-Campus-Cafe", new LocationMapDto("Off-Campus-Cafe", "Off-Campus Cafe", -26.6950, 27.0900, "EXTERNAL", "192.168.43.0/24"));
    }

    // Holds current location per device
    private final Map<String, DeviceLocationDto> currentLocations = new ConcurrentHashMap<>();
    
    // Holds last known context per device (from original service)
    private final Map<String, Context> lastContext = new ConcurrentHashMap<>();

    public LocationMonitoringService(LocationNetworkChangeRepository changeRepo) {
        this.changeRepo = changeRepo;
    }

    /**
     * Validates that location and IP are unchanged.
     * Returns true if unchanged; logs and returns false if changed.
     * Enhanced with coordinate tracking and alert generation.
     */
    public boolean validateContext(Map<String, Object> telemetry) {
        String deviceId = (String) telemetry.get("deviceId");
        String newLoc = (String) telemetry.get("location");
        String newIp = (String) telemetry.get("ipAddress");
        
        // Extract coordinates if provided
        Map<String, Object> coordinates = (Map<String, Object>) telemetry.get("coordinates");
        Double lat = coordinates != null ? (Double) coordinates.get("lat") : null;
        Double lng = coordinates != null ? (Double) coordinates.get("lng") : null;

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
            
            // Add coordinate information if available
            if (lat != null && lng != null) {
                record.setNewLatitude(lat);
                record.setNewLongitude(lng);
            }
            
            changeRepo.save(record);
            unchanged = false;
            
            // Generate location alert if suspicious
            generateLocationAlert(deviceId, prev.location, newLoc);
        }

        lastContext.put(deviceId, new Context(newLoc, newIp));
        
        // Update current location tracking
        updateCurrentLocation(deviceId, newLoc, newIp, lat, lng, telemetry);
        
        return unchanged;
    }

    /**
     * Update current location tracking for real-time monitoring
     */
    private void updateCurrentLocation(String deviceId, String location, String ipAddress, 
                                     Double lat, Double lng, Map<String, Object> telemetry) {
        DeviceLocationDto deviceLocation = new DeviceLocationDto();
        deviceLocation.setDeviceId(deviceId);
        deviceLocation.setCurrentLocation(location);
        deviceLocation.setCurrentIpAddress(ipAddress);
        deviceLocation.setLatitude(lat);
        deviceLocation.setLongitude(lng);
        deviceLocation.setLastUpdate(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        
        // Add additional context from telemetry
        deviceLocation.setTrustScore((Double) telemetry.get("trustScore"));
        
        // Safely parse anomalyScore as it might be a String or Double
        Object anomalyScoreObj = telemetry.get("anomalyScore");
        if (anomalyScoreObj instanceof String) {
            try {
                deviceLocation.setAnomalyScore(Double.parseDouble((String) anomalyScoreObj));
            } catch (NumberFormatException e) {
                deviceLocation.setAnomalyScore(0.0);
            }
        } else if (anomalyScoreObj instanceof Double) {
            deviceLocation.setAnomalyScore((Double) anomalyScoreObj);
        } else {
            deviceLocation.setAnomalyScore(0.0);
        }
        
        deviceLocation.setSuspiciousActivityScore((Integer) telemetry.get("suspiciousActivityScore"));
        
        // Determine location risk level
        LocationMapDto locationInfo = CAMPUS_LOCATIONS.get(location);
        if (locationInfo != null) {
            deviceLocation.setLocationType(locationInfo.getType());
            deviceLocation.setRiskLevel(determineLocationRisk(locationInfo.getType(), deviceLocation));
        } else {
            deviceLocation.setLocationType("UNKNOWN");
            deviceLocation.setRiskLevel("HIGH"); // Unknown locations are high risk
        }
        
        currentLocations.put(deviceId, deviceLocation);
    }

    /**
     * Determine risk level based on location type and device metrics
     */
    private String determineLocationRisk(String locationType, DeviceLocationDto deviceLocation) {
        if ("EXTERNAL".equals(locationType)) {
            return "HIGH";
        }
        
        if ("RESTRICTED".equals(locationType)) {
            // Admin areas require higher trust
            Double trustScore = deviceLocation.getTrustScore();
            if (trustScore == null || trustScore < 80) {
                return "CRITICAL";
            }
            return "MEDIUM";
        }
        
        // Factor in anomaly score
        if (deviceLocation.getAnomalyScore() > 0.7) {
            return "HIGH";
        } else if (deviceLocation.getAnomalyScore() > 0.4) {
            return "MEDIUM";
        }
        
        return "LOW";
    }

    /**
     * Generate location-based alerts for suspicious movement patterns
     */
    /**
 * Generate location-based alerts for suspicious movement patterns
 */
private void generateLocationAlert(String deviceId, String oldLocation, String newLocation) {
    LocationMapDto oldLoc = CAMPUS_LOCATIONS.get(oldLocation);
    LocationMapDto newLoc = CAMPUS_LOCATIONS.get(newLocation);
    
    if (oldLoc == null || newLoc == null) return;
    
    // Initialize the alertReasons list that was missing
    List<String> alertReasons = new ArrayList<>();
    
    // Moving from restricted to external
    if ("RESTRICTED".equals(oldLoc.getType()) && "EXTERNAL".equals(newLoc.getType())) {
        alertReasons.add("Movement from restricted area to external location");
    }
    
    // Frequent location changes (check last hour)
    LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
    long recentChanges = changeRepo.findByDeviceIdOrderByTimestampDesc(deviceId).stream()
        .filter(change -> change.getTimestamp().isAfter(oneHourAgo))
        .count();
    
    if (recentChanges >= 3) {
        alertReasons.add("Excessive location changes in short time period");
    }
    
    // If there are alert reasons, this would be logged or sent to monitoring system
    if (!alertReasons.isEmpty()) {
        System.out.println(String.format("LOCATION ALERT [%s]: %s -> %s. Reasons: %s", 
            deviceId, oldLocation, newLocation, String.join(", ", alertReasons)));
    }
}

    /**
     * Get current location map data for all devices
     */
    public Map<String, Object> getLocationMapData() {
        Map<String, Object> mapData = new HashMap<>();
        
        // Campus locations
        mapData.put("campusLocations", CAMPUS_LOCATIONS.values());
        
        // Current device locations
        List<DeviceLocationDto> deviceLocations = new ArrayList<>(currentLocations.values());
        mapData.put("deviceLocations", deviceLocations);
        
        // Location alerts (recent changes with high risk)
        List<LocationAlertDto> alerts = generateLocationAlerts();
        mapData.put("locationAlerts", alerts);
        
        // Campus bounds for map centering
        Map<String, Double> bounds = calculateCampusBounds();
        mapData.put("campusBounds", bounds);
        
        return mapData;
    }

    /**
     * Calculate campus bounds for map display
     */
    private Map<String, Double> calculateCampusBounds() {
        double minLat = CAMPUS_LOCATIONS.values().stream()
            .filter(loc -> !"EXTERNAL".equals(loc.getType()))
            .mapToDouble(LocationMapDto::getLatitude)
            .min().orElse(-26.69);
        
        double maxLat = CAMPUS_LOCATIONS.values().stream()
            .filter(loc -> !"EXTERNAL".equals(loc.getType()))
            .mapToDouble(LocationMapDto::getLatitude)
            .max().orElse(-26.68);
        
        double minLng = CAMPUS_LOCATIONS.values().stream()
            .filter(loc -> !"EXTERNAL".equals(loc.getType()))
            .mapToDouble(LocationMapDto::getLongitude)
            .min().orElse(27.09);
        
        double maxLng = CAMPUS_LOCATIONS.values().stream()
            .filter(loc -> !"EXTERNAL".equals(loc.getType()))
            .mapToDouble(LocationMapDto::getLongitude)
            .max().orElse(27.10);
        
        // Add padding
        double latPadding = (maxLat - minLat) * 0.1;
        double lngPadding = (maxLng - minLng) * 0.1;
        
        Map<String, Double> bounds = new HashMap<>();
        bounds.put("north", maxLat + latPadding);
        bounds.put("south", minLat - latPadding);
        bounds.put("east", maxLng + lngPadding);
        bounds.put("west", minLng - lngPadding);
        
        return bounds;
    }

    /**
     * Generate recent location alerts for dashboard
     */
    private List<LocationAlertDto> generateLocationAlerts() {
        LocalDateTime twoHoursAgo = LocalDateTime.now().minusHours(2);
        
        return changeRepo.findAllByOrderByTimestampDesc().stream()
            .filter(change -> change.getTimestamp().isAfter(twoHoursAgo))
            .map(change -> {
                LocationAlertDto alert = new LocationAlertDto();
                alert.setDeviceId(change.getDeviceId());
                alert.setOldLocation(change.getOldLocation());
                alert.setNewLocation(change.getNewLocation());
                alert.setTimestamp(change.getTimestamp().atZone(ZoneId.systemDefault()).toInstant());
                
                // Determine alert severity
                LocationMapDto oldLoc = CAMPUS_LOCATIONS.get(change.getOldLocation());
                LocationMapDto newLoc = CAMPUS_LOCATIONS.get(change.getNewLocation());
                
                if (oldLoc != null && newLoc != null) {
                    if ("RESTRICTED".equals(oldLoc.getType()) || "EXTERNAL".equals(newLoc.getType())) {
                        alert.setSeverity("HIGH");
                        alert.setReason("Movement involving restricted/external areas");
                    } else {
                        alert.setSeverity("MEDIUM");
                        alert.setReason("Normal location change");
                    }
                } else {
                    alert.setSeverity("HIGH");
                    alert.setReason("Unknown location detected");
                }
                
                return alert;
            })
            .limit(50) // Limit to recent alerts
            .collect(Collectors.toList());
    }

    /**
     * Get detailed location history for a specific device
     */
    public List<LocationNetworkChange> getLocationHistory(String deviceId, int hours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        return changeRepo.findByDeviceIdOrderByTimestampDesc(deviceId).stream()
            .filter(change -> change.getTimestamp().isAfter(cutoff))
            .collect(Collectors.toList());
    }

    /**
     * Get location statistics for analytics
     */
    public Map<String, Object> getLocationStatistics(String deviceId) {
        Map<String, Object> stats = new HashMap<>();
        
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        List<LocationNetworkChange> recentChanges = changeRepo.findByDeviceIdOrderByTimestampDesc(deviceId).stream()
            .filter(change -> change.getTimestamp().isAfter(oneDayAgo))
            .collect(Collectors.toList());
        
        stats.put("totalChanges24h", recentChanges.size());
        stats.put("uniqueLocations24h", recentChanges.stream()
            .map(LocationNetworkChange::getNewLocation)
            .distinct()
            .count());
        
        // Calculate time spent in different location types
        Map<String, Long> locationTypeTime = recentChanges.stream()
            .collect(Collectors.groupingBy(
                change -> {
                    LocationMapDto loc = CAMPUS_LOCATIONS.get(change.getNewLocation());
                    return loc != null ? loc.getType() : "UNKNOWN";
                },
                Collectors.counting()
            ));
        
        stats.put("locationTypeDistribution", locationTypeTime);
        
        // Risk assessment
        long highRiskChanges = recentChanges.stream()
            .mapToLong(change -> {
                LocationMapDto loc = CAMPUS_LOCATIONS.get(change.getNewLocation());
                return (loc != null && ("EXTERNAL".equals(loc.getType()) || "RESTRICTED".equals(loc.getType()))) ? 1L : 0L;
            })
            .sum();
        
        stats.put("highRiskChanges24h", highRiskChanges);
        stats.put("riskScore", calculateLocationRiskScore(recentChanges));
        
        return stats;
    }

    /**
     * Calculate location-based risk score for a device
     */
    private double calculateLocationRiskScore(List<LocationNetworkChange> changes) {
        if (changes.isEmpty()) return 0.0;
        
        double riskScore = 0.0;
        
        // Factor 1: Frequency of changes (more changes = higher risk)
        riskScore += Math.min(changes.size() * 5, 30);
        
        // Factor 2: Type of locations accessed
        long externalAccess = changes.stream()
            .mapToLong(change -> {
                LocationMapDto loc = CAMPUS_LOCATIONS.get(change.getNewLocation());
                return (loc != null && "EXTERNAL".equals(loc.getType())) ? 1L : 0L;
            })
            .sum();
        
        long restrictedAccess = changes.stream()
            .mapToLong(change -> {
                LocationMapDto loc = CAMPUS_LOCATIONS.get(change.getNewLocation());
                return (loc != null && "RESTRICTED".equals(loc.getType())) ? 1L : 0L;
            })
            .sum();
        
        riskScore += externalAccess * 15;  // External access is high risk
        riskScore += restrictedAccess * 10; // Restricted access is medium-high risk
        
        // Factor 3: Rapid successive changes (indicates possible compromise)
        long rapidChanges = 0;
        for (int i = 1; i < changes.size(); i++) {
            if (changes.get(i-1).getTimestamp().minusMinutes(15).isBefore(changes.get(i).getTimestamp())) {
                rapidChanges++;
            }
        }
        riskScore += rapidChanges * 20;
        
        return Math.min(riskScore, 100.0); // Cap at 100
    }

    // Legacy methods for compatibility
    public List<LocationNetworkChange> getChanges(String deviceId) {
        return changeRepo.findByDeviceIdOrderByTimestampDesc(deviceId);
    }

    public List<LocationNetworkChange> getAllChanges() {
        return changeRepo.findAllByOrderByTimestampDesc();
    }

    public long getDeviceCountWithChanges() {
        try {
            return changeRepo.countDistinctDeviceIds();
        } catch (Exception e) {
            System.err.println("Error getting device count with changes: " + e.getMessage());
            return 0;
        }
    }

    // Internal holder for last context (from original service)
    private static class Context {
        final String location, ip;
        Context(String location, String ip) { 
            this.location = location; 
            this.ip = ip; 
        }
    }
}