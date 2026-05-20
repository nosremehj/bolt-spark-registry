package com.bolt.tecnhical.challenger.messaging.kafka;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.bolt.tecnhical.challenger.domain.Cliente;
import com.bolt.tecnhical.challenger.domain.Endereco;
import com.bolt.tecnhical.challenger.domain.UnidadeConsumidora;
import com.bolt.tecnhical.challenger.messaging.AnaliseClienteMgEvent;

@ExtendWith(MockitoExtension.class)
class KafkaAnaliseClienteMgNotifierTest {

	@Mock
	private ApplicationEventPublisher eventPublisher;

	@InjectMocks
	private KafkaAnaliseClienteMgNotifier notifier;

	@Test
	void devePublicarEventoQuandoClientePossuiUnidadeEmMg() {
		Cliente cliente = clienteComUnidade("MG", "INST-MG-01");
		cliente.setId(42L);
		cliente.setDocumento("52998224725");
		cliente.setNome("João Silva");

		notifier.notificarSeNecessario(cliente);

		verify(eventPublisher).publishEvent(
				new AnaliseClienteMgEvent(42L, "52998224725", "João Silva", List.of("INST-MG-01")));
	}

	@Test
	void deveIncluirApenasNumerosInstalacaoDasUnidadesEmMg() {
		Cliente cliente = new Cliente();
		cliente.setId(1L);
		cliente.setDocumento("52998224725");
		cliente.setNome("Maria");
		cliente.adicionarUnidade(unidadeComUf("MG", "INST-MG-01"));
		cliente.adicionarUnidade(unidadeComUf("RJ", "INST-RJ-01"));
		cliente.adicionarUnidade(unidadeComUf("MG", "INST-MG-02"));

		notifier.notificarSeNecessario(cliente);

		verify(eventPublisher).publishEvent(
				new AnaliseClienteMgEvent(1L, "52998224725", "Maria", List.of("INST-MG-01", "INST-MG-02")));
	}

	@Test
	void naoDevePublicarEventoQuandoClienteNaoPossuiUnidadeEmMg() {
		Cliente cliente = clienteComUnidade("RJ", "INST-RJ-01");

		notifier.notificarSeNecessario(cliente);

		verify(eventPublisher, never()).publishEvent(any());
	}

	private Cliente clienteComUnidade(String uf, String numeroInstalacao) {
		Cliente cliente = new Cliente();
		cliente.adicionarUnidade(unidadeComUf(uf, numeroInstalacao));
		return cliente;
	}

	private UnidadeConsumidora unidadeComUf(String uf, String numeroInstalacao) {
		Endereco endereco = new Endereco();
		endereco.setUf(uf);
		UnidadeConsumidora unidade = new UnidadeConsumidora();
		unidade.setEndereco(endereco);
		unidade.setNumeroInstalacao(numeroInstalacao);
		return unidade;
	}

}
