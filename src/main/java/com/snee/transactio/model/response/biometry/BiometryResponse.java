package com.snee.transactio.model.response.biometry;

import com.google.gson.annotations.Expose;
import com.snee.transactio.model.ResponseModel;
import com.snee.transactio.model.Session;

public abstract class BiometryResponse implements ResponseModel {
    @Expose
    protected Session sessionData;

    public abstract <T extends BiometryResponse> T setSessionData(Session sessionData);
}
