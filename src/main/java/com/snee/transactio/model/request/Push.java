package com.snee.transactio.model.request;

import com.google.gson.annotations.Expose;
import com.snee.transactio.exceptions.RequestValidationException;
import com.snee.transactio.model.RequestModel;

public class Push implements RequestModel {

    @Expose
    private String registrationId;

    @Override
    public void validate() {
        if (registrationId == null || registrationId.isEmpty()) {
            throw new RequestValidationException(
                    "The push notification reg id is missing"
            );
        }
    }

    public String getRegistrationId() {
        return registrationId;
    }
}
