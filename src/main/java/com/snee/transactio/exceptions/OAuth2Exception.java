package com.snee.transactio.exceptions;

public class OAuth2Exception extends RuntimeException {
    private final String error;

    private final String error_description;

    public OAuth2Exception(String error, String error_description) {
        super(error);
        this.error = error;
        this.error_description = error_description;
    }

    public String getError() {
        return error;
    }

    public String getErrorDescription() {
        return error_description;
    }
}
