package edu.university.iot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AnomalyDetectorService {
    private static final Logger logger = LoggerFactory.getLogger(AnomalyDetectorService.class);
    private final Map<String, List<Double>> history = new HashMap<>();

    /**
     * Returns true if value deviates >3σ from rolling history
     */
    public boolean isAnomaly(String deviceId, double value) {
        List<Double> list = history.computeIfAbsent(deviceId, k -> new ArrayList<>());
        list.add(value);

        if (list.size() < 10) {
            logger.debug("Device [{}]: Not enough data points yet ({} values)", deviceId, list.size());
            return false;
        }

        double mean = list.stream().mapToDouble(v -> v).average().orElse(0);
        double std = Math.sqrt(list.stream()
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .sum() / list.size());

        boolean anomaly = Math.abs(value - mean) > 3 * std;
        logger.info("Device [{}]: Value = {}, Mean = {}, StdDev = {}, Anomaly = {}", 
            deviceId, value, mean, std, anomaly);

        return anomaly;
    }
}
