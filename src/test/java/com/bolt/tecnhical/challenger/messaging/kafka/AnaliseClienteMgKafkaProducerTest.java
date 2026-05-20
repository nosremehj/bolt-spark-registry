package com.bolt.tecnhical.challenger.messaging.kafka;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import com.bolt.tecnhical.challenger.exception.KafkaPublishException;
import com.bolt.tecnhical.challenger.messaging.AnaliseClienteMgEvent;

@ExtendWith(MockitoExtension.class)
class AnaliseClienteMgKafkaProducerTest {

	private static final String TOPICO = "analise_cliente_mg";
	private static final int TIMEOUT_SEGUNDOS = 5;

	@Mock
	private KafkaTemplate<String, Object> kafkaTemplate;

	private AnaliseClienteMgKafkaProducer producer;

	@BeforeEach
	void setUp() {
		KafkaAppProperties properties = new KafkaAppProperties(true, TOPICO, TIMEOUT_SEGUNDOS);
		producer = new AnaliseClienteMgKafkaProducer(kafkaTemplate, properties);
	}

	@Test
	void deveEnviarMensagemNoTopicoConfigurado() {
		AnaliseClienteMgEvent event = new AnaliseClienteMgEvent(
				7L, "12345678901", "Ana Costa", List.of("INST-001", "INST-002"));
		SendResult<String, Object> sendResult = org.mockito.Mockito.mock(SendResult.class);
		RecordMetadata metadata = org.mockito.Mockito.mock(RecordMetadata.class);
		org.mockito.Mockito.when(sendResult.getRecordMetadata()).thenReturn(metadata);
		org.mockito.Mockito.when(metadata.partition()).thenReturn(0);
		when(kafkaTemplate.send(eq(TOPICO), eq("7"), org.mockito.ArgumentMatchers.any()))
				.thenReturn(CompletableFuture.completedFuture(sendResult));

		producer.enviar(event);

		ArgumentCaptor<AnaliseClienteMgMessage> captor = ArgumentCaptor.forClass(AnaliseClienteMgMessage.class);
		verify(kafkaTemplate).send(eq(TOPICO), eq("7"), captor.capture());
		AnaliseClienteMgMessage mensagem = captor.getValue();
		org.assertj.core.api.Assertions.assertThat(mensagem.clienteId()).isEqualTo(7L);
		org.assertj.core.api.Assertions.assertThat(mensagem.documento()).isEqualTo("12345678901");
		org.assertj.core.api.Assertions.assertThat(mensagem.nome()).isEqualTo("Ana Costa");
		org.assertj.core.api.Assertions.assertThat(mensagem.numerosInstalacao()).containsExactly("INST-001", "INST-002");
		org.assertj.core.api.Assertions.assertThat(mensagem.publicadoEm()).isNotNull();
	}

	@Test
	void deveLancarExcecaoQuandoSendLancarRuntimeException() {
		AnaliseClienteMgEvent event = new AnaliseClienteMgEvent(
				2L, "12345678901", "Ana", List.of("INST-002"));
		when(kafkaTemplate.send(eq(TOPICO), eq("2"), org.mockito.ArgumentMatchers.any()))
				.thenThrow(new org.apache.kafka.common.KafkaException("broker offline"));

		assertThatThrownBy(() -> producer.enviar(event))
				.isInstanceOf(KafkaPublishException.class)
				.hasMessageContaining("Serviço de mensageria indisponível");
	}

	@Test
	void deveLancarExcecaoQuandoEnvioFalhar() {
		AnaliseClienteMgEvent event = new AnaliseClienteMgEvent(
				1L, "12345678901", "Ana", List.of("INST-001"));
		CompletableFuture<SendResult<String, Object>> futuro = new CompletableFuture<>();
		futuro.completeExceptionally(new org.apache.kafka.common.errors.TimeoutException("broker down"));
		when(kafkaTemplate.send(eq(TOPICO), eq("1"), org.mockito.ArgumentMatchers.any())).thenReturn(futuro);

		assertThatThrownBy(() -> producer.enviar(event))
				.isInstanceOf(KafkaPublishException.class)
				.hasMessageContaining("Serviço de mensageria indisponível");
	}

}
