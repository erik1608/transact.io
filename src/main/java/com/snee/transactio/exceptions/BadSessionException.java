package com.snee.transactio.exceptions;

public class BadSessionException extends SecurityException {
	public BadSessionException() {
		super();
	}

	public BadSessionException(String message) {
		super(message);
	}

	public BadSessionException(String message, Throwable cause) {
		super(message, cause);
	}

	public BadSessionException(Throwable cause) {
		super(cause);
	}
}
