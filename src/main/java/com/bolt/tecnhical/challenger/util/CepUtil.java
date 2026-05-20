package com.bolt.tecnhical.challenger.util;

import com.bolt.tecnhical.challenger.exception.BusinessException;
import org.springframework.http.HttpStatus;

public final class CepUtil {

	private CepUtil() {
	}

	public static String normalizar(String cep) {
		if (cep == null || cep.isBlank()) {
			throw new BusinessException("CEP é obrigatório", HttpStatus.BAD_REQUEST);
		}
		String apenasDigitos = cep.replaceAll("\\D", "");
		if (apenasDigitos.length() != 8) {
			throw new BusinessException("CEP deve conter 8 dígitos", HttpStatus.BAD_REQUEST);
		}
		return apenasDigitos;
	}

}
