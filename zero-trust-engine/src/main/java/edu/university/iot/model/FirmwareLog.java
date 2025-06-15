package edu.university.iot.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "firmware_logs")
public class FirmwareLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "device_id")
    private String deviceId;
    
    @Column(name = "firmware_version")
    private String firmwareVersion;
    
    @Column(name = "valid")
    private boolean valid;
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    public FirmwareLog() {}
    
    public FirmwareLog(Long id, String deviceId, String firmwareVersion, boolean valid, LocalDateTime timestamp) {
        this.id = id;
        this.deviceId = deviceId;
        this.firmwareVersion = firmwareVersion;
        this.valid = valid;
        this.timestamp = timestamp;
    }
    
    public FirmwareLog(String deviceId, String firmwareVersion, boolean valid, LocalDateTime timestamp) {
        this.deviceId = deviceId;
        this.firmwareVersion = firmwareVersion;
        this.valid = valid;
        this.timestamp = timestamp;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getFirmwareVersion() { return firmwareVersion; }
    public void setFirmwareVersion(String firmwareVersion) { this.firmwareVersion = firmwareVersion; }
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}