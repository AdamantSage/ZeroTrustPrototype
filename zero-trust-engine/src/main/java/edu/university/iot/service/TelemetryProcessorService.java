// src/main/java/edu/university/iot/service/TelemetryProcessorService.java
package edu.university.iot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TelemetryProcessorService {

    @Autowired
    private FirmwareService firmwareValidationService;

    @Autowired
    private AnomalyDetectorService anomalyDetectionService;

    @Autowired
    private ComplianceService complianceService;

    public void process(Map<String, Object> telemetry) {
        firmwareValidationService.validateFirmware(telemetry);
        anomalyDetectionService.checkAnomaly(telemetry);
        complianceService.evaluateCompliance(telemetry);
    }
}
