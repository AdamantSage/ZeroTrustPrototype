package edu.university.iot.service;

import edu.university.iot.model.LocationNetworkChange;
import edu.university.iot.model.dtoModel.LocationMapDto;
import edu.university.iot.model.dtoModel.DeviceLocationDto;
import edu.university.iot.model.dtoModel.LocationAlertDto;
import edu.university.iot.repository.LocationNetworkChangeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Unified LocationService - FIXED to handle CoordinateData entity
 */
@Service
public class LocationService {

    private static final Logger log = LoggerFactory.getLogger(LocationService.class);

    private final LocationNetworkChangeRepository changeRepo;

    private static final Map<String, LocationMapDto> CAMPUS_LOCATIONS = initializeCampusLocations();

    private final Map<String, DeviceLocationDto> currentDeviceLocations = new ConcurrentHashMap<>();
    private final Map<String, LocationContext> lastKnownContext = new ConcurrentHashMap<>();

    public LocationService(LocationNetworkChangeRepository changeRepo) {
        this.changeRepo = changeRepo;
    }

    /**
     * Main validation method - FIXED to handle both Map and CoordinateData entity
     */
    public boolean validateContext(Map<String, Object> telemetry) {
        if (telemetry == null) {
            log.debug("validateContext: null telemetry provided");
            return true;
        }

        String deviceId = safeToString(telemetry.get("deviceId"));
        if (deviceId == null || deviceId.trim().isEmpty()) {
            log.debug("validateContext: missing deviceId in telemetry, skipping validation");
            return true;
        }

        String newLocation = safeToString(telemetry.get("location"));
        String newIpAddress = safeToString(telemetry.get("ipAddress"));

        // Extract coordinates - handle both Map and CoordinateData entity
        Double newLat = null;
        Double newLng = null;
        Object coordsObj = telemetry.get("coordinates");

        if (coordsObj instanceof Map) {
            // Standard Map case
            Map<?, ?> coordinates = (Map<?, ?>) coordsObj;
            newLat = safeParseDouble(coordinates.get("lat"));
            newLng = safeParseDouble(coordinates.get("lng"));
        } else if (coordsObj != null) {
            // Handle CoordinateData entity via reflection
            try {
                java.lang.reflect.Method getLat = coordsObj.getClass().getMethod("getLat");
                java.lang.reflect.Method getLng = coordsObj.getClass().getMethod("getLng");
                newLat = (Double) getLat.invoke(coordsObj);
                newLng = (Double) getLng.invoke(coordsObj);
            } catch (Exception e) {
                log.warn("Could not extract coordinates from object type: {}",
                        coordsObj.getClass().getName());
            }
        }

        LocationContext previousContext = lastKnownContext.get(deviceId);
        boolean hasChanged = false;

        // Check if location or IP has changed
        if (previousContext != null) {
            boolean locationChanged = !Objects.equals(previousContext.location, newLocation);
            boolean ipChanged = !Objects.equals(previousContext.ipAddress, newIpAddress);
            boolean coordinatesChanged = !Objects.equals(previousContext.latitude, newLat) ||
                    !Objects.equals(previousContext.longitude, newLng);

            if (locationChanged || ipChanged || coordinatesChanged) {
                // Record the change
                recordLocationChange(deviceId, previousContext, newLocation, newIpAddress, newLat, newLng, telemetry);
                hasChanged = true;

                // Generate alerts for suspicious patterns
                generateAlertsIfNeeded(deviceId, previousContext.location, newLocation);
            }
        }

        // Update context and current location tracking
        updateDeviceContext(deviceId, newLocation, newIpAddress, newLat, newLng, telemetry);

        return !hasChanged;
    }

    // ========== DATA PERSISTENCE ==========

