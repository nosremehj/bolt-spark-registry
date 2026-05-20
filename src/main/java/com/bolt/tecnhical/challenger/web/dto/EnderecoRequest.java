package com.bolt.tecnhical.challenger.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EnderecoRequest(
		@NotBlank(message = "CEP é obrigatório")
		@Pattern(regexp = "^\\d{8}$|^\\d{5}-?\\d{3}$", message = "CEP inválido")
		String cep,
		String complemento) {
}
