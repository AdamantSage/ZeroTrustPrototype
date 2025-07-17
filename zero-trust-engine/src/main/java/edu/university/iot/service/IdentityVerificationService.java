package edu.university.iot.service;

import edu.university.iot.model.IdentityLog;
import edu.university.iot.repository.DeviceRegistryRepository;
import edu.university.iot.repository.IdentityLogRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class IdentityVerificationService {

    private final DeviceRegistryRepository registryRepo;
    private final IdentityLogRepository logRepo;

    public IdentityVerificationService(DeviceRegistryRepository registryRepo, IdentityLogRepository logRepo) {
        this.registryRepo = registryRepo;
        this.logRepo = logRepo;
    }

    /**
     * Verifies device identity and persists an IdentityLog entry.
     */
    public boolean verifyIdentity(Map<String, Object> telemetry) {
    String deviceId = (String) telemetry.get("deviceId");
    boolean certificateValid = Boolean.TRUE.equals(telemetry.get("certificateValid"));
    boolean knownDevice = registryRepo.existsById(deviceId);
    boolean verified = certificateValid && knownDevice;

    IdentityLog log = new IdentityLog(deviceId, certificateValid, verified, Instant.now());
    logRepo.save(log);

    return verified;
    }

    public List<IdentityLog> getLogs(String deviceId) {
    return logRepo.findByDeviceId(deviceId);
}
}

