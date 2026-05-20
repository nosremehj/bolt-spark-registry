package com.bolt.tecnhical.challenger.messaging.kafka;

import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.bolt.tecnhical.challenger.exception.KafkaPublishException;
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

		try {
			var resultado = kafkaTemplate.send(topico, chave, mensagem)
					.get(properties.sendTimeoutSeconds(), TimeUnit.SECONDS);
			log.info("Mensagem publicada no tópico {} para cliente {} (partição {})",
					topico,
					clienteId,
					resultado.getRecordMetadata().partition());
		}
		catch (TimeoutException ex) {
			throw new KafkaPublishException(
					"Serviço de mensageria indisponível: tempo esgotado ao publicar análise do cliente. "
							+ "Verifique se o Kafka está em execução.",
					ex);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new KafkaPublishException(
					"Serviço de mensageria indisponível: publicação interrompida.", ex);
		}
		catch (ExecutionException ex) {
			Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
			log.error("Falha ao publicar no tópico {} para cliente {}", topico, clienteId, causa);
			throw new KafkaPublishException(
					"Serviço de mensageria indisponível: não foi possível publicar a análise do cliente. "
							+ "Verifique se o Kafka está em execução.",
					causa);
		}
		catch (RuntimeException ex) {
			// send() síncrono (ex.: KafkaException) não passa por ExecutionException
			log.error("Falha ao publicar no tópico {} para cliente {}", topico, clienteId, ex);
			throw new KafkaPublishException(
					"Serviço de mensageria indisponível: não foi possível publicar a análise do cliente. "
							+ "Verifique se o Kafka está em execução.",
					ex);
		}
	}

}
