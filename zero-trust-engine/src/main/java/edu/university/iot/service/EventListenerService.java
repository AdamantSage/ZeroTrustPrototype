package edu.university.iot.service;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

import edu.university.iot.entity.DeviceMessage;
import edu.university.iot.repository.DeviceMessageRepository;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Updated EventListenerService with intelligent routing for healthy vs standard
 * telemetry
 */
@Service
public class EventListenerService {

    @Value("${eventhubs.connection-string}")
    private String eventHubConnectionString;

    @Value("${eventhubs.entity-path}")
    private String eventHubName;

    private final DeviceMessageRepository deviceMessageRepository;
    private final TelemetryProcessorService telemetryProcessorService;
    private final HealthyTelemetryProcessorService healthyTelemetryProcessorService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public EventListenerService(DeviceMessageRepository deviceMessageRepository,
            TelemetryProcessorService telemetryProcessorService,
            HealthyTelemetryProcessorService healthyTelemetryProcessorService) {
        this.deviceMessageRepository = deviceMessageRepository;
        this.telemetryProcessorService = telemetryProcessorService;
        this.healthyTelemetryProcessorService = healthyTelemetryProcessorService;
    }

    @PostConstruct
    public void startListening() {
        System.out.println(">> [EventListenerService] Subscribing to Event Hub: " + eventHubName);

        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
                .connectionString(eventHubConnectionString, eventHubName)
                .consumerGroup("$Default")
                .buildAsyncConsumerClient();

        consumer.receive(false).subscribe(
                partitionEvent -> {
                    System.out.println(">> [EventListenerService] Received event from partition: " +
                            partitionEvent.getPartitionContext().getPartitionId());

                    try {
                        String data = partitionEvent.getData().getBodyAsString();
                        DeviceMessage message = objectMapper.readValue(data, DeviceMessage.class);

                        // Save message
                        DeviceMessage saved = deviceMessageRepository.saveAndFlush(message);
                        System.out.println("Saved telemetry from device: " + saved.getDeviceId() +
                                ", assigned id=" + saved.getId());

                        // Convert to map for processing
                        Map<String, Object> telemetryMap = message.toMap();

                        // Intelligent routing based on telemetry type
                        if (isHealthyTelemetry(telemetryMap)) {
                            System.out.println(">>> Routing to HEALTHY telemetry processor for device: " +
                                    message.getDeviceId());
                            healthyTelemetryProcessorService.processHealthyTelemetry(telemetryMap);
                        } else {
                            System.out.println(">>> Routing to STANDARD telemetry processor for device: " +
                                    message.getDeviceId());
                            telemetryProcessorService.process(telemetryMap);
                        }

                        System.out.println("Processed telemetry for device: " + message.getDeviceId());

                    } catch (Exception e) {
                        System.err.println("Error processing telemetry: " + e.getMessage());
                        e.printStackTrace();
                    }
                },
                error -> {
                    System.err.println("!! [EventListenerService] Subscription error: " + error);
                });
    }

    /**
     * Determine if telemetry represents healthy device behavior
     */
    private boolean isHealthyTelemetry(Map<String, Object> telemetry) {
        // Check for explicit healthy behavior indicator
        Boolean healthyIndicator = (Boolean) telemetry.get("healthyBehaviorIndicator");
        if (Boolean.TRUE.equals(healthyIndicator)) {
            return true;
        }

        // Fallback: check if all health criteria are met
        Boolean certValid = (Boolean) telemetry.get("certificateValid");
        Boolean malware = (Boolean) telemetry.get("malwareSignatureDetected");
        Double anomalyScore = (Double) telemetry.get("anomalyScore");
        String patchStatus = (String) telemetry.get("patchStatus");
        Integer suspiciousScore = (Integer) telemetry.get("suspiciousActivityScore");
        Integer consecutiveAnomalies = (Integer) telemetry.get("consecutiveAnomalies");

        return Boolean.TRUE.equals(certValid) &&
                Boolean.FALSE.equals(malware) &&
                anomalyScore != null && anomalyScore < 0.1 &&
                "Up-to-date".equalsIgnoreCase(patchStatus) &&
                (suspiciousScore == null || suspiciousScore == 0) &&
                (consecutiveAnomalies == null || consecutiveAnomalies == 0);
    }
}