    private void recordLocationChange(String deviceId, LocationContext previous,
            String newLocation, String newIpAddress,
            Double newLat, Double newLng,
            Map<String, Object> telemetry) {
        try {
            LocationNetworkChange change = new LocationNetworkChange();
            change.setDeviceId(deviceId);
            change.setOldLocation(previous.location);
            change.setNewLocation(newLocation);
            change.setOldIpAddress(previous.ipAddress);
            change.setNewIpAddress(newIpAddress);
            change.setTimestamp(LocalDateTime.now());

            // Set coordinates
            if (previous.latitude != null && previous.longitude != null) {
                change.setOldLatitude(previous.latitude);
                change.setOldLongitude(previous.longitude);
            }
            if (newLat != null && newLng != null) {
                change.setNewLatitude(newLat);
                change.setNewLongitude(newLng);
            }

            changeRepo.save(change);
            log.info("Recorded location change for device {}: {} -> {}",
                    deviceId, previous.location, newLocation);

        } catch (Exception e) {
            log.error("Failed to save location change for device {}: {}", deviceId, e.getMessage(), e);
        }
    }

    private void updateDeviceContext(String deviceId, String location, String ipAddress,
            Double lat, Double lng, Map<String, Object> telemetry) {
        // Update last known context
        lastKnownContext.put(deviceId, new LocationContext(location, ipAddress, lat, lng));

        // Update real-time tracking
        DeviceLocationDto deviceLocation = new DeviceLocationDto();
        deviceLocation.setDeviceId(deviceId);
        deviceLocation.setCurrentLocation(location);
        deviceLocation.setCurrentIpAddress(ipAddress);
        deviceLocation.setLatitude(lat);
        deviceLocation.setLongitude(lng);
        deviceLocation.setLastUpdate(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());

        // Add telemetry scores
        deviceLocation.setTrustScore(safeParseDouble(telemetry.get("trustScore")));
        deviceLocation.setAnomalyScore(safeParseDouble(telemetry.get("anomalyScore")));
        deviceLocation.setSuspiciousActivityScore(safeParseInteger(telemetry.get("suspiciousActivityScore")));

        // Determine location type and risk
        LocationMapDto locationInfo = CAMPUS_LOCATIONS.get(location);
        if (locationInfo != null) {
            deviceLocation.setLocationType(locationInfo.getType());
            deviceLocation.setRiskLevel(calculateRiskLevel(locationInfo.getType(), deviceLocation));
        } else {
            deviceLocation.setLocationType("UNKNOWN");
            deviceLocation.setRiskLevel("HIGH");
        }

        currentDeviceLocations.put(deviceId, deviceLocation);
    }

    // ========== ALERT GENERATION ==========

    private void generateAlertsIfNeeded(String deviceId, String oldLocation, String newLocation) {
        try {
            LocationMapDto oldLoc = CAMPUS_LOCATIONS.get(oldLocation);
            LocationMapDto newLoc = CAMPUS_LOCATIONS.get(newLocation);

            List<String> alertReasons = new ArrayList<>();

            if (oldLoc != null && newLoc != null) {
                if ("RESTRICTED".equals(oldLoc.getType()) && "EXTERNAL".equals(newLoc.getType())) {
                    alertReasons.add("Movement from restricted to external location");
                }
            }

            long recentChanges = getLocationHistory(deviceId, 1).size();
            if (recentChanges >= 3) {
                alertReasons.add("Excessive location changes in short time");
            }

            if (!alertReasons.isEmpty()) {
                log.warn("LOCATION ALERT [{}]: {} -> {}. Reasons: {}",
                        deviceId, oldLocation, newLocation, String.join(", ", alertReasons));
            }

        } catch (Exception e) {
            log.error("Error generating alerts for device {}: {}", deviceId, e.getMessage());
        }
    }

    // ========== PUBLIC API METHODS ==========

    public List<LocationNetworkChange> getChanges(String deviceId) {
        return changeRepo.findByDeviceIdOrderByTimestampDesc(deviceId);
    }

    public List<LocationNetworkChange> getAllChanges() {
        return changeRepo.findAllByOrderByTimestampDesc();
    }

