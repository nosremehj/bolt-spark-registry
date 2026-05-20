package com.bolt.tecnhical.challenger.util;

import com.bolt.tecnhical.challenger.exception.BusinessException;
import org.springframework.http.HttpStatus;

public final class DocumentoUtil {

	private DocumentoUtil() {
	}

	public static String normalizar(String documento) {
		if (documento == null || documento.isBlank()) {
			throw new BusinessException("Documento é obrigatório", HttpStatus.BAD_REQUEST);
		}
		String apenasDigitos = documento.replaceAll("\\D", "");
		if (apenasDigitos.length() != 11 && apenasDigitos.length() != 14) {
			throw new BusinessException("Documento deve ser um CPF (11 dígitos) ou CNPJ (14 dígitos)",
					HttpStatus.BAD_REQUEST);
		}
		return apenasDigitos;
	}

}
