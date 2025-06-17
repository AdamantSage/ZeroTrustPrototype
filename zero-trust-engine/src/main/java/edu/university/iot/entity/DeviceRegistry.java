// src/main/java/edu/university/iot/entity/DeviceRegistry.java
package edu.university.iot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "device_registry")
public class DeviceRegistry {
    
    @Id
    @Column(name = "device_id", length = 64)
    private String deviceId;
    
    @Column(name = "firmware_version", length = 32)
    private String firmwareVersion;
    
    @Column(name = "registration_date")
    private Instant registrationDate;
    
    @Column(name = "last_update_date")
    private Instant lastUpdateDate;
    
    @Column(name = "status", length = 32)
    private String status;
    
    @Column(name = "device_type", length = 64)
    private String deviceType;
    
    @Column(name = "description", length = 255)
    private String description;
    
    public DeviceRegistry() {}
    
    public DeviceRegistry(String deviceId, String firmwareVersion, String status) {
        this.deviceId = deviceId;
        this.firmwareVersion = firmwareVersion;
        this.status = status;
        this.registrationDate = Instant.now();
    }
    
    // Getters and Setters
    public String getDeviceId() {
        return deviceId;
    }
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    public String getFirmwareVersion() {
        return firmwareVersion;
    }
    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }
    public Instant getRegistrationDate() {
        return registrationDate;
    }
    public void setRegistrationDate(Instant registrationDate) {
        this.registrationDate = registrationDate;
    }
    public Instant getLastUpdateDate() {
        return lastUpdateDate;
    }
    public void setLastUpdateDate(Instant lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getDeviceType() {
        return deviceType;
    }
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}