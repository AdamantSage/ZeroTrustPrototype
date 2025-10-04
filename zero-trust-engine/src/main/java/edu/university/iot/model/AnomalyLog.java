// src/main/java/edu/university/iot/entity/AnomalyLog.java
package edu.university.iot.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class AnomalyLog {
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getDeviceId() {
        return deviceId;
    }
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    public double getCpuUsage() {
        return cpuUsage;
    }
    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }
    public double getMemoryUsage() {
        return memoryUsage;
    }
    public void setMemoryUsage(double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }
    public double getNetworkTrafficVolume() {
        return networkTrafficVolume;
    }
    public void setNetworkTrafficVolume(double networkTrafficVolume) {
        this.networkTrafficVolume = networkTrafficVolume;
    }
    public boolean isAnomalyDetected() {
        return anomalyDetected;
    }
    public void setAnomalyDetected(boolean anomalyDetected) {
        this.anomalyDetected = anomalyDetected;
    }
    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }
    public Instant getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deviceId;
    private double cpuUsage;
    private double memoryUsage;
    private double networkTrafficVolume;
    private boolean anomalyDetected;
    private String reason;
    private Instant timestamp;

    // Getters & Setters ...
}
