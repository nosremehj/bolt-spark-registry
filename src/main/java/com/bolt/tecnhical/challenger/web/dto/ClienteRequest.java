package com.bolt.tecnhical.challenger.web.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ClienteRequest(
		@NotBlank(message = "Nome é obrigatório")
		String nome,
		@NotBlank(message = "Documento é obrigatório")
		String documento,
		@NotNull(message = "Endereço do cliente é obrigatório")
		@Valid
		EnderecoRequest endereco,
		@NotEmpty(message = "Informe ao menos uma unidade consumidora")
		@Valid
		List<UnidadeConsumidoraRequest> unidadesConsumidoras) {
}
