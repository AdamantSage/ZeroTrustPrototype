// src/main/java/edu/university/iot/model/dtoModel/FirmwareLogDto.java
package edu.university.iot.model.dtoModel;

import java.time.Instant;

public class FirmwareLogDto {
    private String deviceId;
    private String reportedFirmwareVersion;
    private String expectedFirmwareVersion;
    private String reportedPatchStatus;
    private boolean firmwareValid;
    private Instant timestamp;

    public FirmwareLogDto() {}

    public FirmwareLogDto(String deviceId,
                          String reportedFirmwareVersion,
                          String expectedFirmwareVersion,
                          String reportedPatchStatus,
                          boolean firmwareValid,
                          Instant timestamp) {
        this.deviceId                = deviceId;
        this.reportedFirmwareVersion = reportedFirmwareVersion;
        this.expectedFirmwareVersion = expectedFirmwareVersion;
        this.reportedPatchStatus     = reportedPatchStatus;
        this.firmwareValid           = firmwareValid;
        this.timestamp               = timestamp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getReportedFirmwareVersion() {
        return reportedFirmwareVersion;
    }

    public void setReportedFirmwareVersion(String reportedFirmwareVersion) {
        this.reportedFirmwareVersion = reportedFirmwareVersion;
    }

    public String getExpectedFirmwareVersion() {
        return expectedFirmwareVersion;
    }

    public void setExpectedFirmwareVersion(String expectedFirmwareVersion) {
        this.expectedFirmwareVersion = expectedFirmwareVersion;
    }

    public String getReportedPatchStatus() {
        return reportedPatchStatus;
    }

    public void setReportedPatchStatus(String reportedPatchStatus) {
        this.reportedPatchStatus = reportedPatchStatus;
    }

    public boolean isFirmwareValid() {
        return firmwareValid;
    }

    public void setFirmwareValid(boolean firmwareValid) {
        this.firmwareValid = firmwareValid;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

   
}
