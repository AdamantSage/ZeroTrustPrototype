package edu.university.iot.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "device_registry")
public class DeviceRegistry {

    @Id
    @Column(name = "device_id")
    private String deviceId;

    // Identity Verification - Using Boolean wrapper types to handle null values
    @Column(name = "trusted")
    private Boolean trusted;

    @Column(name = "certificate_required")
    private Boolean certificateRequired;

    @Column(name = "certificate_valid")
    private Boolean certificateValid = true;

    @Column(name = "firmware_valid")
    private Boolean firmwareValid = true;

    // Firmware/Compliance
    @Column(name = "expected_firmware_version")
    private String expectedFirmwareVersion;

    @Column(name = "expected_patch_status")
    private String expectedPatchStatus;

    @Column(name = "allow_outdated_patch")
    private Boolean allowOutdatedPatch;

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
    @Column(name = "quarantined")
    private Boolean quarantined = false;

    @Column(name = "quarantine_reason")
    private String quarantineReason;

    @Column(name = "quarantine_timestamp")
    private LocalDateTime quarantineTimestamp;

    // Default constructor
    public DeviceRegistry() {}

    // Constructor with device ID
    public DeviceRegistry(String deviceId) {
        this.deviceId = deviceId;
    }

    // Getters and Setters
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Boolean getTrusted() {
        return trusted;
    }
    
    public void setTrusted(Boolean trusted) {
        this.trusted = trusted;
    }

    // Convenience method for boolean check
    public boolean isTrusted() {
        return trusted != null && trusted;
    }

    public Boolean getCertificateRequired() {
        return certificateRequired;
    }
    
    public void setCertificateRequired(Boolean certificateRequired) {
        this.certificateRequired = certificateRequired;
    }

    // Convenience method for boolean check
    public boolean isCertificateRequired() {
        return certificateRequired != null && certificateRequired;
    }

    public Boolean getCertificateValid() {
        return certificateValid;
    }
    
    public void setCertificateValid(Boolean certificateValid) {
        this.certificateValid = certificateValid;
    }

    // Convenience method for boolean check
    public boolean isCertificateValid() {
        return certificateValid != null ? certificateValid : true;
    }

    public Boolean getFirmwareValid() {
        return firmwareValid;
    }
    
    public void setFirmwareValid(Boolean firmwareValid) {
        this.firmwareValid = firmwareValid;
    }

    // Convenience method for boolean check
    public boolean isFirmwareValid() {
        return firmwareValid != null ? firmwareValid : true;
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

    public Boolean getAllowOutdatedPatch() {
        return allowOutdatedPatch;
    }
    
    public void setAllowOutdatedPatch(Boolean allowOutdatedPatch) {
        this.allowOutdatedPatch = allowOutdatedPatch;
    }

    // Convenience method for boolean check
    public boolean isAllowOutdatedPatch() {
        return allowOutdatedPatch != null && allowOutdatedPatch;
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

    public Boolean getQuarantined() {
        return quarantined;
    }
    
    public void setQuarantined(Boolean quarantined) {
        this.quarantined = quarantined;
    }

    // Convenience method for boolean check
    public boolean isQuarantined() {
        return quarantined != null && quarantined;
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

    // Utility methods for quarantine management
    public void quarantine(String reason) {
        this.quarantined = true;
        this.quarantineReason = reason;
        this.quarantineTimestamp = LocalDateTime.now();
    }

    public void removeFromQuarantine() {
        this.quarantined = false;
        this.quarantineReason = null;
        this.quarantineTimestamp = null;
    }

    @Override
    public String toString() {
        return "DeviceRegistry{" +
                "deviceId='" + deviceId + '\'' +
                ", trusted=" + trusted +
                ", certificateRequired=" + certificateRequired +
                ", certificateValid=" + certificateValid +
                ", firmwareValid=" + firmwareValid +
                ", quarantined=" + quarantined +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceRegistry that = (DeviceRegistry) o;
        return deviceId != null ? deviceId.equals(that.deviceId) : that.deviceId == null;
    }

    @Override
    public int hashCode() {
        return deviceId != null ? deviceId.hashCode() : 0;
    }
}