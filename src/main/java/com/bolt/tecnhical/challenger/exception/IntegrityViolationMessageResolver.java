package com.bolt.tecnhical.challenger.exception;

import org.springframework.dao.DataIntegrityViolationException;

final class IntegrityViolationMessageResolver {

	private IntegrityViolationMessageResolver() {
	}

	static String resolver(DataIntegrityViolationException ex) {
		String causa = obterMensagemCausa(ex).toUpperCase();
		if (contemIndicio(causa, "UK_UC_NUMERO_INSTALACAO", "NUMERO_INSTALACAO")) {
			return "Número de instalação já cadastrado para outro cliente";
		}
		if (contemIndicio(causa, "UK_CLIENTE_DOCUMENTO", "DOCUMENTO")) {
			return "Já existe cliente cadastrado com este documento";
		}
		return "Violação de integridade dos dados";
	}

	private static String obterMensagemCausa(DataIntegrityViolationException ex) {
		if (ex.getMostSpecificCause() != null && ex.getMostSpecificCause().getMessage() != null) {
			return ex.getMostSpecificCause().getMessage();
		}
		return ex.getMessage() != null ? ex.getMessage() : "";
	}

	private static boolean contemIndicio(String mensagem, String... indicadores) {
		for (String indicador : indicadores) {
			if (mensagem.contains(indicador)) {
				return true;
			}
		}
		return false;
	}

}
