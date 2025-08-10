package edu.university.iot.model.dtoModel;

import java.time.Instant;

public class DeviceLocationDto {
    private String deviceId;
    private String currentLocation;
    private String currentIpAddress;
    private Double latitude;
    private Double longitude;
    private String locationType;
    private String riskLevel;
    private Double trustScore;
    private Double anomalyScore;
    private Integer suspiciousActivityScore;
    private Instant lastUpdate;

    // Constructors
    public DeviceLocationDto() {}

    // Getters and Setters
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(String currentLocation) { this.currentLocation = currentLocation; }

    public String getCurrentIpAddress() { return currentIpAddress; }
    public void setCurrentIpAddress(String currentIpAddress) { this.currentIpAddress = currentIpAddress; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getLocationType() { return locationType; }
    public void setLocationType(String locationType) { this.locationType = locationType; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public Double getTrustScore() { return trustScore; }
    public void setTrustScore(Double trustScore) { this.trustScore = trustScore; }

    public Double getAnomalyScore() { return anomalyScore; }
    public void setAnomalyScore(Double anomalyScore) { this.anomalyScore = anomalyScore; }

    public Integer getSuspiciousActivityScore() { return suspiciousActivityScore; }
    public void setSuspiciousActivityScore(Integer suspiciousActivityScore) { this.suspiciousActivityScore = suspiciousActivityScore; }

    public Instant getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(Instant lastUpdate) { this.lastUpdate = lastUpdate; }
}