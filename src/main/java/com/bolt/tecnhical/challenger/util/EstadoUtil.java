package com.bolt.tecnhical.challenger.util;

import java.util.List;
import java.util.Set;

import com.bolt.tecnhical.challenger.domain.Cliente;
import com.bolt.tecnhical.challenger.domain.UnidadeConsumidora;
import com.bolt.tecnhical.challenger.exception.BusinessException;
import org.springframework.http.HttpStatus;

public final class EstadoUtil {

	private static final Set<String> ESTADOS_NAO_ATENDIDOS = Set.of("SP", "RS", "PR");

	private static final String UF_MG = "MG";

	private EstadoUtil() {
	}

	public static void validarUnidadeConsumidora(String uf) {
		String ufNormalizada = normalizarUf(uf);
		if (ESTADOS_NAO_ATENDIDOS.contains(ufNormalizada)) {
			throw new BusinessException(
					"Não atendemos unidades consumidoras nos estados SP, RS e PR",
					HttpStatus.BAD_REQUEST);
		}
	}

	public static boolean possuiUnidadeEmMg(String uf) {
		return UF_MG.equals(normalizarUf(uf));
	}

	public static boolean clientePossuiUnidadeEmMg(Cliente cliente) {
		return !numerosInstalacaoEmMg(cliente).isEmpty();
	}

	public static List<String> numerosInstalacaoEmMg(Cliente cliente) {
		return cliente.getUnidadesConsumidoras().stream()
				.filter(unidade -> possuiUnidadeEmMg(unidade.getEndereco().getUf()))
				.map(UnidadeConsumidora::getNumeroInstalacao)
				.toList();
	}

	private static String normalizarUf(String uf) {
		if (uf == null || uf.isBlank()) {
			throw new BusinessException("UF do endereço é obrigatória", HttpStatus.BAD_REQUEST);
		}
		return uf.trim().toUpperCase();
	}

}
