package com.bolt.tecnhical.challenger.messaging.kafka;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public record AnaliseClienteMgMessage(
		Long clienteId,
		String documento,
		String nome,
		List<String> numerosInstalacao,
		@JsonFormat(shape = JsonFormat.Shape.STRING)
		Instant publicadoEm) {
}
