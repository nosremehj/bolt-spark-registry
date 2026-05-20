package com.bolt.tecnhical.challenger.integration.viacep;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ViaCepResponse(
		String cep,
		String logradouro,
		String complemento,
		String bairro,
		@JsonProperty("localidade") String cidade,
		String uf,
		String erro) {

	public boolean cepInvalido() {
		return "true".equalsIgnoreCase(erro);
	}

}
