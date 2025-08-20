package edu.university.iot.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "device_registry")
public class DeviceRegistry {

    @Id
    @Column(name = "device_id")
    private String deviceId;

    // Identity Verification
    @Column(name = "trusted")
    private boolean trusted;

    @Column(name = "certificate_required")
    private boolean certificateRequired;

    @Column(name = "certificate_valid")
    private boolean certificateValid = true; // Added missing field

    @Column(name = "firmware_valid")
    private boolean firmwareValid = true; // Added missing field

    // Firmware/Compliance
    @Column(name = "expected_firmware_version")
    private String expectedFirmwareVersion;

    @Column(name = "expected_patch_status")
    private String expectedPatchStatus;

    @Column(name = "allow_outdated_patch")
    private boolean allowOutdatedPatch;

    // Anomaly Detection Thresholds (optional, can be null = use default)
    @Column(name = "max_cpu_usage")
    private Double maxCpuUsage;

    @Column(name = "max_memory_usage")
    private Double maxMemoryUsage;

    @Column(name = "max_network_traffic")
    private Double maxNetworkTraffic;

    // Trust Score (optional, for analytics)
    @Column(name = "trust_score")
    private Double trustScore;

    // Quarantine fields
    @Column(name = "quarantined", nullable = false)
    private boolean quarantined = false;

    @Column(name = "quarantine_reason")
    private String quarantineReason;

    @Column(name = "quarantine_timestamp")
    private LocalDateTime quarantineTimestamp;

    // Getters and Setters
    public String getDeviceId() {
        return deviceId;
    }
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isTrusted() {
        return trusted;
    }
    public void setTrusted(boolean trusted) {
        this.trusted = trusted;
    }

    public boolean isCertificateRequired() {
        return certificateRequired;
    }
    public void setCertificateRequired(boolean certificateRequired) {
        this.certificateRequired = certificateRequired;
    }

    // Added missing getters/setters
    public boolean isCertificateValid() {
        return certificateValid;
    }
    public void setCertificateValid(boolean certificateValid) {
        this.certificateValid = certificateValid;
    }

    public boolean isFirmwareValid() {
        return firmwareValid;
    }
    public void setFirmwareValid(boolean firmwareValid) {
        this.firmwareValid = firmwareValid;
    }

    public String getExpectedFirmwareVersion() {
        return expectedFirmwareVersion;
    }
    public void setExpectedFirmwareVersion(String expectedFirmwareVersion) {
        this.expectedFirmwareVersion = expectedFirmwareVersion;
    }

    public String getExpectedPatchStatus() {
        return expectedPatchStatus;
    }
    public void setExpectedPatchStatus(String expectedPatchStatus) {
        this.expectedPatchStatus = expectedPatchStatus;
    }

    public boolean isAllowOutdatedPatch() {
        return allowOutdatedPatch;
    }
    public void setAllowOutdatedPatch(boolean allowOutdatedPatch) {
        this.allowOutdatedPatch = allowOutdatedPatch;
    }

    public Double getMaxCpuUsage() {
        return maxCpuUsage;
    }
    public void setMaxCpuUsage(Double maxCpuUsage) {
        this.maxCpuUsage = maxCpuUsage;
    }

    public Double getMaxMemoryUsage() {
        return maxMemoryUsage;
    }
    public void setMaxMemoryUsage(Double maxMemoryUsage) {
        this.maxMemoryUsage = maxMemoryUsage;
    }

    public Double getMaxNetworkTraffic() {
        return maxNetworkTraffic;
    }
    public void setMaxNetworkTraffic(Double maxNetworkTraffic) {
        this.maxNetworkTraffic = maxNetworkTraffic;
    }

    public Double getTrustScore() {
        return trustScore;
    }
    public void setTrustScore(Double trustScore) {
        this.trustScore = trustScore;
    }

    public boolean isQuarantined() {
        return quarantined;
    }
    public void setQuarantined(boolean quarantined) {
        this.quarantined = quarantined;
    }

    public String getQuarantineReason() {
        return quarantineReason;
    }
    public void setQuarantineReason(String quarantineReason) {
        this.quarantineReason = quarantineReason;
    }

    public LocalDateTime getQuarantineTimestamp() {
        return quarantineTimestamp;
    }
    public void setQuarantineTimestamp(LocalDateTime quarantineTimestamp) {
        this.quarantineTimestamp = quarantineTimestamp;
    }
}