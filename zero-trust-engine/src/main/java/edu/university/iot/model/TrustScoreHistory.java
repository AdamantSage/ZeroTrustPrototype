package edu.university.iot.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entity for tracking trust score changes over time with detailed context
 */
@Entity
@Table(name = "trust_score_history", indexes = {
    @Index(name = "idx_trust_history_device_timestamp", columnList = "deviceId, timestamp"),
    @Index(name = "idx_trust_history_timestamp", columnList = "timestamp"),
    @Index(name = "idx_trust_history_severity", columnList = "severity")
})
public class TrustScoreHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String deviceId;

    @Column(nullable = false)
    private Double oldScore;

    @Column(nullable = false)
    private Double newScore;

    @Column(nullable = false)
    private Double scoreChange;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(length = 500)
    private String changeReason;

    @Column(length = 20)
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL

    // Factor analysis columns
    @Column
    private Boolean identityPassed;

    @Column
    private Boolean contextPassed;

    @Column
    private Boolean firmwareValid;

    @Column
    private Boolean anomalyDetected;

    @Column
    private Boolean compliancePassed;

    // Context at time of change
    @Column(length = 100)
    private String locationAtChange;

    @Column(length = 50)
    private String ipAddressAtChange;

    @Column
    private Double cpuUsageAtChange;

    @Column
    private Double memoryUsageAtChange;

    @Column
    private Double networkTrafficAtChange;

    // Constructors
    public TrustScoreHistory() {}

    public TrustScoreHistory(String deviceId, Double oldScore, Double newScore, String changeReason) {
        this.deviceId = deviceId;
        this.oldScore = oldScore;
        this.newScore = newScore;
        this.scoreChange = newScore - oldScore;
        this.changeReason = changeReason;
        this.timestamp = Instant.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public Double getOldScore() { return oldScore; }
    public void setOldScore(Double oldScore) { this.oldScore = oldScore; }

    public Double getNewScore() { return newScore; }
    public void setNewScore(Double newScore) { this.newScore = newScore; }

    public Double getScoreChange() { return scoreChange; }
    public void setScoreChange(Double scoreChange) { this.scoreChange = scoreChange; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public Boolean isIdentityPassed() { return identityPassed; }
    public void setIdentityPassed(Boolean identityPassed) { this.identityPassed = identityPassed; }

    public Boolean isContextPassed() { return contextPassed; }
    public void setContextPassed(Boolean contextPassed) { this.contextPassed = contextPassed; }

    public Boolean isFirmwareValid() { return firmwareValid; }
    public void setFirmwareValid(Boolean firmwareValid) { this.firmwareValid = firmwareValid; }

    public Boolean isAnomalyDetected() { return anomalyDetected; }
    public void setAnomalyDetected(Boolean anomalyDetected) { this.anomalyDetected = anomalyDetected; }

    public Boolean isCompliancePassed() { return compliancePassed; }
    public void setCompliancePassed(Boolean compliancePassed) { this.compliancePassed = compliancePassed; }

    public String getLocationAtChange() { return locationAtChange; }
    public void setLocationAtChange(String locationAtChange) { this.locationAtChange = locationAtChange; }

    public String getIpAddressAtChange() { return ipAddressAtChange; }
    public void setIpAddressAtChange(String ipAddressAtChange) { this.ipAddressAtChange = ipAddressAtChange; }

    public Double getCpuUsageAtChange() { return cpuUsageAtChange; }
    public void setCpuUsageAtChange(Double cpuUsageAtChange) { this.cpuUsageAtChange = cpuUsageAtChange; }

    public Double getMemoryUsageAtChange() { return memoryUsageAtChange; }
    public void setMemoryUsageAtChange(Double memoryUsageAtChange) { this.memoryUsageAtChange = memoryUsageAtChange; }

    public Double getNetworkTrafficAtChange() { return networkTrafficAtChange; }
    public void setNetworkTrafficAtChange(Double networkTrafficAtChange) { this.networkTrafficAtChange = networkTrafficAtChange; }
}