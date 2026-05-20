package com.bolt.tecnhical.challenger.messaging.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.bolt.tecnhical.challenger.domain.Cliente;
import com.bolt.tecnhical.challenger.messaging.AnaliseClienteMgEvent;
import com.bolt.tecnhical.challenger.messaging.AnaliseClienteMgNotifier;
import com.bolt.tecnhical.challenger.util.EstadoUtil;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaAnaliseClienteMgNotifier implements AnaliseClienteMgNotifier {

	private final AnaliseClienteMgKafkaProducer kafkaProducer;

	public KafkaAnaliseClienteMgNotifier(AnaliseClienteMgKafkaProducer kafkaProducer) {
		this.kafkaProducer = kafkaProducer;
	}

	@Override
	public void notificarSeNecessario(Cliente cliente) {
		if (!EstadoUtil.clientePossuiUnidadeEmMg(cliente)) {
			return;
		}
		kafkaProducer.enviar(new AnaliseClienteMgEvent(
				cliente.getId(),
				cliente.getDocumento(),
				cliente.getNome(),
				EstadoUtil.numerosInstalacaoEmMg(cliente)));
	}

}