    public List<LocationNetworkChange> getLocationHistory(String deviceId, int hours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        return changeRepo.findByDeviceIdOrderByTimestampDesc(deviceId).stream()
                .filter(change -> change.getTimestamp().isAfter(cutoff))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getLocationMapData() {
        Map<String, Object> mapData = new HashMap<>();
        mapData.put("campusLocations", CAMPUS_LOCATIONS.values());
        mapData.put("deviceLocations", new ArrayList<>(currentDeviceLocations.values()));
        mapData.put("locationAlerts", generateRecentAlerts());
        mapData.put("campusBounds", calculateCampusBounds());
        return mapData;
    }

    public Map<String, Object> getLocationStatistics(String deviceId) {
        Map<String, Object> stats = new HashMap<>();

        try {
            List<LocationNetworkChange> recentChanges = getLocationHistory(deviceId, 24);

            stats.put("totalChanges24h", recentChanges.size());
            stats.put("uniqueLocations24h", recentChanges.stream()
                    .map(LocationNetworkChange::getNewLocation)
                    .distinct().count());

            Map<String, Long> typeDistribution = recentChanges.stream()
                    .collect(Collectors.groupingBy(
                            change -> getLocationTypeFor(change.getNewLocation()),
                            Collectors.counting()));
            stats.put("locationTypeDistribution", typeDistribution);

            long highRiskChanges = recentChanges.stream()
                    .mapToLong(change -> isHighRiskLocation(change.getNewLocation()) ? 1L : 0L)
                    .sum();

            stats.put("highRiskChanges24h", highRiskChanges);
            stats.put("riskScore", calculateOverallRiskScore(recentChanges));

        } catch (Exception e) {
            log.error("Error calculating statistics for device {}: {}", deviceId, e.getMessage());
        }

        return stats;
    }

    public long getDeviceCountWithChanges() {
        try {
            return changeRepo.countDistinctDeviceIds();
        } catch (Exception e) {
            log.error("Error getting device count: {}", e.getMessage());
            return 0;
        }
    }

    // ========== HELPER METHODS ==========

    private static Map<String, LocationMapDto> initializeCampusLocations() {
        Map<String, LocationMapDto> locations = new HashMap<>();
        locations.put("Library-Floor1",
                new LocationMapDto("Library-Floor1", "Library Floor 1", -26.6876, 27.0936, "ACADEMIC", "10.1.1.0/24"));
        locations.put("Library-Floor2",
                new LocationMapDto("Library-Floor2", "Library Floor 2", -26.6877, 27.0937, "ACADEMIC", "10.1.2.0/24"));
        locations.put("Lecture-Hall-A",
                new LocationMapDto("Lecture-Hall-A", "Lecture Hall A", -26.6880, 27.0940, "ACADEMIC", "10.1.3.0/24"));
        locations.put("Lecture-Hall-B",
                new LocationMapDto("Lecture-Hall-B", "Lecture Hall B", -26.6882, 27.0942, "ACADEMIC", "10.1.4.0/24"));
        locations.put("Computer-Lab-1",
                new LocationMapDto("Computer-Lab-1", "Computer Lab 1", -26.6875, 27.0935, "LAB", "10.1.5.0/24"));
        locations.put("Computer-Lab-2",
                new LocationMapDto("Computer-Lab-2", "Computer Lab 2", -26.6873, 27.0933, "LAB", "10.1.6.0/24"));
        locations.put("Student-Center",
                new LocationMapDto("Student-Center", "Student Center", -26.6878, 27.0938, "SOCIAL", "10.1.7.0/24"));
        locations.put("Admin-Building", new LocationMapDto("Admin-Building", "Administration Building", -26.6885,
                27.0945, "RESTRICTED", "10.1.8.0/24"));
        locations.put("Cafeteria",
                new LocationMapDto("Cafeteria", "Cafeteria", -26.6879, 27.0939, "SOCIAL", "10.1.9.0/24"));
        locations.put("Off-Campus-Home", new LocationMapDto("Off-Campus-Home", "Off-Campus Home", -26.7000, 27.1000,
                "EXTERNAL", "192.168.1.0/24"));
        locations.put("Off-Campus-Cafe", new LocationMapDto("Off-Campus-Cafe", "Off-Campus Cafe", -26.6950, 27.0900,
                "EXTERNAL", "192.168.43.0/24"));
        return locations;
    }

    private String calculateRiskLevel(String locationType, DeviceLocationDto deviceLocation) {
        if ("EXTERNAL".equals(locationType))
            return "HIGH";
        if ("RESTRICTED".equals(locationType)) {
            Double trustScore = deviceLocation.getTrustScore();
            return (trustScore != null && trustScore < 80) ? "CRITICAL" : "MEDIUM";
        }
        Double anomalyScore = deviceLocation.getAnomalyScore();
        if (anomalyScore != null && anomalyScore > 0.7)
            return "HIGH";
        if (anomalyScore != null && anomalyScore > 0.4)
            return "MEDIUM";
        return "LOW";
    }

    private List<LocationAlertDto> generateRecentAlerts() {
        LocalDateTime twoHoursAgo = LocalDateTime.now().minusHours(2);
        return changeRepo.findAllByOrderByTimestampDesc().stream()
                .filter(change -> change.getTimestamp().isAfter(twoHoursAgo))
                .limit(50)
                .map(this::convertToAlert)
                .collect(Collectors.toList());
    }

    private LocationAlertDto convertToAlert(LocationNetworkChange change) {
        LocationAlertDto alert = new LocationAlertDto();
        alert.setDeviceId(change.getDeviceId());
        alert.setOldLocation(change.getOldLocation());
        alert.setNewLocation(change.getNewLocation());
        alert.setTimestamp(change.getTimestamp().atZone(ZoneId.systemDefault()).toInstant());

        boolean isHighRisk = isHighRiskLocation(change.getOldLocation()) || isHighRiskLocation(change.getNewLocation());
        alert.setSeverity(isHighRisk ? "HIGH" : "MEDIUM");
        alert.setReason(isHighRisk ? "Involves restricted/external location" : "Normal location change");

        return alert;
    }

    private Map<String, Double> calculateCampusBounds() {
        double minLat = CAMPUS_LOCATIONS.values().stream()
                .filter(loc -> !"EXTERNAL".equals(loc.getType()))
                .mapToDouble(LocationMapDto::getLatitude).min().orElse(-26.69);
        double maxLat = CAMPUS_LOCATIONS.values().stream()
                .filter(loc -> !"EXTERNAL".equals(loc.getType()))
                .mapToDouble(LocationMapDto::getLatitude).max().orElse(-26.68);
        double minLng = CAMPUS_LOCATIONS.values().stream()
                .filter(loc -> !"EXTERNAL".equals(loc.getType()))
                .mapToDouble(LocationMapDto::getLongitude).min().orElse(27.09);
        double maxLng = CAMPUS_LOCATIONS.values().stream()
                .filter(loc -> !"EXTERNAL".equals(loc.getType()))
                .mapToDouble(LocationMapDto::getLongitude).max().orElse(27.10);

        Map<String, Double> bounds = new HashMap<>();
        bounds.put("north", maxLat + 0.001);
        bounds.put("south", minLat - 0.001);
        bounds.put("east", maxLng + 0.001);
        bounds.put("west", minLng - 0.001);
        return bounds;
    }

    private String getLocationTypeFor(String location) {
        LocationMapDto loc = CAMPUS_LOCATIONS.get(location);
        return loc != null ? loc.getType() : "UNKNOWN";
    }

    private boolean isHighRiskLocation(String location) {
        LocationMapDto loc = CAMPUS_LOCATIONS.get(location);
        return loc != null && ("EXTERNAL".equals(loc.getType()) || "RESTRICTED".equals(loc.getType()));
    }

    private double calculateOverallRiskScore(List<LocationNetworkChange> changes) {
        if (changes.isEmpty())
            return 0.0;

        double score = Math.min(changes.size() * 5, 30);

        long highRiskChanges = changes.stream()
                .mapToLong(change -> isHighRiskLocation(change.getNewLocation()) ? 1L : 0L)
                .sum();

        score += highRiskChanges * 15;

        return Math.min(score, 100.0);
    }

    private static String safeToString(Object obj) {
        return obj != null ? String.valueOf(obj) : null;
    }

    private static Double safeParseDouble(Object obj) {
        if (obj == null)
            return null;
        if (obj instanceof Double)
            return (Double) obj;
        if (obj instanceof Integer)
            return ((Integer) obj).doubleValue();
        if (obj instanceof String) {
            try {
                return Double.parseDouble((String) obj);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    private static Integer safeParseInteger(Object obj) {
        if (obj == null)
            return null;
        if (obj instanceof Integer)
            return (Integer) obj;
        if (obj instanceof Double)
            return ((Double) obj).intValue();
        if (obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private static class LocationContext {
        final String location;
        final String ipAddress;
        final Double latitude;
        final Double longitude;

        LocationContext(String location, String ipAddress, Double latitude, Double longitude) {
            this.location = location;
            this.ipAddress = ipAddress;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}