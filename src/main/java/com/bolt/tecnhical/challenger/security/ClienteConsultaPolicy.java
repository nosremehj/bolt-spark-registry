package com.bolt.tecnhical.challenger.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Quando a segurança está ativa, restringe consulta e alteração por id para o perfil {@code CLIENTE}
 * aos registros que ele cadastrou nesta instância (rastreio em memória).
 */
@Component
public class ClienteConsultaPolicy {

	private final boolean securityEnabled;
	private final ClienteOwnershipTracker ownershipTracker;

	public ClienteConsultaPolicy(
			@Value("${app.security.enabled:true}") boolean securityEnabled,
			ClienteOwnershipTracker ownershipTracker) {
		this.securityEnabled = securityEnabled;
		this.ownershipTracker = ownershipTracker;
	}

	public void garantirPodeConsultarPorId(Long clienteId) {
		garantirProprioCadastroSeCliente(clienteId,
				"Você só pode consultar clientes que você cadastrou",
				"Você não tem permissão para consultar este cliente");
	}

	public void garantirPodeAtualizarPorId(Long clienteId) {
		garantirProprioCadastroSeCliente(clienteId,
				"Você só pode alterar clientes que você cadastrou",
				"Você não tem permissão para alterar este cliente");
	}

	public void garantirPodeRemoverPorId(Long clienteId) {
		garantirProprioCadastroSeCliente(clienteId,
				"Você só pode remover clientes que você cadastrou",
				"Você não tem permissão para remover este cliente");
	}

	private void garantirProprioCadastroSeCliente(
			Long clienteId,
			String mensagemSeClienteSemPosse,
			String mensagemPerfilInvalido) {
		if (!securityEnabled) {
			return;
		}
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			return;
		}
		if (isAdmin(auth)) {
			return;
		}
		if (isCliente(auth)) {
			if (ownershipTracker.podeConsultarCliente(auth.getName(), clienteId)) {
				return;
			}
			throw new AccessDeniedException(mensagemSeClienteSemPosse);
		}
		throw new AccessDeniedException(mensagemPerfilInvalido);
	}

	private static boolean isAdmin(Authentication auth) {
		return auth.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.anyMatch("ROLE_ADMIN"::equals);
	}

	private static boolean isCliente(Authentication auth) {
		return auth.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.anyMatch("ROLE_CLIENTE"::equals);
	}

}
