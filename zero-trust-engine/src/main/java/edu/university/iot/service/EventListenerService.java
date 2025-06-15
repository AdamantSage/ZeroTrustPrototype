package edu.university.iot.service;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import edu.university.iot.entity.DeviceMessage;
import edu.university.iot.model.*;
import edu.university.iot.repository.DeviceMessageRepository;
import edu.university.iot.repository.IdentityLogRepository;
import edu.university.iot.repository.FirmwareLogRepository;
import edu.university.iot.repository.AnomalyLogRepository;
import edu.university.iot.repository.ComplianceLogRepository;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EventListenerService {

    @Autowired private IdentityService identityService;
    @Autowired private FirmwareService firmwareService;
    @Autowired private AnomalyDetectorService anomalyDetectorService;
    @Autowired private ComplianceService complianceService;

    @Autowired private DeviceMessageRepository deviceRepo;
    @Autowired private IdentityLogRepository identityRepo;
    @Autowired private FirmwareLogRepository firmwareRepo;
    @Autowired private AnomalyLogRepository anomalyRepo;
    @Autowired private ComplianceLogRepository complianceRepo;

    @Value("${eventhubs.connection-string}")
    private String connectionString;

    @Value("${eventhubs.entity-path}")
    private String entityPath;

    @Value("${eventhubs.consumer-group}")
    private String consumerGroup;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @PostConstruct
    @Transactional
    public void listen() {
        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
                .connectionString(connectionString, entityPath)
                .consumerGroup(consumerGroup)
                .buildAsyncConsumerClient();

        consumer.receive()
                .subscribe(partitionEvent -> {
                    try {
                        String data = partitionEvent.getData().getBodyAsString();
                        System.out.println("📩 Message received: " + data);

                        // Deserialize JSON → DeviceMessage
                        DeviceMessage msg = objectMapper.readValue(data, DeviceMessage.class);
                        deviceRepo.save(msg);

                        // Route to services
                        boolean trusted = identityService.isTrusted(msg.getDeviceId());
                        boolean fwValid = firmwareService.isValid(msg.getDeviceId(), msg.getFirmwareVersion());
                        boolean anomaly = anomalyDetectorService.isAnomaly(msg.getDeviceId(), msg.getTemperature());
                        boolean comply = complianceService.isCompliant(msg.getFirmwareVersion());

                        // Handle null timestamp
                        LocalDateTime timestamp = msg.getTimestamp();
                        if (timestamp == null) {
                            timestamp = LocalDateTime.now();
                        }

                        // Persist logs
                        identityRepo.save(new IdentityLog(msg.getDeviceId(), trusted, timestamp));
                        firmwareRepo.save(new FirmwareLog(msg.getDeviceId(), msg.getFirmwareVersion(), fwValid, timestamp));
                        anomalyRepo.save(new AnomalyLog(msg.getDeviceId(), msg.getTemperature(), anomaly, timestamp));
                        complianceRepo.save(new ComplianceLog(msg.getDeviceId(), msg.getFirmwareVersion(), comply, timestamp));

                    } catch (Exception e) {
                        System.err.println("❌ Error processing event: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
    }
}