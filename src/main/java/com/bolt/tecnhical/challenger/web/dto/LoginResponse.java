package com.bolt.tecnhical.challenger.web.dto;

public record LoginResponse(
		String accessToken,
		String tokenType,
		long expiresInSeconds) {
}
