package com.bolt.tecnhical.challenger.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UnidadeConsumidoraRequest(
		@NotBlank(message = "Nome da unidade consumidora é obrigatório")
		String nome,
		@NotBlank(message = "Número de instalação é obrigatório")
		String numeroInstalacao,
		@NotNull(message = "Endereço da unidade consumidora é obrigatório")
		@Valid
		EnderecoRequest endereco) {
}
