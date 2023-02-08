package com.snee.transactio.model.response.biometry.reg;

import com.google.gson.annotations.Expose;
import com.snee.transactio.model.Session;
import com.snee.transactio.model.response.biometry.BiometryResponse;

public class FinishRegistrationResponse extends BiometryResponse {
    @Expose
    private String status;

    @Expose
    private Integer regId;

    @Override
    public <T extends BiometryResponse> T setSessionData(Session sessionData) {
        this.sessionData = sessionData;

        //noinspection unchecked
        return (T) this;
    }

    public String getStatus() {
        return status;
    }

    public FinishRegistrationResponse setStatus(String status) {
        this.status = status;
        return this;
    }

    public Integer getRegId() {
        return regId;
    }

    public FinishRegistrationResponse setRegId(Integer regId) {
        this.regId = regId;
        return this;
    }
}
