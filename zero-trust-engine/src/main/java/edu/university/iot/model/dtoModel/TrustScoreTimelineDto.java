package edu.university.iot.model.dtoModel;

import java.time.Instant;
import java.util.Map;

/**
 * DTO for trust score timeline visualization
 */
public class TrustScoreTimelineDto {
    private Instant timestamp;
    private double trustScore;
    private Double scoreChange;
    private String eventType; // IMPROVEMENT, DEGRADATION, MAJOR_IMPROVEMENT, MAJOR_DEGRADATION, CURRENT
    private String description;
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL
    private Map<String, Object> context;

    // Constructors
    public TrustScoreTimelineDto() {}

    public TrustScoreTimelineDto(Instant timestamp, double trustScore, String eventType, String description) {
        this.timestamp = timestamp;
        this.trustScore = trustScore;
        this.eventType = eventType;
        this.description = description;
    }

    // Getters and Setters
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public double getTrustScore() { return trustScore; }
    public void setTrustScore(double trustScore) { this.trustScore = trustScore; }

    public Double getScoreChange() { return scoreChange; }
    public void setScoreChange(Double scoreChange) { this.scoreChange = scoreChange; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public Map<String, Object> getContext() { return context; }
    public void setContext(Map<String, Object> context) { this.context = context; }
}