package com.bolt.tecnhical.challenger.messaging.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.bolt.tecnhical.challenger.messaging.AnaliseClienteMgEvent;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class AnaliseClienteMgEventHandler {

	private final AnaliseClienteMgKafkaProducer kafkaProducer;

	public AnaliseClienteMgEventHandler(AnaliseClienteMgKafkaProducer kafkaProducer) {
		this.kafkaProducer = kafkaProducer;
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void publicarNoTopico(AnaliseClienteMgEvent event) {
		kafkaProducer.enviar(event);
	}

}
