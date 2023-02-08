package com.snee.transactio.model.request;

import com.google.gson.annotations.Expose;
import com.snee.transactio.exceptions.RequestValidationException;
import com.snee.transactio.model.RequestModel;
import com.snee.transactio.model.Session;

public class LoginRequest implements RequestModel {

    @Expose
    private String usernameOrEmail;

    @Expose
    private String password;

    @Expose
    private String mode;

    @Expose
    private Session sessionData;

    @Expose
    private Device deviceInfo;

    public void validate() {
        if ("create".equals(mode)) {
            validateUsernameOrEmail();
            validatePassword();
            if (deviceInfo != null) {
                deviceInfo.validate();
            }
        } else if ("renew".equals(mode)) {
            validateSessionData();
        }
    }

    private void validateSessionData() {
        if (sessionData == null) {
            throw new RequestValidationException(
                    "Session data is required"
            );
        }
        sessionData.validate();
    }

    private void validatePassword() {
        if (password == null || password.isEmpty()) {
            throw new RequestValidationException(
                    "Password is required"
            );
        }
    }

    private void validateUsernameOrEmail() {
        if (usernameOrEmail == null || usernameOrEmail.isEmpty()) {
            throw new RequestValidationException(
                    "Username or email is required"
            );
        }
    }

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public String getPassword() {
        return password;
    }

    public String getMode() {
        return mode;
    }

    public Session getSessionData() {
        return sessionData;
    }

    public Device getDeviceInfo() {
        return deviceInfo;
    }
}
