// src/main/java/edu/university/iot/entity/DeviceMessage.java
package edu.university.iot.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class DeviceMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deviceId;
    private boolean certificateValid;
    private String patchStatus;
    private String firmwareVersion;
    private String ipAddress;
    private String location;
    private double cpuUsage;
    private double memoryUsage;
    private double networkTrafficVolume;
    private double anomalyScore;
    private boolean malwareSignatureDetected;
    private int sessionDuration;
    private Instant timestamp;
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
    public boolean isCertificateValid() {
        return certificateValid;
    }
    public void setCertificateValid(boolean certificateValid) {
        this.certificateValid = certificateValid;
    }
    public String getPatchStatus() {
        return patchStatus;
    }
    public void setPatchStatus(String patchStatus) {
        this.patchStatus = patchStatus;
    }
    public String getFirmwareVersion() {
        return firmwareVersion;
    }
    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }
    public String getIpAddress() {
        return ipAddress;
    }
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
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
    public double getAnomalyScore() {
        return anomalyScore;
    }
    public void setAnomalyScore(double anomalyScore) {
        this.anomalyScore = anomalyScore;
    }
    public boolean isMalwareSignatureDetected() {
        return malwareSignatureDetected;
    }
    public void setMalwareSignatureDetected(boolean malwareSignatureDetected) {
        this.malwareSignatureDetected = malwareSignatureDetected;
    }
    public int getSessionDuration() {
        return sessionDuration;
    }
    public void setSessionDuration(int sessionDuration) {
        this.sessionDuration = sessionDuration;
    }
    public Instant getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    // Getters and setters omitted for brevity (generate these with your IDE)
    // ... 
}
