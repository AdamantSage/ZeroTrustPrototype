package edu.university.iot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class DeviceRegistry {
  @Id
  private String deviceId;
  private boolean registered;
  private String firmwareVersion;
  // getters/setters
  public String getDeviceId() {
    return deviceId;
  }
  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }
  public boolean isRegistered() {
    return registered;
  }
  public void setRegistered(boolean registered) {
    this.registered = registered;
  }
  public String getFirmwareVersion() {
    return firmwareVersion;
  }
  public void setFirmwareVersion(String firmwareVersion) {
    this.firmwareVersion = firmwareVersion;
  }
}
