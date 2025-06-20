// src/main/java/edu/university/iot/service/TelemetryProcessorService.java
package edu.university.iot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TelemetryProcessorService {

    @Autowired
    private IdentityVerificationService identityVerificationService;

    @Autowired
    private FirmwareService firmwareService;

    @Autowired
    private AnomalyDetectorService anomalyDetectionService;

    @Autowired
    private ComplianceService complianceService;

    public void process(Map<String, Object> telemetry) {
        identityVerificationService.verifyIdentity(telemetry);
        firmwareService.validateAndLogFirmware(telemetry);
        anomalyDetectionService.checkAnomaly(telemetry);
        complianceService.evaluateCompliance(telemetry);
    }
}
