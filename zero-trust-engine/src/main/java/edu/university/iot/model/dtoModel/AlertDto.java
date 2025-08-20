package edu.university.iot.model.dtoModel;

import java.time.Instant;

public class AlertDto {
    private String deviceId;
    private String type;
    private String severity;
    private String message;
    private String category;
    private Instant timestamp;

    // Default constructor
    public AlertDto() {}

    // Constructor with all fields
    public AlertDto(String deviceId, String type, String severity, String message, String category, Instant timestamp) {
        this.deviceId = deviceId;
        this.type = type;
        this.severity = severity;
        this.message = message;
        this.category = category;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "AlertDto{" +
                "deviceId='" + deviceId + '\'' +
                ", type='" + type + '\'' +
                ", severity='" + severity + '\'' +
                ", message='" + message + '\'' +
                ", category='" + category + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}