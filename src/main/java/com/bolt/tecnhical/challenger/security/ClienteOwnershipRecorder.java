package com.bolt.tecnhical.challenger.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class ClienteOwnershipRecorder {

	private final ClienteOwnershipTracker ownershipTracker;

	public ClienteOwnershipRecorder(ClienteOwnershipTracker ownershipTracker) {
		this.ownershipTracker = ownershipTracker;
	}

	public void registrarSePerfilCliente(Long clienteId) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			return;
		}
		boolean cliente = auth.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.anyMatch("ROLE_CLIENTE"::equals);
		if (cliente) {
			ownershipTracker.registrarCadastro(auth.getName(), clienteId);
		}
	}

}
