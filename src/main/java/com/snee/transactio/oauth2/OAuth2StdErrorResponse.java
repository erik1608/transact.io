package com.snee.transactio.oauth2;

import com.snee.transactio.exceptions.OAuth2Exception;

// OAuth Authorization server error responses (RFC 6749).
public enum OAuth2StdErrorResponse {
    /**
     * The request is missing a required parameter, includes an invalid parameter value, includes a parameter more than  once, or is otherwise malformed.
     */
    INVALID_REQUEST(new OAuth2Exception("invalid_request", "The request is missing a required parameter, or is malformed")),

    /**
     * The client is not authorized to request an authorization code using this method.
     */
    UNAUTHORIZED_CLIENT(new OAuth2Exception("unauthorized_client", "The client is not authorized to request an authorization code using this method.")),

    /**
     * The resource owner or authorization server denied the request.
     */
    ACCESS_DENIED(new OAuth2Exception("access_denied", "The resource owner or authorization server denied the request.")),

    /**
     * The authorization server does not support obtaining an authorization code using this method.
     */
    UNSUPPORTED_RESPONSE_TYPE(new OAuth2Exception("unsupported_response_type", "The authorization server does not support obtaining an authorization code using this method.")),

    /**
     * The requested scope is invalid, unknown, or malformed.
     */
    INVALID_SCOPE(new OAuth2Exception("invalid_scope", "The requested scope is invalid, unknown, or malformed")),

    /**
     * The authorization server encountered an unexpected condition that prevented it from fulfilling the request.
     * (This error code is needed because a 500 Internal Server Error HTTP status code cannot be returned to the client via an HTTP redirect.)
     */
    SERVER_ERROR(new OAuth2Exception("server_error", "The authorization server encountered an unexpected error")),

    /**
     * The authorization server is currently unable to handle the request due to a temporary overloading or maintenance
     * of the server. (This error code is needed because a 503 Service Unavailable HTTP status code cannot be returned to the client via an HTTP redirect.)
     */
    TEMPORARILY_UNAVAILABLE(new OAuth2Exception("temporarily_unavailable", "The authorization server is currently unable"));

    private final OAuth2Exception mException;

    OAuth2StdErrorResponse(OAuth2Exception errorType) {
        mException = errorType;
    }

    public OAuth2Exception getException() {
        return mException;
    }
}
