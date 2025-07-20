// src/main/java/edu/university/iot/model/dtoModel/LocationChangeDto.java
package edu.university.iot.model.dtoModel;

import java.time.Instant;

public class LocationChangeDto {
    private String deviceId;
    private String oldLocation;
    private String newLocation;
    private String oldIpAddress;
    private String newIpAddress;
    private Instant timestamp;

    public LocationChangeDto() {}

    public LocationChangeDto(String deviceId, String oldLocation, String newLocation,
                             String oldIpAddress, String newIpAddress, Instant timestamp) {
        this.deviceId     = deviceId;
        this.oldLocation  = oldLocation;
        this.newLocation  = newLocation;
        this.oldIpAddress = oldIpAddress;
        this.newIpAddress = newIpAddress;
        this.timestamp    = timestamp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getOldLocation() {
        return oldLocation;
    }

    public void setOldLocation(String oldLocation) {
        this.oldLocation = oldLocation;
    }

    public String getNewLocation() {
        return newLocation;
    }

    public void setNewLocation(String newLocation) {
        this.newLocation = newLocation;
    }

    public String getOldIpAddress() {
        return oldIpAddress;
    }

    public void setOldIpAddress(String oldIpAddress) {
        this.oldIpAddress = oldIpAddress;
    }

    public String getNewIpAddress() {
        return newIpAddress;
    }

    public void setNewIpAddress(String newIpAddress) {
        this.newIpAddress = newIpAddress;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    
}
