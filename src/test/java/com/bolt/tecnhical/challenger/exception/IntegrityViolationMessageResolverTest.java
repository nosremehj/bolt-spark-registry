package com.bolt.tecnhical.challenger.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class IntegrityViolationMessageResolverTest {

	@Test
	void deveResolverMensagemParaNumeroInstalacaoDuplicado() {
		var ex = new DataIntegrityViolationException(
				"UK_UC_NUMERO_INSTALACAO",
				new RuntimeException("Unique index violation on UK_UC_NUMERO_INSTALACAO"));

		assertThat(IntegrityViolationMessageResolver.resolver(ex))
				.isEqualTo("Número de instalação já cadastrado para outro cliente");
	}

	@Test
	void deveResolverMensagemParaDocumentoDuplicado() {
		var ex = new DataIntegrityViolationException(
				"UK_CLIENTE_DOCUMENTO",
				new RuntimeException("Unique index violation on UK_CLIENTE_DOCUMENTO"));

		assertThat(IntegrityViolationMessageResolver.resolver(ex))
				.isEqualTo("Já existe cliente cadastrado com este documento");
	}

	@Test
	void deveRetornarMensagemGenericaQuandoConstraintForDesconhecida() {
		var ex = new DataIntegrityViolationException("outra constraint", new RuntimeException("erro genérico"));

		assertThat(IntegrityViolationMessageResolver.resolver(ex))
				.isEqualTo("Violação de integridade dos dados");
	}

}
