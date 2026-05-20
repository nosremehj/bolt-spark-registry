package com.bolt.tecnhical.challenger.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bolt.tecnhical.challenger.service.ClienteService;
import com.bolt.tecnhical.challenger.web.dto.ClienteRequest;
import com.bolt.tecnhical.challenger.web.dto.ClienteResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Clientes", description = "Cadastro e manutenção de clientes")
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

	private final ClienteService clienteService;

	public ClienteController(ClienteService clienteService) {
		this.clienteService = clienteService;
	}

	@Operation(summary = "Cadastrar cliente")
	@PostMapping
	public ResponseEntity<ClienteResponse> cadastrar(@Valid @RequestBody ClienteRequest request) {
		ClienteResponse response = clienteService.cadastrar(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Operation(summary = "Atualizar cliente")
	@PutMapping("/{id}")
	public ResponseEntity<ClienteResponse> atualizar(
			@PathVariable Long id,
			@Valid @RequestBody ClienteRequest request) {
		return ResponseEntity.ok(clienteService.atualizar(id, request));
	}

	@Operation(summary = "Remover cliente (exclusão lógica)")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> remover(@PathVariable Long id) {
		clienteService.remover(id);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Listar todos os clientes ativos")
	@GetMapping
	public ResponseEntity<List<ClienteResponse>> listarTodos() {
		return ResponseEntity.ok(clienteService.listarTodos());
	}

	@Operation(summary = "Listar os últimos 20 clientes cadastrados")
	@GetMapping("/recentes")
	public ResponseEntity<List<ClienteResponse>> listarRecentes() {
		return ResponseEntity.ok(clienteService.listarUltimosCadastrados());
	}

	@Operation(summary = "Buscar cliente por ID")
	@GetMapping("/{id}")
	public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(clienteService.buscarPorId(id));
	}

}
