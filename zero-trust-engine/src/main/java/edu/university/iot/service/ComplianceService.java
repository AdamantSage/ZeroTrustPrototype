// src/main/java/edu/university/iot/service/ComplianceService.java
package edu.university.iot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.university.iot.config.ComplianceConfig;

@Service
public class ComplianceService {
    private final String minVersion;

    @Autowired
    public ComplianceService(ComplianceConfig cfg) {
        this.minVersion = cfg.getMinFirmwareVersion();
    }

    /** returns true if the device's firmwareVersion ≥ configured minimum */
    public boolean isCompliant(String firmwareVersion) {
        return firmwareVersion.compareTo(minVersion) >= 0;
    }
}
