package com.bolt.tecnhical.challenger.web.dto;

import java.time.Instant;
import java.util.List;

import com.bolt.tecnhical.challenger.domain.Cliente;

public record ClienteResponse(
		Long id,
		String nome,
		String documento,
		EnderecoResponse endereco,
		boolean ativo,
		Instant createdAt,
		Instant updatedAt,
		List<UnidadeConsumidoraResponse> unidadesConsumidoras) {

	public static ClienteResponse from(Cliente cliente) {
		List<UnidadeConsumidoraResponse> unidades = cliente.getUnidadesConsumidoras().stream()
				.map(UnidadeConsumidoraResponse::from)
				.toList();
		return new ClienteResponse(
				cliente.getId(),
				cliente.getNome(),
				cliente.getDocumento(),
				EnderecoResponse.from(cliente.getEndereco()),
				cliente.isAtivo(),
				cliente.getCreatedAt(),
				cliente.getUpdatedAt(),
				unidades);
	}

}
