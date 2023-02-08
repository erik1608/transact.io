package com.snee.transactio.oauth2.model.client;

import java.util.List;

public class RegisteredClient {
    private String id;
    private String secret;
    private String secretKey;
    private String secretKeyUse;
    private String secretKeyAlg;
    private List<String> redirect_uris;
    private TokenConfiguration token;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSecretKeyAlg() {
        return secretKeyAlg;
    }

    public void setSecretKeyAlg(String secretKeyAlg) {
        this.secretKeyAlg = secretKeyAlg;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public List<String> getRedirectUris() {
        return redirect_uris;
    }

    public void setRedirectUris(List<String> redirect_uris) {
        this.redirect_uris = redirect_uris;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getSecretKeyUse() {
        return secretKeyUse;
    }

    public void setSecretKeyUse(String secretKeyUse) {
        this.secretKeyUse = secretKeyUse;
    }

    public TokenConfiguration getToken() {
        return token;
    }

    public void setToken(TokenConfiguration token) {
        this.token = token;
    }
}
