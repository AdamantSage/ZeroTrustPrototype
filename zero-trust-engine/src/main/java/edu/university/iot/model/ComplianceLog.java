// src/main/java/edu/university/iot/entity/ComplianceLog.java
package edu.university.iot.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class ComplianceLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    public boolean isCompliant() {
        return compliant;
    }
    public void setCompliant(boolean compliant) {
        this.compliant = compliant;
    }
    public String getViolations() {
        return violations;
    }
    public void setViolations(String violations) {
        this.violations = violations;
    }
    public Instant getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    private String deviceId;
    private boolean compliant;
    private String violations;
    private Instant timestamp;

    // Getters & Setters ...
}
