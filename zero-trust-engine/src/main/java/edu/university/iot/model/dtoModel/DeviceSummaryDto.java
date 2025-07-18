// src/main/java/edu/university/iot/dto/DeviceDto.java
package edu.university.iot.model.dtoModel;

public class DeviceDto {
    private String deviceId;
    private boolean trusted;
    private double trustScore;
    private boolean quarantined;

    public DeviceDto() {}

    public DeviceDto(String deviceId, boolean trusted, double trustScore, boolean quarantined) {
        this.deviceId    = deviceId;
        this.trusted     = trusted;
        this.trustScore  = trustScore;
        this.quarantined = quarantined;
    }

    // getters & setters
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public boolean isTrusted() { return trusted; }
    public void setTrusted(boolean trusted) { this.trusted = trusted; }

    public double getTrustScore() { return trustScore; }
    public void setTrustScore(double trustScore) { this.trustScore = trustScore; }

    public boolean isQuarantined() { return quarantined; }
    public void setQuarantined(boolean quarantined) { this.quarantined = quarantined; }
}
