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

    // ✅ Register support for Instant, LocalDateTime, etc.
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public EventListenerService(DeviceMessageRepository deviceMessageRepository) {
        this.deviceMessageRepository = deviceMessageRepository;
    }

    @PostConstruct
    public void startListening() {
        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
            .connectionString(eventHubConnectionString, eventHubName)
            .consumerGroup("$Default")
            .buildAsyncConsumerClient();

        consumer.receive(false).subscribe(partitionEvent -> {
            try {
                String data = partitionEvent.getData().getBodyAsString();
                DeviceMessage message = objectMapper.readValue(data, DeviceMessage.class);
                deviceMessageRepository.save(message);
                System.out.println("Saved telemetry from device: " + message.getDeviceId());
            } catch (Exception e) {
                System.err.println("Error processing telemetry: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
