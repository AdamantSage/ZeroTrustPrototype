package edu.university.iot.service;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import edu.university.iot.entity.DeviceMessage;
import edu.university.iot.entity.DeviceRegistry;
import edu.university.iot.repository.DeviceMessageRepository;
import edu.university.iot.repository.DeviceRegistryRepository;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.Optional;

@Service
public class EventListenerService {
    private static final Logger log = LoggerFactory.getLogger(EventListenerService.class);

    private final DeviceRegistryRepository deviceRegistryRepository;
    private final DeviceMessageRepository deviceMessageRepository;
    private final ObjectMapper objectMapper;

    @Value("${eventhubs.connection-string}")
    private String connectionString;
    @Value("${eventhubs.entity-path}")
    private String entityPath;
    @Value("${eventhubs.consumer-group}")
    private String consumerGroup;

    public EventListenerService(DeviceRegistryRepository deviceRegistryRepository,
                                DeviceMessageRepository deviceMessageRepository) {
        this.deviceRegistryRepository = deviceRegistryRepository;
        this.deviceMessageRepository = deviceMessageRepository;
        // configure Jackson for Instant parsing
        this.objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /** Start listening as soon as Spring Boot is up */
    @PostConstruct
    public void startListening() {
        log.info("🔌 Connecting to Azure Event Hub...");
        EventHubConsumerAsyncClient client = new EventHubClientBuilder()
            .connectionString(connectionString, entityPath)
            .consumerGroup(consumerGroup)
            .buildAsyncConsumerClient();

        // Build a Flux<String> of raw JSON bodies
        Flux<String> payloadFlux = Flux.create(sink ->
            client.receive()
                  .map(evt -> evt.getData().getBodyAsString())
                  .subscribe(sink::next, sink::error, sink::complete)
        );

        processEvents(payloadFlux);
        log.info("🚀 Event Listener connected and processing started.");
    }

    /** Feed incoming JSON strings into your processing pipeline */
    public void processEvents(Flux<String> eventFlux) {
        eventFlux
          .map(this::parseMessage)           // JSON → DeviceMessage
          .subscribe(
            this::processDeviceMessageSafely,
            err -> log.error("Stream error", err)
          );
    }

    /** 
     * Deserialize JSON → DeviceMessage.
     * If it fails, it will be logged and dropped.
     */
    private DeviceMessage parseMessage(String json) {
        try {
            DeviceMessage msg = objectMapper.readValue(json, DeviceMessage.class);
            log.debug("📩 Parsed message from {}: {}", msg.getDeviceId(), msg);
            return msg;
        } catch (Exception e) {
            log.error("❌ Failed to parse JSON: {}", json, e);
            // Return null to filter out
            return null;
        }
    }

    /**
     * Core processing: auto‑register PENDING, skip non‑ACTIVE, save ACTIVE.
     */
    @Transactional
    public void processDeviceMessageSafely(DeviceMessage message) {
        if (message == null) return;

        String deviceId = message.getDeviceId();
        registerIfMissing(message);

        Optional<DeviceRegistry> regOpt = deviceRegistryRepository.findByDeviceId(deviceId);
        if (regOpt.isEmpty()) {
            log.error("DeviceRegistry missing after registration for {}", deviceId);
            return;
        }

        String status = regOpt.get().getStatus();
        if (!"ACTIVE".equalsIgnoreCase(status)) {
            log.warn("⏸️ Skipping {} (status={})", deviceId, status);
            return;
        }

        deviceMessageRepository.save(message);
        log.info("✅ Saved telemetry for device {}", deviceId);
    }

    /** On first‐contact, insert a PENDING registration */
    @Transactional
    private void registerIfMissing(DeviceMessage message) {
        String deviceId = message.getDeviceId();
        if (deviceRegistryRepository.findByDeviceId(deviceId).isEmpty()) {
            DeviceRegistry dr = new DeviceRegistry();
            dr.setDeviceId(deviceId);
            dr.setFirmwareVersion(message.getFirmwareVersion());
            dr.setRegistrationDate(Instant.now());
            dr.setStatus("PENDING");
            dr.setDeviceType("IOT_SENSOR");
            deviceRegistryRepository.save(dr);
            log.info("🔔 Auto-registered {} as PENDING", deviceId);
        }
    }
}
