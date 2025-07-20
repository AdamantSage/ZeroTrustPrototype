// src/main/java/edu/university/iot/model/dtoModel/QuarantineLogDto.java
package edu.university.iot.model.dtoModel;

import java.time.Instant;

public class QuarantineLogDto {
    private String deviceId;
    private String reason;
    private String status;
    private Instant timestamp;
    private String errorMessage;

    public QuarantineLogDto() {}

    public QuarantineLogDto(String deviceId, String reason, String status,
                             Instant timestamp, String errorMessage) {
        this.deviceId     = deviceId;
        this.reason       = reason;
        this.status       = status;
        this.timestamp    = timestamp;
        this.errorMessage = errorMessage;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
