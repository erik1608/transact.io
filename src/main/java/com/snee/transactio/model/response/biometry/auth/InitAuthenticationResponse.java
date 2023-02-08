package com.snee.transactio.model.response.biometry.auth;

import com.google.gson.annotations.Expose;
import com.snee.transactio.model.Session;
import com.snee.transactio.model.response.biometry.BiometryResponse;

public class InitAuthenticationResponse extends BiometryResponse {

    @Expose
    private String correlationId;

    private String challenge;

    @Override
    public <T extends BiometryResponse> T setSessionData(Session sessionData) {
        return (T) this;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getChallenge() {
        return challenge;
    }
}
