package com.bolt.tecnhical.challenger.util;

import java.util.Locale;
import java.util.regex.Pattern;

import com.bolt.tecnhical.challenger.exception.BusinessException;
import org.springframework.http.HttpStatus;

public final class DocumentoUtil {

	private static final int TAMANHO_CPF = 11;

	private static final int TAMANHO_CNPJ = 14;

	private static final int TAMANHO_CORPO_CNPJ = 12;

	private static final Pattern FORMATACAO_DOCUMENTO = Pattern.compile("[.\\-/\\s]");

	private static final Pattern APENAS_DIGITOS = Pattern.compile("\\d+");

	private static final Pattern CARACTERES_CORPO_CNPJ = Pattern.compile("[0-9A-Z]+");

	private DocumentoUtil() {
	}

	public static String normalizar(String documento) {
		if (documento == null || documento.isBlank()) {
			throw new BusinessException("Documento é obrigatório", HttpStatus.BAD_REQUEST);
		}
		String limpo = FORMATACAO_DOCUMENTO.matcher(documento.trim()).replaceAll("").toUpperCase(Locale.ROOT);

		return switch (limpo.length()) {
			case TAMANHO_CPF -> validarCpf(limpo);
			case TAMANHO_CNPJ -> validarCnpj(limpo);
			default -> throw new BusinessException(
					"Documento deve ter 11 caracteres (CPF) ou 14 caracteres (CNPJ)",
					HttpStatus.BAD_REQUEST);
		};
	}

	private static String validarCpf(String limpo) {
		if (!APENAS_DIGITOS.matcher(limpo).matches()) {
			throw new BusinessException("CPF deve conter apenas números", HttpStatus.BAD_REQUEST);
		}
		return limpo;
	}

	private static String validarCnpj(String limpo) {
		String corpo = limpo.substring(0, TAMANHO_CORPO_CNPJ);
		String digitosVerificadores = limpo.substring(TAMANHO_CORPO_CNPJ);

		if (!CARACTERES_CORPO_CNPJ.matcher(corpo).matches()) {
			throw new BusinessException(
					"CNPJ inválido: os 12 primeiros caracteres devem ser alfanuméricos (0-9 ou A-Z)",
					HttpStatus.BAD_REQUEST);
		}
		if (!APENAS_DIGITOS.matcher(digitosVerificadores).matches()) {
			throw new BusinessException(
					"CNPJ inválido: os 2 últimos caracteres devem ser dígitos verificadores numéricos",
					HttpStatus.BAD_REQUEST);
		}
		return limpo;
	}

}
