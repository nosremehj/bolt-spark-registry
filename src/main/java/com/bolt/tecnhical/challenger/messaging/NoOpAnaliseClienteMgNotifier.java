package com.bolt.tecnhical.challenger.messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.bolt.tecnhical.challenger.domain.Cliente;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "false")
public class NoOpAnaliseClienteMgNotifier implements AnaliseClienteMgNotifier {

	@Override
	public void notificarSeNecessario(Cliente cliente) {
		// Kafka desabilitado (ex.: perfil de testes automatizados).
	}

}
