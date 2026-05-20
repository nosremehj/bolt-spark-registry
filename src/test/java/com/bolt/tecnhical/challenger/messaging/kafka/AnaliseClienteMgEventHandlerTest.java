package com.bolt.tecnhical.challenger.messaging.kafka;

import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bolt.tecnhical.challenger.messaging.AnaliseClienteMgEvent;

@ExtendWith(MockitoExtension.class)
class AnaliseClienteMgEventHandlerTest {

	@Mock
	private AnaliseClienteMgKafkaProducer kafkaProducer;

	@InjectMocks
	private AnaliseClienteMgEventHandler handler;

	@Test
	void deveDelegarPublicacaoAoProducer() {
		AnaliseClienteMgEvent event = new AnaliseClienteMgEvent(
				3L, "52998224725", "João Silva", List.of("INST-MG-01"));

		handler.publicarNoTopico(event);

		verify(kafkaProducer).enviar(event);
	}

}
