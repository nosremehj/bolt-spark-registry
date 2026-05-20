package com.bolt.tecnhical.challenger.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityAppProperties(
		boolean enabled,
		Jwt jwt,
		Users users) {

	public record Jwt(String secret, long expirationSeconds) {
	}

	public record Users(Credentials admin, Credentials cliente) {
		public record Credentials(String username, String password) {
		}
	}
}
