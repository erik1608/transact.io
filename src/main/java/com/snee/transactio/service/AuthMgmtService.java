package com.snee.transactio.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.snee.transactio.component.JWT;
import com.snee.transactio.component.OAuth2;
import com.snee.transactio.exceptions.BadSessionException;
import com.snee.transactio.exceptions.RequestValidationException;
import com.snee.transactio.model.Session;
import com.snee.transactio.model.request.BiometricRequest;
import com.snee.transactio.model.request.oauth2.AuthCodeRequest;
import com.snee.transactio.model.request.oauth2.TokenRequest;
import com.snee.transactio.model.response.biometry.BiometryResponse;
import com.snee.transactio.model.response.oauth2.AuthCodeResponse;
import com.snee.transactio.model.response.oauth2.TokenResponse;
import com.snee.transactio.oauth2.model.AccessToken;
import org.springframework.context.annotation.DependsOn;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.ParseException;

@Service
@DependsOn({"OAuth2", "JWT"})
public class AuthMgmtService {

    public enum BiometryOp {
        REG,
        AUTH
    }

    private final BiometryService mBiometryService;
    private final JWT mJWT;
    private final OAuth2 mOAuth2;

    public AuthMgmtService(BiometryService biometryService,
                           JWT jwt,
                           OAuth2 adapterFactory) {

        mBiometryService = biometryService;
        mJWT = jwt;
        mOAuth2 = adapterFactory;
    }

    public Session createSession(String subject) {
        try {
            Session session = new Session();
            session.setKey(mJWT.createTokenWithDefaultExpiry(subject));
            session.setExpiry(JWT.TOKEN_LIFETIME / 1000); // Set expiry in seconds.
            session.setSubject(subject);
            return session;
        } catch (JOSEException e) {
            return null;
        }
    }

    public Session validateSession(Session session) {
        try {
            return createSession(mJWT.validate(session.getKey()).getSubject());
        } catch (ParseException | BadJOSEException | JOSEException e) {
            throw new BadSessionException("Failed to validate the session", e);
        }
    }

    public BiometryResponse processBiometryOp(@NonNull BiometricRequest request, @NonNull BiometryOp op) {
        BiometryResponse response;
        switch (op) {
            case REG:
                // Validate the user session. For the registration request.
                Session session = validateSession(request.getSessionData());
                request.setSubject(session.getSubject());
                switch (request.getOperation()) {
                    case INIT_REG:
                        response = mBiometryService.initRegistration(request);
                        break;
                    case FINISH_REG:
                        response = mBiometryService.register(request);
                        break;
                    case DELETE_REG:
                        response = mBiometryService.deleteReg(request);
                        break;
                    default:
                        throw new RequestValidationException("Unknown operation provided");
                }

                // Set the sessionData to the response.
                response.setSessionData(session);
                return response;
            case AUTH:
                switch (request.getOperation()) {
                    case INIT_AUTH:
                        response = mBiometryService.initAuth(request);
                        break;
                    case FINISH_AUTH:
                        response = mBiometryService.auth(request);
                        response.setSessionData(createSession(request.getSubject()));
                        break;
                    default:
                        throw new RequestValidationException("Unknown operation provided");
                }
                return response;
            default:
                throw new RequestValidationException("Unknown operation!");
        }

    }

    @Transactional
    public synchronized AuthCodeResponse generateOAuthAuthorizeCode(AuthCodeRequest request) {
        AuthCodeResponse response = new AuthCodeResponse();
        response.setCode(mOAuth2.generateAuthCode(request));
        response.setState(request.getState());
        return response;
    }

    @Transactional
    public synchronized TokenResponse generateOAuthAccessToken(TokenRequest request, String clientAuthorization) {
        AccessToken accessToken = mOAuth2.generateAccessToken(request, clientAuthorization);
        TokenResponse response = new TokenResponse();
        response.setAccessToken(accessToken.getToken());
        response.setTokenType(accessToken.getTokenType());
        return response;
    }

}
