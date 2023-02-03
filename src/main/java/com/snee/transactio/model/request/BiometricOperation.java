package com.snee.transactio.model.request;

public enum BiometricOperation {
    INIT_REG,
    FINISH_REG,
    DELETE_REG,

    INIT_AUTH,
    FINISH_AUTH;

    public boolean isInit() {
        return this.equals(INIT_AUTH) || this.equals(INIT_REG);
    }
}
