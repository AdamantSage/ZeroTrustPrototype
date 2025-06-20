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

@Service
public class EventListenerService {

    @Value("${eventhubs.connection-string}")
    private String eventHubConnectionString;

    @Value("${eventhubs.entity-path}")
    private String eventHubName;

    private final DeviceMessageRepository deviceMessageRepository;
    private final TelemetryProcessorService telemetryProcessorService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public EventListenerService(DeviceMessageRepository deviceMessageRepository,
                                TelemetryProcessorService telemetryProcessorService) {
        this.deviceMessageRepository = deviceMessageRepository;
        this.telemetryProcessorService = telemetryProcessorService;
    }

    @PostConstruct
    public void startListening() {
        // ← Add this debug line to confirm the method is called
        System.out.println(">> [EventListenerService] Subscribing to Event Hub: " + eventHubName);

        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
            .connectionString(eventHubConnectionString, eventHubName)
            .consumerGroup("$Default")
            .buildAsyncConsumerClient();

        // ← Wrap subscribe() with an error handler and an early debug print
        consumer.receive(false).subscribe(
            partitionEvent -> {
                // ← Add this to see each incoming event
                System.out.println(">> [EventListenerService] Received event from partition: " +
                    partitionEvent.getPartitionContext().getPartitionId());

                try {
                    String data = partitionEvent.getData().getBodyAsString();
                    DeviceMessage message = objectMapper.readValue(data, DeviceMessage.class);

                    // save & process
                    DeviceMessage saved = deviceMessageRepository.saveAndFlush(message);
                    System.out.println("Saved telemetry from device: " + saved.getDeviceId() +
                                       ", assigned id=" + saved.getId());

                    telemetryProcessorService.process(message.toMap());
                    System.out.println("Processed telemetry for device: " + message.getDeviceId());

                } catch (Exception e) {
                    System.err.println("Error processing telemetry: " + e.getMessage());
                    e.printStackTrace();
                }
            },
            error -> {
                // ← Add this to catch subscription errors
                System.err.println("!! [EventListenerService] Subscription error: " + error);
            }
        );
    }
}
