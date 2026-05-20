package com.bolt.tecnhical.challenger.web.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.bolt.tecnhical.challenger.domain.Endereco;

class EnderecoResponseTest {

	@Test
	void deveRetornarTextoPadraoQuandoComplementoForNulo() {
		Endereco endereco = new Endereco();
		endereco.setCep("30130010");
		endereco.setLogradouro("Rua A");
		endereco.setBairro("Centro");
		endereco.setCidade("Belo Horizonte");
		endereco.setUf("MG");

		EnderecoResponse response = EnderecoResponse.from(endereco);

		assertThat(response.complemento()).isEqualTo("Sem complemento");
	}

	@Test
	void deveManterComplementoInformado() {
		Endereco endereco = new Endereco();
		endereco.setCep("30130010");
		endereco.setLogradouro("Rua A");
		endereco.setComplemento("Apto 101");
		endereco.setBairro("Centro");
		endereco.setCidade("Belo Horizonte");
		endereco.setUf("MG");

		EnderecoResponse response = EnderecoResponse.from(endereco);

		assertThat(response.complemento()).isEqualTo("Apto 101");
	}

}
