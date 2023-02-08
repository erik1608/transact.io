package com.snee.transactio.model.response.oauth2;

import com.google.gson.annotations.Expose;

public class TokenResponse {
    @Expose
    private String access_token;

    @Expose
    private String token_type;

    public String getAccessToken() {
        return access_token;
    }

    public String getTokenType() {
        return token_type;
    }

    public void setAccessToken(String access_token) {
        this.access_token = access_token;
    }

    public void setTokenType(String token_type) {
        this.token_type = token_type;
    }
}
