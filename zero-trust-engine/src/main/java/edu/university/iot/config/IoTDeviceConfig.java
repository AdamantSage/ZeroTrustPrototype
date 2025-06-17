package edu.university.iot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "iot.device")
public class IoTDeviceConfig {
    
    private boolean autoRegisterDevices = true;
    private String defaultDeviceType = "IOT_SENSOR";
    private String defaultStatus = "ACTIVE";
    private boolean updateFirmwareVersion = true;
    private int maxRetryAttempts = 3;
    
    // Getters and Setters
    public boolean isAutoRegisterDevices() {
        return autoRegisterDevices;
    }
    
    public void setAutoRegisterDevices(boolean autoRegisterDevices) {
        this.autoRegisterDevices = autoRegisterDevices;
    }
    
    public String getDefaultDeviceType() {
        return defaultDeviceType;
    }
    
    public void setDefaultDeviceType(String defaultDeviceType) {
        this.defaultDeviceType = defaultDeviceType;
    }
    
    public String getDefaultStatus() {
        return defaultStatus;
    }
    
    public void setDefaultStatus(String defaultStatus) {
        this.defaultStatus = defaultStatus;
    }
    
    public boolean isUpdateFirmwareVersion() {
        return updateFirmwareVersion;
    }
    
    public void setUpdateFirmwareVersion(boolean updateFirmwareVersion) {
        this.updateFirmwareVersion = updateFirmwareVersion;
    }
    
    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }
    
    public void setMaxRetryAttempts(int maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;
    }
}