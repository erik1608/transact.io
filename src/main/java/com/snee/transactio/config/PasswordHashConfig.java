package com.snee.transactio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "user.default.password.hash")
public class PasswordHashConfig {
    private String length;
    private String algo;
    private String saltLength;
    private String iterCount;

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getAlgo() {
        return algo;
    }

    public void setAlgo(String algo) {
        this.algo = algo;
    }

    public String getSaltLength() {
        return saltLength;
    }

    public void setSaltLength(String saltLength) {
        this.saltLength = saltLength;
    }

    public String getIterCount() {
        return iterCount;
    }

    public void setIterCount(String iterCount) {
        this.iterCount = iterCount;
    }
}
