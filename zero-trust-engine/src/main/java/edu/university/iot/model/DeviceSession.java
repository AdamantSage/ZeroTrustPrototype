package edu.university.iot.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "device_sessions")
public class DeviceSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "session_id", nullable = false, unique = true)
    private String sessionId;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "last_activity_time", nullable = false)
    private Instant lastActivityTime;

    @Column(name = "status", nullable = false)
    private String status; // ACTIVE, PAUSED, TERMINATED

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }

    public Instant getLastActivityTime() { return lastActivityTime; }
    public void setLastActivityTime(Instant lastActivityTime) { this.lastActivityTime = lastActivityTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
