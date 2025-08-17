package edu.university.iot.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

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
    @Embedded
    private CoordinateData coordinates;

    private int suspiciousActivityScore;
    private int consecutiveAnomalies;
    public CoordinateData getCoordinates() {
        return coordinates;
    }


    public void setCoordinates(CoordinateData coordinates) {
        this.coordinates = coordinates;
    }

    public int getSuspiciousActivityScore() {
        return suspiciousActivityScore;
    }

    public void setSuspiciousActivityScore(int suspiciousActivityScore) {
        this.suspiciousActivityScore = suspiciousActivityScore;
    }

    public int getConsecutiveAnomalies() {
        return consecutiveAnomalies;
    }

    public void setConsecutiveAnomalies(int consecutiveAnomalies) {
        this.consecutiveAnomalies = consecutiveAnomalies;
    }

    public String getDeviceProfile() {
        return deviceProfile;
    }

    public void setDeviceProfile(String deviceProfile) {
        this.deviceProfile = deviceProfile;
    }

    private String deviceProfile;

    // Getters and setters omitted here for brevity
    // (Include all your existing getter/setter methods)

    /**
     * Converts this DeviceMessage into a Map<String, Object> with all its fields.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("deviceId", deviceId);
        map.put("certificateValid", certificateValid);
        map.put("patchStatus", patchStatus);
        map.put("firmwareVersion", firmwareVersion);
        map.put("ipAddress", ipAddress);
        map.put("location", location);
        map.put("cpuUsage", cpuUsage);
        map.put("memoryUsage", memoryUsage);
        map.put("networkTrafficVolume", networkTrafficVolume);
        map.put("anomalyScore", anomalyScore);
        map.put("malwareSignatureDetected", malwareSignatureDetected);
        map.put("sessionDuration", sessionDuration);
        map.put("timestamp", timestamp);
        return map;
    }

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
}
