package com.bolt.tecnhical.challenger.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CampoValidacaoFormatterTest {

	@Test
	void deveFormatarCampoDeUnidadeConsumidoraAninhada() {
		assertThat(CampoValidacaoFormatter.formatar("unidadesConsumidoras[1].endereco.cep"))
				.isEqualTo("Unidade consumidora 2 — Endereço — CEP");
	}

	@Test
	void deveFormatarCampoDoEnderecoDoCliente() {
		assertThat(CampoValidacaoFormatter.formatar("endereco.cep"))
				.isEqualTo("Endereço do cliente — CEP");
	}

	@Test
	void deveFormatarCampoSimples() {
		assertThat(CampoValidacaoFormatter.formatar("documento")).isEqualTo("Documento");
	}

}
