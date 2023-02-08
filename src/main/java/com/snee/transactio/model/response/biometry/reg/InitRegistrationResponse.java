package com.snee.transactio.model.response.biometry.reg;

import com.google.gson.annotations.Expose;
import com.snee.transactio.model.Session;
import com.snee.transactio.model.response.biometry.BiometryResponse;

public class InitRegistrationResponse extends BiometryResponse {
    @Expose
    private String correlationId;

    @Expose
    private String challenge;

    @Override
    public <T extends BiometryResponse> T setSessionData(Session sessionData) {
        //noinspection unchecked
        return (T) this;
    }

    public InitRegistrationResponse setChallenge(String challenge) {
        this.challenge = challenge;
        return this;
    }

    public InitRegistrationResponse setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    public String getChallenge() {
        return challenge;
    }

    public String getCorrelationId() {
        return correlationId;
    }
}
