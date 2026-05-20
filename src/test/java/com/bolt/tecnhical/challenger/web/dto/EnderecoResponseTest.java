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
		assertThat(response.unidade()).isEqualTo("Sem unidade");
		assertThat(response.estado()).isEqualTo("Sem estado");
		assertThat(response.regiao()).isEqualTo("Sem região");
		assertThat(response.ibge()).isEqualTo("Sem IBGE");
		assertThat(response.gia()).isEqualTo("Sem GIA");
		assertThat(response.ddd()).isEqualTo("Sem DDD");
		assertThat(response.siafi()).isEqualTo("Sem SIAFI");
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

	@Test
	void deveRetornarCamposDoViaCepQuandoInformados() {
		Endereco endereco = new Endereco();
		endereco.setCep("31630900");
		endereco.setLogradouro("Rodovia Papa João Paulo II");
		endereco.setBairro("Serra Verde (Venda Nova)");
		endereco.setCidade("Belo Horizonte");
		endereco.setUf("MG");
		endereco.setUnidade("Cidade Administrativa");
		endereco.setEstado("Minas Gerais");
		endereco.setRegiao("Sudeste");
		endereco.setIbge("3106200");
		endereco.setDdd("31");
		endereco.setSiafi("4123");

		EnderecoResponse response = EnderecoResponse.from(endereco);

		assertThat(response.unidade()).isEqualTo("Cidade Administrativa");
		assertThat(response.estado()).isEqualTo("Minas Gerais");
		assertThat(response.regiao()).isEqualTo("Sudeste");
		assertThat(response.ibge()).isEqualTo("3106200");
		assertThat(response.ddd()).isEqualTo("31");
		assertThat(response.siafi()).isEqualTo("4123");
		assertThat(response.gia()).isEqualTo("Sem GIA");
	}

}
