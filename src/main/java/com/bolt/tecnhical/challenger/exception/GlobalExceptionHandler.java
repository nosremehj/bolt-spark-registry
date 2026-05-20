package com.bolt.tecnhical.challenger.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
		HttpStatus status = ex.getStatus();
		return ResponseEntity.status(status)
				.body(ErrorResponse.of(status.value(), status.getReasonPhrase(), ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
		List<String> details = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
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
