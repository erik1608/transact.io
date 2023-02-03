package com.snee.transactio.oauth2;

/**
 * An enum representing the suppored
 */
public enum OAuthAdapterAlgorithm {
    AES_ENC("aes", "enc");

    final String name, use;

    OAuthAdapterAlgorithm(String name, String use) {
        this.name = name;
        this.use = use;
    }

    public String getName() {
        return name;
    }

    public String getUse() {
        return use;
    }
}
