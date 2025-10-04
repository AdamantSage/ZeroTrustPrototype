package edu.university.iot.model.dtoModel;

import java.time.Instant;

public class LocationAlertDto {
    private String deviceId;
    private String oldLocation;
    private String newLocation;
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL
    private String reason;
    private Instant timestamp;

    // Constructors
    public LocationAlertDto() {}

    // Getters and Setters
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getOldLocation() { return oldLocation; }
    public void setOldLocation(String oldLocation) { this.oldLocation = oldLocation; }

    public String getNewLocation() { return newLocation; }
    public void setNewLocation(String newLocation) { this.newLocation = newLocation; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}