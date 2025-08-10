package edu.university.iot.model.dtoModel;

import java.time.Instant;

public class TrustHistoryDto {
    private Instant timestamp;
    private Double trustScore;
    private String eventType;
    private String description;

    // Constructors
    public TrustHistoryDto() {}

    public TrustHistoryDto(Instant timestamp, Double trustScore, String eventType) {
        this.timestamp = timestamp;
        this.trustScore = trustScore;
        this.eventType = eventType;
    }

    // Getters and Setters
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public Double getTrustScore() { return trustScore; }
    public void setTrustScore(Double trustScore) { this.trustScore = trustScore; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}