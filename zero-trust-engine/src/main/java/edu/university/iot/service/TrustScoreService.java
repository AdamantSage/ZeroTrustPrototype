// src/main/java/edu/university/iot/service/TrustScoreService.java
package edu.university.iot.service;

import edu.university.iot.entity.DeviceRegistry;
import edu.university.iot.repository.DeviceRegistryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TrustScoreService {

    private static final Logger logger = LoggerFactory.getLogger(TrustScoreService.class);

    @Autowired
    private DeviceRegistryRepository registryRepo;

    // Thresholds and weights
    private static final double TRUSTED_THRESHOLD = 70.0;
    private static final double IDENTITY_PENALTY = 5.0;
    private static final double IDENTITY_REWARD = 1.0;
    private static final double CONTEXT_PENALTY = 2.0;
    private static final double CONTEXT_REWARD = 0.5;
    private static final double FIRMWARE_PENALTY = 5.0;
    private static final double FIRMWARE_REWARD = 1.0;
    private static final double ANOMALY_PENALTY = 10.0;
    private static final double ANOMALY_REWARD = 2.0;
    private static final double COMPLIANCE_PENALTY = 10.0;
    private static final double COMPLIANCE_REWARD = 2.0;

    /**
     * Adjusts trust score considering identity, context, firmware, anomalies, and compliance.
     */
    public void adjustTrust(String deviceId,
                            boolean identityPass,
                            boolean contextPass,
                            boolean firmwareValid,
                            boolean anomalyDetected,
                            boolean compliancePassed) {
        DeviceRegistry device = registryRepo.findById(deviceId).orElse(null);
        if (device == null) {
            logger.warn("Device [{}] not found for trust adjustment", deviceId);
            return;
        }

        double score = device.getTrustScore() != null ? device.getTrustScore() : 50.0;
        double originalScore = score;

        // Identity
        score += identityPass ? IDENTITY_REWARD : -IDENTITY_PENALTY;
        // Context
        score += contextPass ? CONTEXT_REWARD : -CONTEXT_PENALTY;
        // Firmware
        score += firmwareValid ? FIRMWARE_REWARD : -FIRMWARE_PENALTY;
        // Anomaly
        score += anomalyDetected ? -ANOMALY_PENALTY : ANOMALY_REWARD;
        // Compliance
        score += compliancePassed ? COMPLIANCE_REWARD : -COMPLIANCE_PENALTY;

        // Clamp 0-100
        score = Math.max(0, Math.min(score, 100));

        device.setTrustScore(score);
        boolean wasTrusted = device.isTrusted();
        boolean isTrusted = score >= TRUSTED_THRESHOLD;
        device.setTrusted(isTrusted);

        registryRepo.save(device);

        logger.info("Device [{}] trust score: {} -> {} (identity={}, context={}, firmware={}, anomaly={}, compliance={})",
                    deviceId, originalScore, score,
                    identityPass, contextPass, firmwareValid, !anomalyDetected, compliancePassed);

        if (wasTrusted && !isTrusted) {
            logger.warn("Device [{}] lost trusted status at {}", deviceId, score);
        } else if (!wasTrusted && isTrusted) {
            logger.info("Device [{}] regained trusted status at {}", deviceId, score);
        }
    }

     public double getTrustScore(String deviceId) {
        DeviceRegistry device = registryRepo.findById(deviceId).orElse(null);
        return device != null && device.getTrustScore() != null 
             ? device.getTrustScore() 
             : 0.0;
    }
}
