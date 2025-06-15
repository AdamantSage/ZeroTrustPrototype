// src/main/java/edu/university/iot/config/ComplianceConfig.java
package edu.university.iot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "compliance")
public class ComplianceConfig {
    /**
     * Automatically bound from 'compliance.minFirmwareVersion'
     */
    private String minFirmwareVersion;
    public String getMinFirmwareVersion() { return minFirmwareVersion; }
    public void setMinFirmwareVersion(String v) { this.minFirmwareVersion = v; }
}
