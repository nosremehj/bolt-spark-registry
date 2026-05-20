package com.bolt.tecnhical.challenger.web.dto;

import com.bolt.tecnhical.challenger.domain.Endereco;

public record EnderecoResponse(
		String cep,
		String logradouro,
		String complemento,
		String bairro,
		String cidade,
		String uf) {

	private static final String SEM_COMPLEMENTO = "Sem complemento";

	public static EnderecoResponse from(Endereco endereco) {
		return new EnderecoResponse(
				endereco.getCep(),
				endereco.getLogradouro(),
				formatarComplemento(endereco.getComplemento()),
				endereco.getBairro(),
				endereco.getCidade(),
				endereco.getUf());
	}

	private static String formatarComplemento(String complemento) {
		if (complemento == null || complemento.isBlank()) {
			return SEM_COMPLEMENTO;
		}
		return complemento;
	}

}
