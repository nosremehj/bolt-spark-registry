package com.bolt.tecnhical.challenger.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bolt.tecnhical.challenger.domain.UnidadeConsumidora;

public interface UnidadeConsumidoraRepository extends JpaRepository<UnidadeConsumidora, Long> {

	boolean existsByNumeroInstalacao(String numeroInstalacao);

	boolean existsByNumeroInstalacaoAndClienteIdNot(String numeroInstalacao, Long clienteId);

}
