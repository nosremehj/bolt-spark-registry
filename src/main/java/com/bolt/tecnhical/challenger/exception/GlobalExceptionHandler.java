package com.bolt.tecnhical.challenger.exception;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(ErrorResponse.of(HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage()));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
		String message = ex.getMessage() != null && !ex.getMessage().isBlank()
				? ex.getMessage()
				: "Você não tem permissão para este recurso";
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(ErrorResponse.of(HttpStatus.FORBIDDEN.value(), "Forbidden", message));
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(ErrorResponse.of(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", "Usuário ou senha inválidos"));
	}

	@ExceptionHandler(KafkaPublishException.class)
	public ResponseEntity<ErrorResponse> handleKafkaPublish(KafkaPublishException ex) {
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body(ErrorResponse.of(
						HttpStatus.SERVICE_UNAVAILABLE.value(),
						"Service Unavailable",
						ex.getMessage()));
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
		HttpStatus status = ex.getStatus();
		return ResponseEntity.status(status)
				.body(ErrorResponse.of(status.value(), status.getReasonPhrase(), ex.getMessage()));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
		String mensagem = IntegrityViolationMessageResolver.resolver(ex);
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(ErrorResponse.of(HttpStatus.CONFLICT.value(), "Conflict", mensagem));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
		List<FieldErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> new FieldErrorDetail(
						CampoValidacaoFormatter.formatar(error.getField()),
						error.getDefaultMessage()))
				.toList();
		return ResponseEntity.badRequest()
				.body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "Bad Request", "Dados inválidos", details));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex) {
		String message = resolverMensagemCorpoRequisicao(ex);
		return ResponseEntity.badRequest()
				.body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "Bad Request", message));
	}

	private String resolverMensagemCorpoRequisicao(HttpMessageNotReadableException ex) {
		String detalhe = ex.getMessage() != null ? ex.getMessage() : "";
		if (detalhe.contains("Required request body is missing")) {
			return "Nenhum dado foi enviado no corpo da requisição";
		}
		return "Corpo da requisição inválido ou mal formatado";
	}

}
