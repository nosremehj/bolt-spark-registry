package com.bolt.tecnhical.challenger.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Mapper usado em respostas JSON escritas fora do {@code HttpMessageConverter} do MVC
 * (ex.: filtros e handlers do Spring Security). Precisa de {@link JavaTimeModule} para
 * {@code java.time.Instant} em {@code ErrorResponse}.
 */
public final class SecurityJsonMapper {

	private static final ObjectMapper INSTANCE = criar();

	private SecurityJsonMapper() {
	}

	private static ObjectMapper criar() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		return mapper;
	}

	public static ObjectMapper instancia() {
		return INSTANCE;
	}
}
