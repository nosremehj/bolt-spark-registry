package com.bolt.tecnhical.challenger.web.dto;

import com.bolt.tecnhical.challenger.domain.UnidadeConsumidora;

public record UnidadeConsumidoraResponse(
		Long id,
		String nome,
		String numeroInstalacao,
		EnderecoResponse endereco) {

	public static UnidadeConsumidoraResponse from(UnidadeConsumidora unidade) {
		return new UnidadeConsumidoraResponse(
				unidade.getId(),
				unidade.getNome(),
				unidade.getNumeroInstalacao(),
				EnderecoResponse.from(unidade.getEndereco()));
	}

}
