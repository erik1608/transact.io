package com.snee.transactio.model.response;

import com.google.gson.annotations.Expose;
import com.snee.transactio.model.ResponseModel;
import com.snee.transactio.model.Session;
import org.springframework.http.HttpStatus;

public class LoginResponse implements ResponseModel {
    @Expose
    private Session sessionData;

    @Expose
    private HttpStatus status;

    public Session getSessionData() {
        return sessionData;
    }

    public void setSessionData(Session sessionData) {
        this.sessionData = sessionData;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }
}
