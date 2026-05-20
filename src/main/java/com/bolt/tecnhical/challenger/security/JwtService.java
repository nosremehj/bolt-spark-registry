package com.bolt.tecnhical.challenger.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
@ConditionalOnProperty(name = "app.security.enabled", havingValue = "true", matchIfMissing = true)
public class JwtService {

	private static final int SECRET_MIN_BYTES = 32;

	private final SecretKey secretKey;
	private final long expirationSeconds;

	public JwtService(SecurityAppProperties securityProps) {
		var jwt = securityProps.jwt();
		byte[] keyBytes = jwt.secret().getBytes(StandardCharsets.UTF_8);
		if (keyBytes.length < SECRET_MIN_BYTES) {
			throw new IllegalStateException(
					"app.security.jwt.secret deve ter pelo menos " + SECRET_MIN_BYTES + " bytes (UTF-8)");
		}
		this.secretKey = Keys.hmacShaKeyFor(keyBytes);
		this.expirationSeconds = jwt.expirationSeconds();
	}

	public String gerarToken(UserDetails user) {
		Instant now = Instant.now();
		List<String> roles = user.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.toList());
		return Jwts.builder()
				.subject(user.getUsername())
				.claim("roles", roles)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plusSeconds(expirationSeconds)))
				.signWith(secretKey)
				.compact();
	}

	public Optional<JwtAuthentication> tryParse(String token) {
		if (token == null || token.isBlank()) {
			return Optional.empty();
		}
		try {
			Claims claims = Jwts.parser()
					.verifyWith(secretKey)
					.build()
					.parseSignedClaims(token)
					.getPayload();
			String username = claims.getSubject();
			@SuppressWarnings("unchecked")
			List<String> roles = claims.get("roles", List.class);
			if (username == null || username.isBlank() || roles == null || roles.isEmpty()) {
				return Optional.empty();
			}
			List<GrantedAuthority> authorities = roles.stream()
					.map(SimpleGrantedAuthority::new)
					.collect(Collectors.toList());
			return Optional.of(new JwtAuthentication(username, authorities));
		}
		catch (JwtException | IllegalArgumentException ex) {
			return Optional.empty();
		}
	}

	public JwtParseResult parseWithResult(String token) {
		if (token == null || token.isBlank()) {
			return JwtParseResult.missing();
		}
		try {
			Claims claims = Jwts.parser()
					.verifyWith(secretKey)
					.build()
					.parseSignedClaims(token)
					.getPayload();
			String username = claims.getSubject();
			@SuppressWarnings("unchecked")
			List<String> roles = claims.get("roles", List.class);
			if (username == null || username.isBlank() || roles == null || roles.isEmpty()) {
				return JwtParseResult.invalid();
			}
			List<GrantedAuthority> authorities = roles.stream()
					.map(SimpleGrantedAuthority::new)
					.collect(Collectors.toList());
			return JwtParseResult.valid(new JwtAuthentication(username, authorities));
		}
		catch (JwtException | IllegalArgumentException ex) {
			return JwtParseResult.invalid();
		}
	}

	public long getExpirationSeconds() {
		return expirationSeconds;
	}

	public record JwtAuthentication(String username, List<GrantedAuthority> authorities) {
	}

	public sealed interface JwtParseResult permits JwtParseResult.Valid, JwtParseResult.Missing, JwtParseResult.Invalid {

		static JwtParseResult valid(JwtAuthentication authentication) {
			return new Valid(authentication);
		}

		static JwtParseResult missing() {
			return new Missing();
		}

		static JwtParseResult invalid() {
			return new Invalid();
		}

		record Valid(JwtAuthentication authentication) implements JwtParseResult {
		}

		record Missing() implements JwtParseResult {
		}

		record Invalid() implements JwtParseResult {
		}
	}

}
