// src/main/java/edu/university/iot/model/dtoModel/DeviceSummaryDto.java
package edu.university.iot.model.dtoModel;

import java.time.Instant;
import java.util.Map;

public class DeviceSummaryDto {
    private String deviceId;
    private String deviceType;
    private boolean trusted;
    private double trustScore;
    private boolean quarantined;
    private Instant lastSeen;
    private String location;
    private String ipAddress;
    private String firmwareVersion;
    private boolean firmwareCompliant;
    private boolean compliant;
    private int recentAnomalies;
    private Map<String, String> trustFactors;

    // Constructors
    public DeviceSummaryDto() {}

    // Getters and setters
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public boolean isTrusted() {
        return trusted;
    }

    public void setTrusted(boolean trusted) {
        this.trusted = trusted;
    }

    public double getTrustScore() {
        return trustScore;
    }

    public void setTrustScore(double trustScore) {
        this.trustScore = trustScore;
    }

    public boolean isQuarantined() {
        return quarantined;
    }

    public void setQuarantined(boolean quarantined) {
        this.quarantined = quarantined;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Instant lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public boolean isFirmwareCompliant() {
        return firmwareCompliant;
    }

    public void setFirmwareCompliant(boolean firmwareCompliant) {
        this.firmwareCompliant = firmwareCompliant;
    }

    public boolean isCompliant() {
        return compliant;
    }

    public void setCompliant(boolean compliant) {
        this.compliant = compliant;
    }

    public int getRecentAnomalies() {
        return recentAnomalies;
    }

    public void setRecentAnomalies(int recentAnomalies) {
        this.recentAnomalies = recentAnomalies;
    }

    public Map<String, String> getTrustFactors() {
        return trustFactors;
    }

    public void setTrustFactors(Map<String, String> trustFactors) {
        this.trustFactors = trustFactors;
    }
}