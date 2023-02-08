package com.snee.transactio.model.request;

import com.google.gson.annotations.Expose;
import com.snee.transactio.exceptions.RequestValidationException;
import com.snee.transactio.model.RequestModel;

public class Device implements RequestModel {
    @Expose
    private String deviceId;

    @Expose
    private String model;

    @Expose
    private String platform;

    @Expose
    private String manufacturer;

    @Expose
    private String version;

    @Expose
    private Push push;

    @Override
    public void validate() {
        if (deviceId == null || deviceId.isEmpty()) {
            throw new RequestValidationException("The device id is missing");
        }

        if (model == null || model.isEmpty()) {
            throw new RequestValidationException("The model is missing");
        }

        if (platform == null || platform.isEmpty()) {
            throw new RequestValidationException("The platform is missing");
        }

        if (manufacturer == null || manufacturer.isEmpty()) {
            throw new RequestValidationException("The manufacturer is missing");
        }

        if (version == null || version.isEmpty()) {
            throw new RequestValidationException("The manufacturer is missing");
        }

        push.validate();
    }

    public String getModel() {
        return model;
    }

    public String getPlatform() {
        return platform;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getVersion() {
        return version;
    }

    public Push getPush() {
        return push;
    }

    public String getDeviceId() {
        return deviceId;
    }
}
