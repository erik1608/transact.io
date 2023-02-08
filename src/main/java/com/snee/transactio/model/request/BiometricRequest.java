package com.snee.transactio.model.request;

import com.google.gson.annotations.Expose;
import com.snee.transactio.exceptions.RequestValidationException;
import com.snee.transactio.model.RequestModel;
import com.snee.transactio.model.Session;

import java.util.Arrays;
import java.util.List;

public class BiometricRequest implements RequestModel {
    private final List<BiometricOperation> sessionRequiredOps = Arrays.asList(
            BiometricOperation.INIT_REG,
            BiometricOperation.FINISH_REG,
            BiometricOperation.DELETE_REG
    );

    @Expose
    private BiometricOperation operation;

    @Expose
    private String message;

    @Expose
    private Device deviceInfo;

    @Expose
    private Session sessionData;

    @Expose
    private String subject;

    @Override
    public void validate() {
        if (operation == null) {
            throw new RequestValidationException("Operation is required");
        }

        if (sessionRequiredOps.contains(operation)) {
            if (sessionData == null) {
                throw new RequestValidationException("Session is required for this operation");
            } else {
                sessionData.validate();
            }
        } else {
            if (operation.isInit() && (subject == null || subject.isEmpty())) {
                throw new RequestValidationException("Subject is empty");
            }
        }

        if (!operation.isInit() && operation != BiometricOperation.DELETE_REG && (message == null || message.isEmpty())) {
            throw new RequestValidationException("Message is required");
        }

        if (deviceInfo == null) {
            throw new RequestValidationException("Device info is required");
        }

        deviceInfo.validate();
    }

    public BiometricOperation getOperation() {
        return operation;
    }

    public String getMessage() {
        return message;
    }

    public Device getDeviceInfo() {
        return deviceInfo;
    }

    public Session getSessionData() {
        return sessionData;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
