package edu.university.iot.model.dtoModel;

import java.time.Instant;
import java.util.Map;

public class TrustFactorDto {
    private String factorName;
    private String category;
    private Double score;
    private String status;
    private String description;
    private String impact;
    private Instant lastUpdated;
    private int dataPoints;
    private Map<String, Object> details;

    // Constructors
    public TrustFactorDto() {}

    // Getters and Setters
    public String getFactorName() { return factorName; }
    public void setFactorName(String factorName) { this.factorName = factorName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImpact() { return impact; }
    public void setImpact(String impact) { this.impact = impact; }

    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }

    public int getDataPoints() { return dataPoints; }
    public void setDataPoints(int dataPoints) { this.dataPoints = dataPoints; }

    public Map<String, Object> getDetails() { return details; }
    public void setDetails(Map<String, Object> details) { this.details = details; }
}
