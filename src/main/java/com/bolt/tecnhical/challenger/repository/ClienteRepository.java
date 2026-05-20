package com.bolt.tecnhical.challenger.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bolt.tecnhical.challenger.domain.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

	boolean existsByDocumento(String documento);

	boolean existsByDocumentoAndIdNot(String documento, Long id);

	Optional<Cliente> findByIdAndAtivoTrue(Long id);

	List<Cliente> findByAtivoTrue();

	List<Cliente> findByAtivoTrueOrderByCreatedAtDesc(Pageable pageable);

	@Query("SELECT c FROM Cliente c LEFT JOIN FETCH c.unidadesConsumidoras WHERE c.id = :id AND c.ativo = true")
	Optional<Cliente> findByIdAndAtivoTrueWithUnidades(@Param("id") Long id);

	@Query("SELECT DISTINCT c FROM Cliente c LEFT JOIN FETCH c.unidadesConsumidoras WHERE c.ativo = true")
	List<Cliente> findAllAtivosWithUnidades();

	@Query("""
			SELECT DISTINCT c FROM Cliente c
			LEFT JOIN FETCH c.unidadesConsumidoras
			WHERE c.ativo = true
			ORDER BY c.createdAt DESC
			""")
	List<Cliente> findUltimosAtivosWithUnidades(Pageable pageable);

}
