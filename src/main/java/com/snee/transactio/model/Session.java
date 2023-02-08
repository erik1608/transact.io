package com.snee.transactio.model;

import com.google.gson.annotations.Expose;
import com.snee.transactio.exceptions.RequestValidationException;

public class Session implements RequestModel, ResponseModel {
    @Expose
    private String sessionKey;

    @Expose
    private String subject;

    @Expose
    private Long expiry;

    public String getKey() {
        return sessionKey;
    }

    public void setKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Long getExpiry() {
        return expiry;
    }

    public void setExpiry(Long expiry) {
        this.expiry = expiry;
    }

    @Override
    public void validate() {
        if (sessionKey == null || sessionKey.isEmpty()) {
            throw new RequestValidationException("JWT token is missing");
        }

        if (subject == null || subject.isEmpty()) {
            throw new RequestValidationException("Subject is missing");
        }

        if (expiry == null || expiry.intValue() <= 0) {
            throw new RequestValidationException("Subject is missing");
        }
    }
}
