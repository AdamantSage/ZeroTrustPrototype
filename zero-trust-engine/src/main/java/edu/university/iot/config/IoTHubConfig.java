package edu.university.iot.config;

import com.microsoft.azure.sdk.iot.service.RegistryManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class IoTHubConfig {
    
    @Bean
    public RegistryManager registryManager(
            @Value("${azure.iot.hub.connection-string}") String connectionString
    ) throws IOException {
        return RegistryManager.createFromConnectionString(connectionString);
    }
}