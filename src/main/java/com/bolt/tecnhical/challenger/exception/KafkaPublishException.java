package com.bolt.tecnhical.challenger.exception;

/**
 * Falha ao publicar mensagem no Kafka (broker indisponível, timeout, etc.).
 */
public class KafkaPublishException extends RuntimeException {

	public KafkaPublishException(String message) {
		super(message);
	}

	public KafkaPublishException(String message, Throwable cause) {
		super(message, cause);
	}

}
