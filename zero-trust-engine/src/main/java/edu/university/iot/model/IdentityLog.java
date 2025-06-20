// File: src/main/java/edu/university/iot/entity/IdentityLog.java
package edu.university.iot.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class IdentityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String deviceId;
    private boolean certificateValid;
    private boolean identityVerified;
    private Instant timestamp;

    public IdentityLog() {}

    public IdentityLog(String deviceId, boolean certificateValid, boolean identityVerified, Instant timestamp) {
        this.deviceId = deviceId;
        this.certificateValid = certificateValid;
        this.identityVerified = identityVerified;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public boolean isCertificateValid() { return certificateValid; }
    public void setCertificateValid(boolean certificateValid) { this.certificateValid = certificateValid; }

    public boolean isIdentityVerified() { return identityVerified; }
    public void setIdentityVerified(boolean identityVerified) { this.identityVerified = identityVerified; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
