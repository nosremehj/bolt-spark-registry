package com.bolt.tecnhical.challenger.exception;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class CampoValidacaoFormatter {

	private static final Pattern UNIDADE_CONSUMIDORA = Pattern.compile("unidadesConsumidoras\\[(\\d+)]\\.(.+)");

	private CampoValidacaoFormatter() {
	}

	static String formatar(String field) {
		if (field == null || field.isBlank()) {
			return "Campo";
		}
		Matcher unidade = UNIDADE_CONSUMIDORA.matcher(field);
		if (unidade.matches()) {
			int numero = Integer.parseInt(unidade.group(1)) + 1;
			return "Unidade consumidora " + numero + " — " + formatarSubcampo(unidade.group(2));
		}
		if (field.startsWith("endereco.")) {
			return "Endereço do cliente — " + rotulo(field.substring("endereco.".length()));
		}
		return rotulo(field);
	}

	private static String formatarSubcampo(String path) {
		if (path.startsWith("endereco.")) {
			return "Endereço — " + rotulo(path.substring("endereco.".length()));
		}
		return rotulo(path);
	}

	private static String rotulo(String nome) {
		return switch (nome) {
			case "cep" -> "CEP";
			case "nome" -> "Nome";
			case "documento" -> "Documento";
			case "numeroInstalacao" -> "Número de instalação";
			case "complemento" -> "Complemento";
			case "unidadesConsumidoras" -> "Unidades consumidoras";
			case "endereco" -> "Endereço";
			default -> nome;
		};
	}

}
