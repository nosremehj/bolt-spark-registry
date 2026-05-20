package com.bolt.tecnhical.challenger.messaging.kafka;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KafkaAppProperties.class)
public class KafkaMessagingPropertiesConfig {
}
