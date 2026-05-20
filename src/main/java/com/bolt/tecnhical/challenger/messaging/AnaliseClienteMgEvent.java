package com.bolt.tecnhical.challenger.messaging;

import java.util.List;

public record AnaliseClienteMgEvent(
		Long clienteId,
		String documento,
		String nome,
		List<String> numerosInstalacao) {
}
