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

    // Added coordinate fields for enhanced location tracking
    @Column(name = "old_latitude")
    private Double oldLatitude;

    @Column(name = "old_longitude")
    private Double oldLongitude;

    @Column(name = "new_latitude")
    private Double newLatitude;

    @Column(name = "new_longitude")
    private Double newLongitude;

    // Constructors
    public LocationNetworkChange() {}

    public LocationNetworkChange(String deviceId, String oldLocation, String newLocation,
                                String oldIpAddress, String newIpAddress, LocalDateTime timestamp) {
        this.deviceId = deviceId;
        this.oldLocation = oldLocation;
        this.newLocation = newLocation;
        this.oldIpAddress = oldIpAddress;
        this.newIpAddress = newIpAddress;
        this.timestamp = timestamp;
    }

    // Existing getters and setters
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

    // New coordinate getters and setters
    public Double getOldLatitude() {
        return oldLatitude;
    }

    public void setOldLatitude(Double oldLatitude) {
        this.oldLatitude = oldLatitude;
    }

    public Double getOldLongitude() {
        return oldLongitude;
    }

    public void setOldLongitude(Double oldLongitude) {
        this.oldLongitude = oldLongitude;
    }

    public Double getNewLatitude() {
        return newLatitude;
    }

    public void setNewLatitude(Double newLatitude) {
        this.newLatitude = newLatitude;
    }

    public Double getNewLongitude() {
        return newLongitude;
    }

    public void setNewLongitude(Double newLongitude) {
        this.newLongitude = newLongitude;
    }

    @Override
    public String toString() {
        return "LocationNetworkChange{" +
                "id=" + id +
                ", deviceId='" + deviceId + '\'' +
                ", oldLocation='" + oldLocation + '\'' +
                ", newLocation='" + newLocation + '\'' +
                ", oldIpAddress='" + oldIpAddress + '\'' +
                ", newIpAddress='" + newIpAddress + '\'' +
                ", timestamp=" + timestamp +
                ", oldLatitude=" + oldLatitude +
                ", oldLongitude=" + oldLongitude +
                ", newLatitude=" + newLatitude +
                ", newLongitude=" + newLongitude +
                '}';
    }
}