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
@Table(name = "compliance_logs")
public class ComplianceLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "device_id")
    private String deviceId;
    
    @Column(name = "firmware_version")
    private String firmwareVersion;
    
    @Column(name = "compliant")
    private boolean compliant;
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    public ComplianceLog() {}
    
    public ComplianceLog(Long id, String deviceId, String firmwareVersion, boolean compliant, LocalDateTime timestamp) {
        this.id = id;
        this.deviceId = deviceId;
        this.firmwareVersion = firmwareVersion;
        this.compliant = compliant;
        this.timestamp = timestamp;
    }
    
    public ComplianceLog(String deviceId, String firmwareVersion, boolean compliant, LocalDateTime timestamp) {
        this.deviceId = deviceId;
        this.firmwareVersion = firmwareVersion;
        this.compliant = compliant;
        this.timestamp = timestamp;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getFirmwareVersion() { return firmwareVersion; }
    public void setFirmwareVersion(String firmwareVersion) { this.firmwareVersion = firmwareVersion; }
    public boolean isCompliant() { return compliant; }
    public void setCompliant(boolean compliant) { this.compliant = compliant; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}