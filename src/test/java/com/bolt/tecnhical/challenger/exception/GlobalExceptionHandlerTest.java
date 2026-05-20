package com.bolt.tecnhical.challenger.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

	private GlobalExceptionHandler handler;

	@BeforeEach
	void setUp() {
		handler = new GlobalExceptionHandler();
	}

	@Test
	void deveRetornar503QuandoKafkaIndisponivel() {
		var ex = new KafkaPublishException("Serviço de mensageria indisponível");

		ResponseEntity<ErrorResponse> response = handler.handleKafkaPublish(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().status()).isEqualTo(503);
		assertThat(response.getBody().message()).contains("mensageria indisponível");
	}

	@Test
	void deveRetornar409ParaViolacaoDeNumeroInstalacao() {
		var ex = new DataIntegrityViolationException(
				"falha",
				new RuntimeException("UK_UC_NUMERO_INSTALACAO violation"));

		ResponseEntity<ErrorResponse> response = handler.handleDataIntegrity(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().status()).isEqualTo(409);
		assertThat(response.getBody().message())
				.isEqualTo("Número de instalação já cadastrado para outro cliente");
		assertThat(response.getBody().details()).isEmpty();
	}

}
