package edu.university.iot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "identity_logs")
public class IdentityLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "device_id")
    private String deviceId;
    
    @Column(name = "trusted")
    private boolean trusted;
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    // Default constructor
    public IdentityLog() {}
    
    // Constructor with all fields
    public IdentityLog(Long id, String deviceId, boolean trusted, LocalDateTime timestamp) {
        this.id = id;
        this.deviceId = deviceId;
        this.trusted = trusted;
        this.timestamp = timestamp;
    }
    
    // Constructor without ID (for new records)
    public IdentityLog(String deviceId, boolean trusted, LocalDateTime timestamp) {
        this.deviceId = deviceId;
        this.trusted = trusted;
        this.timestamp = timestamp;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    
    public boolean isTrusted() { return trusted; }
    public void setTrusted(boolean trusted) { this.trusted = trusted; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}