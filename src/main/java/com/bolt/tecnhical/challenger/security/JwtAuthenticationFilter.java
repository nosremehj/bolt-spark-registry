package com.bolt.tecnhical.challenger.security;

import java.io.IOException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.bolt.tecnhical.challenger.exception.ErrorResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@ConditionalOnProperty(name = "app.security.enabled", havingValue = "true", matchIfMissing = true)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtService jwtService;

	public JwtAuthenticationFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null && header.startsWith(BEARER_PREFIX)) {
			String raw = header.substring(BEARER_PREFIX.length()).trim();
			var result = jwtService.parseWithResult(raw);
			if (result instanceof JwtService.JwtParseResult.Invalid) {
				escreverUnauthorized(response,
						"Token inválido ou expirado. Faça login em /api/auth/login e envie 'Authorization: Bearer <token>'.");
				return;
			}
			if (result instanceof JwtService.JwtParseResult.Valid valid) {
				var auth = valid.authentication();
				var tokenAuth = new UsernamePasswordAuthenticationToken(
						auth.username(),
						null,
						auth.authorities());
				tokenAuth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(tokenAuth);
			}
		}
		filterChain.doFilter(request, response);
	}

	private static void escreverUnauthorized(HttpServletResponse response, String message) throws IOException {
		response.setStatus(401);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		SecurityJsonMapper.instancia().writeValue(response.getWriter(), ErrorResponse.of(401, "Unauthorized", message));
		response.getWriter().flush();
	}

}
