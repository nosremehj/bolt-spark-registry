package com.bolt.tecnhical.challenger.security;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Associa o usuário ROLE_CLIENTE aos IDs de clientes que ele cadastrou nesta instância (mock em memória).
 */
@Component
public class ClienteOwnershipTracker {

	private final ConcurrentHashMap<String, Set<Long>> idsPorUsuario = new ConcurrentHashMap<>();

	public void registrarCadastro(String username, Long clienteId) {
		idsPorUsuario.compute(username, (k, v) -> {
			if (v == null) {
				v = ConcurrentHashMap.newKeySet();
			}
			v.add(clienteId);
			return v;
		});
	}

	public boolean podeConsultarCliente(String username, Long clienteId) {
		Set<Long> ids = idsPorUsuario.get(username);
		return ids != null && ids.contains(clienteId);
	}

}
