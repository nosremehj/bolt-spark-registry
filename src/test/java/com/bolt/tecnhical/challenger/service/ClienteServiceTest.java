package com.bolt.tecnhical.challenger.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.bolt.tecnhical.challenger.domain.Endereco;
import com.bolt.tecnhical.challenger.exception.BusinessException;
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
