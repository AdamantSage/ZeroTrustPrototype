package edu.university.iot.model;

import java.time.Instant;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "anomaly_logs")
public class AnomalyLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "device_id")
    private String deviceId;
    
    @Column(name = "temperature")
    private Double temperature;
    
    @Column(name = "anomaly")
    private boolean anomaly;
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    public AnomalyLog() {}
    
    public AnomalyLog(Long id, String deviceId, Double temperature, boolean anomaly, LocalDateTime timestamp) {
        this.id = id;
        this.deviceId = deviceId;
        this.temperature = temperature;
        this.anomaly = anomaly;
        this.timestamp = timestamp;
    }
    
    public AnomalyLog(String deviceId, Double temperature, boolean anomaly, LocalDateTime timestamp) {
        this.deviceId = deviceId;
        this.temperature = temperature;
        this.anomaly = anomaly;
        this.timestamp = timestamp;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    public boolean isAnomaly() { return anomaly; }
    public void setAnomaly(boolean anomaly) { this.anomaly = anomaly; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}