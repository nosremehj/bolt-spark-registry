package com.bolt.tecnhical.challenger.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.bolt.tecnhical.challenger.domain.Cliente;
import com.bolt.tecnhical.challenger.domain.Endereco;
import com.bolt.tecnhical.challenger.domain.UnidadeConsumidora;
import com.bolt.tecnhical.challenger.exception.BusinessException;
import com.bolt.tecnhical.challenger.web.dto.ClienteResponse;

import jakarta.persistence.EntityManager;
import com.bolt.tecnhical.challenger.integration.viacep.ViaCepClient;
import com.bolt.tecnhical.challenger.repository.ClienteRepository;
import com.bolt.tecnhical.challenger.repository.UnidadeConsumidoraRepository;
import com.bolt.tecnhical.challenger.web.dto.ClienteRequest;
import com.bolt.tecnhical.challenger.web.dto.EnderecoRequest;
import com.bolt.tecnhical.challenger.web.dto.UnidadeConsumidoraRequest;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

	@Mock
	private ClienteRepository clienteRepository;

	@Mock
	private UnidadeConsumidoraRepository unidadeConsumidoraRepository;

	@Mock
	private ViaCepClient viaCepClient;

	@Mock
	private EntityManager entityManager;

	@InjectMocks
	private ClienteService clienteService;

	@Test
	void deveRejeitarDocumentoDuplicado() {
		when(clienteRepository.existsByDocumento("12345678901")).thenReturn(true);

		ClienteRequest request = criarRequest("12345678901", "30130010", "UC-001");

		assertThatThrownBy(() -> clienteService.cadastrar(request))
				.isInstanceOf(BusinessException.class)
				.satisfies(ex -> org.assertj.core.api.Assertions.assertThat(
						((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));

		verify(clienteRepository, never()).save(any());
	}

	@Test
	void deveFazerFlushAoAtualizarComMesmoNumeroDeInstalacao() {
		Cliente cliente = clienteComUnidade("52998224725", "INST-1001");
		when(clienteRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(cliente));
		when(clienteRepository.existsByDocumentoAndIdNot(eq("52998224725"), eq(1L))).thenReturn(false);
		when(unidadeConsumidoraRepository.existsByNumeroInstalacaoAndClienteIdNot(eq("INST-1001"), eq(1L)))
				.thenReturn(false);
		when(viaCepClient.buscarEndereco(anyString(), any())).thenReturn(enderecoComUf("MG"));
		when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ClienteRequest request = criarRequest("529.982.247-25", "30130010", "INST-1001");

		ClienteResponse response = clienteService.atualizar(1L, request);

		verify(entityManager).flush();
		assertThat(response.nome()).isEqualTo("João Silva");
		assertThat(response.unidadesConsumidoras()).hasSize(1);
		assertThat(response.unidadesConsumidoras().get(0).numeroInstalacao()).isEqualTo("INST-1001");
	}

	@Test
	void deveListarApenasClientesInativos() {
		Cliente inativo = clienteComUnidade("52998224725", "INST-1001");
		inativo.setAtivo(false);
		when(clienteRepository.findAllInativosWithUnidades()).thenReturn(List.of(inativo));

		var resposta = clienteService.listarInativos();

		assertThat(resposta).hasSize(1);
		assertThat(resposta.get(0).ativo()).isFalse();
		assertThat(resposta.get(0).documento()).isEqualTo("52998224725");
		verify(clienteRepository).findAllInativosWithUnidades();
	}

	@Test
	void deveRetornarListaVaziaQuandoNaoHouverInativos() {
		when(clienteRepository.findAllInativosWithUnidades()).thenReturn(Collections.emptyList());

		assertThat(clienteService.listarInativos()).isEmpty();
	}

	@Test
	void naoDeveFazerFlushNoCadastro() {
		when(clienteRepository.existsByDocumento(anyString())).thenReturn(false);
		when(unidadeConsumidoraRepository.existsByNumeroInstalacao(anyString())).thenReturn(false);
		when(viaCepClient.buscarEndereco(anyString(), any())).thenReturn(enderecoComUf("MG"));
		when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

		clienteService.cadastrar(criarRequest("52998224725", "30130010", "INST-2001"));

		verify(entityManager, never()).flush();
	}

	@Test
	void deveRejeitarUnidadeConsumidoraEmEstadoNaoAtendido() {
		when(clienteRepository.existsByDocumento(anyString())).thenReturn(false);
		when(unidadeConsumidoraRepository.existsByNumeroInstalacao(anyString())).thenReturn(false);
		when(viaCepClient.buscarEndereco(anyString(), any())).thenReturn(enderecoComUf("SP"));

		ClienteRequest request = criarRequest("12345678901", "01310100", "UC-001");

		assertThatThrownBy(() -> clienteService.cadastrar(request))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("SP, RS e PR");
	}

	private ClienteRequest criarRequest(String documento, String cep, String numeroInstalacao) {
		EnderecoRequest endereco = new EnderecoRequest(cep, null);
		UnidadeConsumidoraRequest unidade = new UnidadeConsumidoraRequest("Minha casa", numeroInstalacao, endereco);
		return new ClienteRequest("João Silva", documento, endereco, List.of(unidade));
	}

	private Cliente clienteComUnidade(String documento, String numeroInstalacao) {
		Cliente cliente = new Cliente();
		cliente.setId(1L);
		cliente.setNome("Maria");
		cliente.setDocumento(documento);
		cliente.setEndereco(enderecoComUf("MG"));
		UnidadeConsumidora unidade = new UnidadeConsumidora();
		unidade.setNome("Minha casa");
		unidade.setNumeroInstalacao(numeroInstalacao);
		unidade.setEndereco(enderecoComUf("MG"));
		cliente.adicionarUnidade(unidade);
		return cliente;
	}

	private Endereco enderecoComUf(String uf) {
		Endereco endereco = new Endereco();
		endereco.setCep("01310100");
		endereco.setLogradouro("Av. Paulista");
		endereco.setBairro("Bela Vista");
		endereco.setCidade("São Paulo");
		endereco.setUf(uf);
		return endereco;
	}

}
