package edu.university.iot.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_messages")
public class DeviceMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "device_id")
    @JsonProperty("deviceId")
    private String deviceId;
    
    @Column(name = "firmware_version")
    @JsonProperty("firmwareVersion")
    private String firmwareVersion;
    
    @Column(name = "temperature")
    @JsonProperty("temperature")
    private Double temperature;
    
    @Column(name = "timestamp")
    @JsonProperty("timestamp")
    // Solution 1: Use ISO format with optional milliseconds and timezone
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][X]")
    private LocalDateTime timestamp;
    
    // Default constructor
    public DeviceMessage() {}
    
    // Constructor for JSON deserialization
    @JsonCreator
    public DeviceMessage(@JsonProperty("deviceId") String deviceId,
                        @JsonProperty("firmwareVersion") String firmwareVersion,
                        @JsonProperty("temperature") Double temperature,
                        @JsonProperty("timestamp") LocalDateTime timestamp) {
        this.deviceId = deviceId;
        this.firmwareVersion = firmwareVersion;
        this.temperature = temperature;
        this.timestamp = timestamp;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    
    public String getFirmwareVersion() { return firmwareVersion; }
    public void setFirmwareVersion(String firmwareVersion) { this.firmwareVersion = firmwareVersion; }
    
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}