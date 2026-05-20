package com.bolt.tecnhical.challenger.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

import com.bolt.tecnhical.challenger.exception.ErrorResponse;

@Configuration
@Order(1)
@ConditionalOnProperty(name = "app.security.enabled", havingValue = "true", matchIfMissing = true)
public class JwtSecurityConfiguration {

	private static final RegexRequestMatcher GET_CLIENTE_POR_ID = new RegexRequestMatcher(
			"^/api/clientes/\\d+$",
			HttpMethod.GET.name());

	@Bean
	SecurityFilterChain jwtFilterChain(
			HttpSecurity http,
			JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(authenticationEntryPoint())
						.accessDeniedHandler(accessDeniedHandler()))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/error", "/error/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
						.requestMatchers("/h2-console/**").permitAll()
						.requestMatchers(
								"/swagger-ui.html",
								"/swagger-ui/**",
								"/v3/api-docs",
								"/v3/api-docs/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/clientes").hasAnyRole("ADMIN", "CLIENTE")
						.requestMatchers(HttpMethod.GET, "/api/clientes/recentes").hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/clientes/inativos").hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/clientes").hasRole("ADMIN")
						.requestMatchers(GET_CLIENTE_POR_ID).hasAnyRole("ADMIN", "CLIENTE")
						.requestMatchers(HttpMethod.PUT, "/api/clientes/**").hasAnyRole("ADMIN", "CLIENTE")
						.requestMatchers(HttpMethod.DELETE, "/api/clientes/**").hasAnyRole("ADMIN", "CLIENTE")
						.anyRequest().authenticated())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	private static AuthenticationEntryPoint authenticationEntryPoint() {
		return (request, response, authException) -> {
			response.setStatus(401);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setCharacterEncoding("UTF-8");
			SecurityJsonMapper.instancia().writeValue(response.getWriter(),
					ErrorResponse.of(401, "Unauthorized",
							"Autenticação necessária. Faça login em /api/auth/login e envie 'Authorization: Bearer <token>'."));
			response.getWriter().flush();
		};
	}

	private static AccessDeniedHandler accessDeniedHandler() {
		return (request, response, accessDeniedException) -> {
			response.setStatus(403);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setCharacterEncoding("UTF-8");
			SecurityJsonMapper.instancia().writeValue(response.getWriter(),
					ErrorResponse.of(403, "Forbidden",
							"Acesso negado. Verifique seu perfil (admin/cliente) e se você está consultando um recurso permitido."));
			response.getWriter().flush();
		};
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	UserDetailsService userDetailsService(SecurityAppProperties props, PasswordEncoder passwordEncoder) {
		var admin = props.users().admin();
		var cliente = props.users().cliente();
		return new InMemoryUserDetailsManager(
				User.builder()
						.username(admin.username())
						.password(passwordEncoder.encode(admin.password()))
						.roles("ADMIN")
						.build(),
				User.builder()
						.username(cliente.username())
						.password(passwordEncoder.encode(cliente.password()))
						.roles("CLIENTE")
						.build());
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

}
