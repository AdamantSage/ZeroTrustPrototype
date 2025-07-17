package edu.university.iot.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "quarantine_log")
public class QuarantineLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "error_message")
    private String errorMessage;

    public enum Status {
        PENDING,
        SUCCESS,
        FAILED,
        ALREADY_QUARANTINED,
        RECREATED
    }

    // Constructors
    public QuarantineLog() {}

    public QuarantineLog(String deviceId, String reason, Status status) {
        this.deviceId = deviceId;
        this.reason = reason;
        this.status = status;
        this.timestamp = Instant.now();
    }

    // Getters and Setters
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "QuarantineLog{" +
                "id=" + id +
                ", deviceId='" + deviceId + '\'' +
                ", reason='" + reason + '\'' +
                ", timestamp=" + timestamp +
                ", status=" + status +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}