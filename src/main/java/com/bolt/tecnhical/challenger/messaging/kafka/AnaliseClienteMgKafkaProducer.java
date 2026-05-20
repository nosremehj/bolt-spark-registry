package com.bolt.tecnhical.challenger.messaging.kafka;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.bolt.tecnhical.challenger.messaging.AnaliseClienteMgEvent;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class AnaliseClienteMgKafkaProducer {

	private static final Logger log = LoggerFactory.getLogger(AnaliseClienteMgKafkaProducer.class);

	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final KafkaAppProperties properties;

	public AnaliseClienteMgKafkaProducer(
			KafkaTemplate<String, Object> kafkaTemplate,
			KafkaAppProperties properties) {
		this.kafkaTemplate = kafkaTemplate;
		this.properties = properties;
	}

	public void enviar(AnaliseClienteMgEvent event) {
		String topico = properties.topicAnaliseClienteMg();
		AnaliseClienteMgMessage mensagem = new AnaliseClienteMgMessage(
				event.clienteId(),
				event.documento(),
				event.nome(),
				event.numerosInstalacao(),
				Instant.now());
		Long clienteId = event.clienteId();
		String chave = String.valueOf(clienteId);

		kafkaTemplate.send(topico, chave, mensagem)
				.whenComplete((resultado, erro) -> {
					if (erro != null) {
						log.error("Falha ao publicar no tópico {} para cliente {}", topico, clienteId, erro);
						return;
					}
					log.info("Mensagem publicada no tópico {} para cliente {} (partição {})",
							topico,
							clienteId,
							resultado.getRecordMetadata().partition());
				});
	}

}
