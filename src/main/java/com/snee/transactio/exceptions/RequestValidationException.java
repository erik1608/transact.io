package com.snee.transactio.exceptions;

public class RequestValidationException extends RuntimeException {
    public RequestValidationException() {
        super();
    }

    public RequestValidationException(String message) {
        super(message);
    }

    public RequestValidationException(Throwable cause) {
        super(cause);
    }

    public RequestValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
