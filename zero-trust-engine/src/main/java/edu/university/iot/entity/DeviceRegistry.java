// src/main/java/edu/university/iot/entity/DeviceRegistry.java
package edu.university.iot.entity;

import jakarta.persistence.*;

@Entity
public class DeviceRegistry {

    @Id
    private String deviceId;

    // Identity Verification
    private boolean trusted;
    private boolean certificateRequired;

    // Firmware/Compliance
    private String expectedFirmwareVersion;
    private String expectedPatchStatus;
    private boolean allowOutdatedPatch;

    // Anomaly Detection Thresholds (optional, can be null = use default)
    private Double maxCpuUsage;
    private Double maxMemoryUsage;
    private Double maxNetworkTraffic;

    // Trust Score (optional, for analytics)
    private int trustScore;

    // Getters & Setters
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public boolean isTrusted() { return trusted; }
    public void setTrusted(boolean trusted) { this.trusted = trusted; }

    public boolean isCertificateRequired() { return certificateRequired; }
    public void setCertificateRequired(boolean certificateRequired) { this.certificateRequired = certificateRequired; }

    public String getExpectedFirmwareVersion() { return expectedFirmwareVersion; }
    public void setExpectedFirmwareVersion(String expectedFirmwareVersion) { this.expectedFirmwareVersion = expectedFirmwareVersion; }

    public String getExpectedPatchStatus() { return expectedPatchStatus; }
    public void setExpectedPatchStatus(String expectedPatchStatus) { this.expectedPatchStatus = expectedPatchStatus; }

    public boolean isAllowOutdatedPatch() { return allowOutdatedPatch; }
    public void setAllowOutdatedPatch(boolean allowOutdatedPatch) { this.allowOutdatedPatch = allowOutdatedPatch; }

    public Double getMaxCpuUsage() { return maxCpuUsage; }
    public void setMaxCpuUsage(Double maxCpuUsage) { this.maxCpuUsage = maxCpuUsage; }

    public Double getMaxMemoryUsage() { return maxMemoryUsage; }
    public void setMaxMemoryUsage(Double maxMemoryUsage) { this.maxMemoryUsage = maxMemoryUsage; }

    public Double getMaxNetworkTraffic() { return maxNetworkTraffic; }
    public void setMaxNetworkTraffic(Double maxNetworkTraffic) { this.maxNetworkTraffic = maxNetworkTraffic; }

    public int getTrustScore() { return trustScore; }
    public void setTrustScore(int trustScore) { this.trustScore = trustScore; }
}
