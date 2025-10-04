package edu.university.iot.service;

import edu.university.iot.entity.DeviceRegistry;
import edu.university.iot.model.AnomalyLog;
import edu.university.iot.repository.AnomalyLogRepository;
import edu.university.iot.repository.DeviceRegistryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class AnomalyDetectorService {

    private static final Logger logger = LoggerFactory.getLogger(AnomalyDetectorService.class);

    @Autowired
    private DeviceRegistryRepository deviceRepo;

    @Autowired
    private AnomalyLogRepository anomalyRepo;

    /**
     * Checks for anomalies, logs them, and returns true if an anomaly was detected.
     * Fixed to handle both Double and Integer types for numeric values.
     */
    public boolean checkAnomaly(Map<String, Object> telemetry) {
        String deviceId = (String) telemetry.get("deviceId");

        // Safe parsing of numeric values (can be Double or Integer from JSON)
        double cpu = parseDouble(telemetry.get("cpuUsage"));
        double mem = parseDouble(telemetry.get("memoryUsage"));
        double net = parseDouble(telemetry.get("networkTrafficVolume"));
        boolean malware = Boolean.TRUE.equals(telemetry.get("malwareSignatureDetected"));

        DeviceRegistry device = deviceRepo.findById(deviceId).orElse(null);
        boolean anomaly = false;
        StringBuilder reason = new StringBuilder();

        // Check malware first
        if (malware) {
            anomaly = true;
            reason.append("Malware signature detected; ");
        }

        // Check resource limits from device registry
        if (device != null) {
            if (device.getMaxCpuUsage() != null && cpu > device.getMaxCpuUsage()) {
                anomaly = true;
                reason.append("High CPU (").append(String.format("%.1f", cpu))
                        .append("% > ").append(device.getMaxCpuUsage()).append("%); ");
            }
            if (device.getMaxMemoryUsage() != null && mem > device.getMaxMemoryUsage()) {
                anomaly = true;
                reason.append("High Memory (").append(String.format("%.1f", mem))
                        .append("% > ").append(device.getMaxMemoryUsage()).append("%); ");
            }
            if (device.getMaxNetworkTraffic() != null && net > device.getMaxNetworkTraffic()) {
                anomaly = true;
                reason.append("High Network (").append(String.format("%.1f", net))
                        .append(" > ").append(device.getMaxNetworkTraffic()).append("); ");
            }
        }

        // Create and save anomaly log
        AnomalyLog log = new AnomalyLog();
        log.setDeviceId(deviceId);
        log.setCpuUsage(cpu);
        log.setMemoryUsage(mem);
        log.setNetworkTrafficVolume(net);
        log.setAnomalyDetected(anomaly);
        log.setReason(anomaly ? reason.toString().trim() : "Normal behavior");
        log.setTimestamp(Instant.now());

        anomalyRepo.save(log);

        if (anomaly) {
            logger.warn("Anomaly detected for device [{}]: {}", deviceId, log.getReason());
        } else {
            logger.debug("Normal behavior logged for device [{}]", deviceId);
        }

        return anomaly;
    }

    /**
     * Helper method to safely parse numeric values that could be Double or Integer
     */
    private double parseDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                logger.warn("Could not parse numeric value: {}", value);
                return 0.0;
            }
        }
        return 0.0;
    }

    public List<AnomalyLog> getLogs(String deviceId) {
        return anomalyRepo.findByDeviceId(deviceId);
    }
}