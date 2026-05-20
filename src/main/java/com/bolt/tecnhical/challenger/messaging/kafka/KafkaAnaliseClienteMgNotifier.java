package com.bolt.tecnhical.challenger.messaging.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.bolt.tecnhical.challenger.domain.Cliente;
import com.bolt.tecnhical.challenger.messaging.AnaliseClienteMgEvent;
import com.bolt.tecnhical.challenger.messaging.AnaliseClienteMgNotifier;
import com.bolt.tecnhical.challenger.util.EstadoUtil;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaAnaliseClienteMgNotifier implements AnaliseClienteMgNotifier {

	private final ApplicationEventPublisher eventPublisher;

	public KafkaAnaliseClienteMgNotifier(ApplicationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	@Override
	public void notificarSeNecessario(Cliente cliente) {
		if (!EstadoUtil.clientePossuiUnidadeEmMg(cliente)) {
			return;
		}
		eventPublisher.publishEvent(new AnaliseClienteMgEvent(
				cliente.getId(),
				cliente.getDocumento(),
				cliente.getNome(),
				EstadoUtil.numerosInstalacaoEmMg(cliente)));
	}

}
