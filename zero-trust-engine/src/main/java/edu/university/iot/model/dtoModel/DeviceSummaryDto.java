// src/main/java/edu/university/iot/model/dtoModel/DeviceSummaryDto.java
package edu.university.iot.model.dtoModel;

import java.time.Instant;

public class DeviceSummaryDto {
    private String deviceId;
    private Instant lastSeen;
    private String location;
    private String ipAddress;
    private boolean trusted;
    private double trustScore;
    private boolean quarantined;

    public DeviceSummaryDto() {}

    public DeviceSummaryDto(String deviceId,
                            Instant lastSeen,
                            String location,
                            String ipAddress,
                            boolean trusted,
                            double trustScore,
                            boolean quarantined) {
        this.deviceId    = deviceId;
        this.lastSeen    = lastSeen;
        this.location    = location;
        this.ipAddress   = ipAddress;
        this.trusted     = trusted;
        this.trustScore  = trustScore;
        this.quarantined = quarantined;
    }

    // --- Getters & Setters ---

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public Instant getLastSeen() { return lastSeen; }
    public void setLastSeen(Instant lastSeen) { this.lastSeen = lastSeen; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public boolean isTrusted() { return trusted; }
    public void setTrusted(boolean trusted) { this.trusted = trusted; }

    public double getTrustScore() { return trustScore; }
    public void setTrustScore(double trustScore) { this.trustScore = trustScore; }

    public boolean isQuarantined() { return quarantined; }
    public void setQuarantined(boolean quarantined) { this.quarantined = quarantined; }
}
