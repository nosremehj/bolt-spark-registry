package com.bolt.tecnhical.challenger.messaging.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public record KafkaAppProperties(
		boolean enabled,
		String topicAnaliseClienteMg) {
}
