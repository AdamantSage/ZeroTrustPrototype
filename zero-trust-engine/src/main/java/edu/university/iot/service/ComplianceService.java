// src/main/java/edu/university/iot/service/ComplianceService.java
package edu.university.iot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.university.iot.config.ComplianceConfig;

@Service
public class ComplianceService {
    private static final Logger logger = LoggerFactory.getLogger(ComplianceService.class);
    private final String minVersion;

    @Autowired
    public ComplianceService(ComplianceConfig cfg) {
        this.minVersion = cfg.getMinFirmwareVersion();
        logger.info("ComplianceService initialized with minVersion {}", minVersion);
    }

    /** returns true if the device's firmwareVersion ≥ configured minimum */
       public boolean isCompliant(String firmwareVersion) {
        boolean compliant = firmwareVersion.compareTo(minVersion) >= 0;
        logger.info("Firmware version check: Reported [{}], Required ≥ [{}] → Compliant: {}", 
                    firmwareVersion, minVersion, compliant);
        return compliant;
    }
}
