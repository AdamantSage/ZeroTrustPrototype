package edu.university.iot.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AnomalyDetectorService {
  private final Map<String, List<Double>> history = new HashMap<>();

  /** 
   * returns true if value deviates >3σ from rolling history 
   */
  public boolean isAnomaly(String deviceId, double value) {
    List<Double> list = history.computeIfAbsent(deviceId, k -> new ArrayList<>());
    list.add(value);
    if (list.size() < 10) return false;

    double mean = list.stream().mapToDouble(v -> v).average().orElse(0);
    double std  = Math.sqrt(list.stream()
        .mapToDouble(v -> Math.pow(v - mean, 2))
        .sum() / list.size());

    return Math.abs(value - mean) > 3 * std;
  }
}
