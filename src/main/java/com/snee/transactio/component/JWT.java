package com.snee.transactio.component;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

@Component
public class JWT {
    private static final JWSAlgorithm DEFAULT_JWS_ALGO = JWSAlgorithm.HS256;

    @Value("${server.enc.key}")
    private String DEFAULT_ENC_KEY;

    // The token issuer and audience.
    public static final String ISSUER = "https://alexa.pysnippet.org/";
    public static final String AUDIENCE = ISSUER;

    // Session lifetime in ms.
    public static final Long TOKEN_LIFETIME = 3600000L;

    /**
     * Creates a JWT, compatible for authentication in the protected resources.
     *
     * @param sub The subject of the token.
     * @return a signed JWT token.
     * @throws JOSEException When the signing of the token has failed.
     */
    public String createTokenWithDefaultExpiry(String sub) throws JOSEException {
        Date iat = new Date();
        Date exp = new Date(iat.getTime() + TOKEN_LIFETIME);
        return createToken(sub, iat, exp);
    }

    public String createTokenWithNoExpiry(String sub) throws JOSEException {
        Date iat = new Date();
        return createToken(sub, iat, null);
    }

    public String createTokenWithExpiry(String sub, Long lifetime) throws JOSEException {
        Date iat = new Date();
        Date exp = new Date(iat.getTime() + lifetime);
        return createToken(sub, iat, exp);
    }

    private String createToken(String sub, Date iat, Date exp) throws JOSEException {
        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder();
        claimsBuilder.audience(AUDIENCE);
        claimsBuilder.issuer(ISSUER);
        claimsBuilder.issueTime(iat);
        claimsBuilder.jwtID(UUID.randomUUID().toString());
        claimsBuilder.subject(sub);
        claimsBuilder.expirationTime(exp);

        OctetSequenceKey jwk = new OctetSequenceKey
                .Builder(new Base64URL(DEFAULT_ENC_KEY))
                .keyUse(KeyUse.SIGNATURE)
                .keyID("hs_256_sig_key")
                .algorithm(Algorithm.parse(DEFAULT_JWS_ALGO.getName()))
                .build();

        MACSigner signer = new MACSigner(jwk);
        JWSHeader jwsHeader = new JWSHeader
                .Builder(DEFAULT_JWS_ALGO)
                .keyID(jwk.getKeyID()).build();

        SignedJWT jwt = new SignedJWT(jwsHeader, claimsBuilder.build());
        jwt.sign(signer);
        return jwt.serialize();
    }


    /**
     * @param jwtToken The token to be validated.
     * @return The {@link JWTClaimsSet} obtained from the token.
     */
    public JWTClaimsSet validate(String jwtToken) throws BadJOSEException, ParseException, JOSEException {
        OctetSequenceKey jwk = new OctetSequenceKey
                .Builder(new Base64URL(DEFAULT_ENC_KEY))
                .keyUse(KeyUse.SIGNATURE)
                .keyID("hs_256_sig_key")
                .algorithm(Algorithm.parse(DEFAULT_JWS_ALGO.getName()))
                .build();

        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        jwtProcessor.setJWSKeySelector(new JWSVerificationKeySelector<>(
                DEFAULT_JWS_ALGO, new ImmutableJWKSet<>(new JWKSet(jwk))
        ));
        return jwtProcessor.process(jwtToken, null);
    }
}
