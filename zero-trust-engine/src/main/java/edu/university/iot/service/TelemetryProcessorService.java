package edu.university.iot.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

import edu.university.iot.entity.DeviceRegistry;
import edu.university.iot.repository.DeviceRegistryRepository;

@Service
public class TelemetryProcessorService {

    private final LocationNetworkChangeService contextService;
    private final SessionManagementService sessionService;
    private final IdentityVerificationService identityService;
    private final FirmwareService firmwareService;
    private final AnomalyDetectorService anomalyService;
    private final ComplianceService complianceService;
    private final TrustScoreService trustService;
    private final QuarantineService quarantineService;
    private final DeviceRegistryRepository registryRepo;

    public TelemetryProcessorService(
        LocationNetworkChangeService contextService,
        SessionManagementService sessionService,
        IdentityVerificationService identityService,
        FirmwareService firmwareService,
        AnomalyDetectorService anomalyService,
        ComplianceService complianceService,
        TrustScoreService trustService,
        QuarantineService quarantineService,
        DeviceRegistryRepository registryRepo) {

        this.contextService = contextService;
        this.sessionService = sessionService;
        this.identityService = identityService;
        this.firmwareService = firmwareService;
        this.anomalyService = anomalyService;
        this.complianceService = complianceService;
        this.trustService = trustService;
        this.quarantineService = quarantineService;
        this.registryRepo = registryRepo;
    }

    @Transactional
    public void process(Map<String, Object> telemetry) {
        String deviceId = (String) telemetry.get("deviceId");

        // 1) Start or refresh session
        String sessionId = sessionService.startOrRefreshSession(deviceId);

        DeviceRegistry device = registryRepo.findById(deviceId)
            .orElseThrow(() -> new IllegalStateException("Unknown device: " + deviceId));

        // 2) If already quarantined, terminate session and skip
        if (device.isQuarantined()) {
            sessionService.terminateSession(sessionId, "Device already quarantined");
            return;
        }

        // 3) Context check via LocationNetworkChangeService
        boolean contextUnchanged = contextService.validateContext(telemetry);
        if (!contextUnchanged) {
            sessionService.pauseSession(sessionId, "Location/IP change detected");
        }

        // 4) Identity + context
        boolean identityOk = identityService.verifyIdentity(telemetry) && contextUnchanged;

        // 5) Firmware & patch
        boolean firmwareOk = firmwareService.validateAndLogFirmware(telemetry);

        // 6) Anomaly detection
        boolean anomalyDetected = anomalyService.checkAnomaly(telemetry);

        // 7) Compliance rules
        boolean compliant = complianceService.evaluateCompliance(telemetry);

        // 8) Adjust trust score
        trustService.adjustTrust(deviceId, identityOk,contextUnchanged, firmwareOk, anomalyDetected, compliant);

        // 9) Reload device to get updated trust & quarantine flags
        device = registryRepo.findById(deviceId).get();

        // 10) If trust dropped, terminate session & fully quarantine
        if (!device.isTrusted()) {
            sessionService.terminateSession(sessionId, 
                "Trust score below threshold: " + device.getTrustScore());
            quarantineService.quarantineDevice(deviceId, 
                "Trust score below threshold: " + device.getTrustScore());
        }
    }
}