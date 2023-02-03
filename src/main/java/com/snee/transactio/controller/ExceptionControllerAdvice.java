package com.snee.transactio.controller;


import com.snee.transactio.exceptions.BadSessionException;
import com.snee.transactio.exceptions.OAuth2Exception;
import com.snee.transactio.exceptions.RequestValidationException;
import com.google.gson.JsonObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

	@ExceptionHandler(RequestValidationException.class)
	protected ResponseEntity<Object> handleValidationError(RequestValidationException ex, WebRequest request) {
		JsonObject response = new JsonObject();
		response.addProperty("message", ex.getMessage());
		return handleExceptionInternal(ex, response.toString(), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
	}

	@ExceptionHandler(OAuth2Exception.class)
	protected ResponseEntity<Object> handleOAuthError(OAuth2Exception ex, WebRequest request) {
		JsonObject response = new JsonObject();
		response.addProperty("error", ex.getError());
		response.addProperty("error_description", ex.getErrorDescription());
		return handleExceptionInternal(ex, response.toString(), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
	}

	@ExceptionHandler(BadSessionException.class)
	protected ResponseEntity<Object> handleBadSession(BadSessionException ex, WebRequest request) {
		JsonObject response = new JsonObject();
		response.addProperty("message", ex.getMessage());
		return handleExceptionInternal(ex, response.toString(), new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
	}
}
