// src/main/java/edu/university/iot/service/AnomalyDetectorService.java
package edu.university.iot.service;

import edu.university.iot.entity.DeviceRegistry;
import edu.university.iot.model.AnomalyLog;
import edu.university.iot.repository.AnomalyLogRepository;
import edu.university.iot.repository.DeviceRegistryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class AnomalyDetectorService {

    @Autowired
    private DeviceRegistryRepository deviceRepo;

    @Autowired
    private AnomalyLogRepository anomalyRepo;

    /**
     * Checks for anomalies, logs them, and returns true if an anomaly was detected.
     */
    public boolean checkAnomaly(Map<String, Object> telemetry) {
        String deviceId = (String) telemetry.get("deviceId");
        double cpu = (double) telemetry.get("cpuUsage");
        double mem = (double) telemetry.get("memoryUsage");
        double net = (double) telemetry.get("networkTrafficVolume");
        boolean malware = (boolean) telemetry.get("malwareSignatureDetected");

        DeviceRegistry device = deviceRepo.findById(deviceId).orElse(null);
        boolean anomaly = false;
        StringBuilder reason = new StringBuilder();

        if (malware) {
            anomaly = true;
            reason.append("Malware signature detected; ");
        } 
        if (device != null) {
            if (device.getMaxCpuUsage() != null && cpu > device.getMaxCpuUsage()) {
                anomaly = true;
                reason.append("High CPU; ");
            }
            if (device.getMaxMemoryUsage() != null && mem > device.getMaxMemoryUsage()) {
                anomaly = true;
                reason.append("High Memory; ");
            }
            if (device.getMaxNetworkTraffic() != null && net > device.getMaxNetworkTraffic()) {
                anomaly = true;
                reason.append("High Network; ");
            }
        }

        AnomalyLog log = new AnomalyLog();
        log.setDeviceId(deviceId);
        log.setCpuUsage(cpu);
        log.setMemoryUsage(mem);
        log.setNetworkTrafficVolume(net);
        log.setAnomalyDetected(anomaly);
        log.setReason(reason.toString().trim());
        log.setTimestamp(Instant.now());

        anomalyRepo.save(log);
        return anomaly;
    }

    public List<AnomalyLog> getLogs(String deviceId) {
    return anomalyRepo.findByDeviceId(deviceId);
}
}
