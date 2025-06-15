package edu.university.iot.service;

import com.azure.messaging.eventhubs.*;
import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Service;

@Service
public class EventListenerService {

    private static final String connectionString = "HostName=myZeroTrustHub.azure-devices.net;SharedAccessKeyName=eventHubListenerPolicy;SharedAccessKey=uaM21VIKKTpDzy37rSQnicPnMQpKVabcYAIoTMb7xkU=";
    private static final String eventHubName = "Endpoint=sb://ihsuprodsouthafricanorthres004dednamespace.servicebus.windows.net/;SharedAccessKeyName=iothubowner;SharedAccessKey=EUVSpzmKu8WgN5pkQGNkCZlMLmdDhQjX3AIoTAv7Vwg=;EntityPath=iothub-ehub-myzerotrus-55412696-26a7252d47";

    @PostConstruct
    public void listen() {
        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
            .connectionString(connectionString, eventHubName)
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .buildAsyncConsumerClient();

        consumer.receive()
            .subscribe(partitionEvent -> {
                String data = partitionEvent.getData().getBodyAsString();
                System.out.println("Message received: " + data);
                // TODO: parse and store message
            });
    }
}
