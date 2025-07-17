package edu.university.iot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "location_network_changes")
public class LocationNetworkChange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "old_location")
    private String oldLocation;

    @Column(name = "new_location")
    private String newLocation;

    @Column(name = "old_ip_address")
    private String oldIpAddress;

    @Column(name = "new_ip_address")
    private String newIpAddress;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    
}