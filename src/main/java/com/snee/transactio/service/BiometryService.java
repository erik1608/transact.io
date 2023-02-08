package com.snee.transactio.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.snee.transactio.crypto.CryptoUtils;
import com.snee.transactio.db.entities.user.Biometrics;
import com.snee.transactio.db.entities.user.User;
import com.snee.transactio.db.entities.user.UserDevice;
import com.snee.transactio.db.repo.BiometricsRepo;
import com.snee.transactio.db.repo.Repos;
import com.snee.transactio.exceptions.RequestValidationException;
import com.snee.transactio.model.request.BiometricOperation;
import com.snee.transactio.model.request.BiometricRequest;
import com.snee.transactio.model.request.Device;
import com.snee.transactio.model.response.biometry.BiometryResponse;
import com.snee.transactio.model.response.biometry.auth.FinishAuthenticationResponse;
import com.snee.transactio.model.response.biometry.auth.InitAuthenticationResponse;
import com.snee.transactio.model.response.biometry.reg.DeleteRegistrationResponse;
import com.snee.transactio.model.response.biometry.reg.FinishRegistrationResponse;
import com.snee.transactio.model.response.biometry.reg.InitRegistrationResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class BiometryService {

    private final UserHandlerService mUsersService;

    private final TransactionService mTransactionService;

    private final BiometricsRepo mBiometricsRepo;

    private final Map<String, BiometryCache> mRequestCache = new HashMap<>();

    public BiometryService(
            UserHandlerService userHandlerService,
            Repos repos,
            TransactionService transactionService
    ) {
        // Add the Security provider.
        Security.addProvider(new BouncyCastleProvider());
        mUsersService = userHandlerService;
        mTransactionService = transactionService;
        mBiometricsRepo = repos.get(BiometricsRepo.class);
    }

    @Transactional
    public InitAuthenticationResponse initAuth(BiometricRequest request) {
        getRegIfPresent(request);
        InitAuthenticationResponse response = new InitAuthenticationResponse();
        response.setCorrelationId(UUID.randomUUID().toString());
        response.setChallenge(CryptoUtils.generateChallenge());

        BiometryCache cache = new BiometryCache();
        cache.request = request;
        cache.response = response;

        mRequestCache.put(response.getCorrelationId(), cache);
        return response;
    }

    @Transactional
    public DeleteRegistrationResponse deleteReg(BiometricRequest request) {
        DeleteRegistrationResponse response = new DeleteRegistrationResponse();
        response.setStatus("SUCCESS");
        try {
            Biometrics registration = getRegIfPresent(request);
            mBiometricsRepo.deleteById(registration.getId());
        } catch (EntityNotFoundException ignored) {
        }
        return response;
    }

    @Transactional
    public FinishAuthenticationResponse auth(BiometricRequest request) {
        Biometrics reg = getRegIfPresent(request);
        FinishMessage finishAuthMsg = new FinishMessage(
                request.getMessage(),
                request.getOperation()
        );

        String correlationId = finishAuthMsg.getCorrelationId();
        BiometryCache cache = mRequestCache.get(correlationId);
        if (cache == null) {
            throw new RequestValidationException("Nothing associated with the correlationId");
        }

        InitAuthenticationResponse initResponse;
        initResponse = (InitAuthenticationResponse) cache.response;


        String originalChallenge = initResponse.getChallenge();
        if (!originalChallenge.equals(finishAuthMsg.getChallenge())) {
            throw new RequestValidationException(
                    "Security constraint error"
            );
        }

        if (isChallengeSignatureInvalid(
                reg.getPubKeyBase64(),
                finishAuthMsg.getChallenge(),
                finishAuthMsg.getSignature())
        ) {
            throw new RequestValidationException(
                    "Challenge invalidly signed"
            );
        }

        FinishAuthenticationResponse finishAuth =
                new FinishAuthenticationResponse();

        finishAuth.setStatus("SUCCESS");
        request.setSubject(cache.request.getSubject());
        mRequestCache.remove(correlationId);

        if (finishAuthMsg.getTransactionId() != null) {
            mTransactionService.completeTransaction(
                    finishAuthMsg.getTransactionId()
            );
        }

        return finishAuth;
    }

    @Transactional
    public InitRegistrationResponse initRegistration(BiometricRequest request) {
        InitRegistrationResponse initReg = new InitRegistrationResponse();
        String correlationId = UUID.randomUUID().toString();
        initReg.setChallenge(CryptoUtils.generateChallenge());
        initReg.setCorrelationId(correlationId);

        BiometryCache cache = new BiometryCache();
        cache.request = request;
        cache.response = initReg;

        mRequestCache.put(correlationId, cache);
        return initReg;
    }

    @Transactional
    public FinishRegistrationResponse register(BiometricRequest request) {
        User userInfo = mUsersService.getUser(request.getSubject());

        FinishRegistrationResponse finishReg = new FinishRegistrationResponse();
        FinishMessage finishRegMsg = new FinishMessage(
                request.getMessage(),
                request.getOperation()
        );

        String correlationId = finishRegMsg.getCorrelationId();
        String challenge = finishRegMsg.getChallenge();
        String publicKeyString = finishRegMsg.getPubKeyPEM();
        String signature = finishRegMsg.getSignature();

        InitRegistrationResponse initRegResponse =
                (InitRegistrationResponse) mRequestCache.get(correlationId)
                        .response;

        if (initRegResponse == null) {
            throw new RequestValidationException(
                    "No response associated with the correlationId"
            );
        }

        String originalChallenge = initRegResponse.getChallenge();
        if (!originalChallenge.equals(challenge)) {
            throw new RequestValidationException(
                    "Security constraint error"
            );
        }

        if (isChallengeSignatureInvalid(
                publicKeyString,
                challenge,
                signature)
        ) {
            throw new RequestValidationException(
                    "Challenge invalidly signed"
            );
        }

        Device deviceInfo = request.getDeviceInfo();
        UserDevice device = mUsersService.updateDeviceAndGet(userInfo, deviceInfo);

        // Save the biometrics registration.
        Biometrics biometricsRegistration = new Biometrics();
        biometricsRegistration
                .setUser(userInfo)
                .setPubKeyBase64(publicKeyString)
                .setDevice(device)
                .setPubKeyBase64(publicKeyString);

        finishReg.setRegId(biometricsRegistration.getId());
        mBiometricsRepo.save(biometricsRegistration);

        // Remove the cached request.
        mRequestCache.remove(correlationId);

        finishReg.setStatus("SUCCESS");
        return finishReg;
    }

    private boolean isChallengeSignatureInvalid(
            String publicKeyPEM,
            String challenge,
            String signature
    ) {
        try { // Verify the signature.
            Signature sign = Signature.getInstance("SHA256withRSAandMGF1");
            sign.initVerify(CryptoUtils.rsaPemToPublicKey(publicKeyPEM));
            sign.update(challenge.getBytes(StandardCharsets.UTF_8));
            return !sign.verify(Base64.getDecoder().decode(signature.getBytes()));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RequestValidationException(
                    "Internal server error", e
            );
        } catch (InvalidKeyException e) {
            throw new RequestValidationException(
                    "Invalid key provided", e
            );
        } catch (SignatureException e) {
            throw new RequestValidationException(
                    "Challenge invalidly signed", e
            );
        }
    }

    private Biometrics getRegIfPresent(BiometricRequest request) {
        User userInfo = getUserInfo(request.getSubject());
        Device requestedDevice = request.getDeviceInfo();
        UserDevice device = mUsersService.updateDeviceAndGet(userInfo, requestedDevice);
        Biometrics reg = mBiometricsRepo.findByUserAndDevice(userInfo, device);
        if (reg == null) {
            throw new RequestValidationException(
                    "Requested user does not exist"
            );
        }
        return reg;
    }

    private User getUserInfo(String username) {
        User userInfo = mUsersService.getUser(username);
        if (userInfo == null) {
            throw new RequestValidationException(
                    "Requested user does not exist"
            );
        }
        return userInfo;
    }

    private static class BiometryCache {
        BiometryResponse response;
        BiometricRequest request;
    }

    private static final class FinishMessage {
        private static final String KEY_CORRELATION_ID = "correlationId";

        private static final String KEY_CHALLENGE = "challenge";

        private static final String KEY_SIGNATURE = "signature";

        private static final String KEY_PUBLIC_KEY = "pubKey";

        private static final String KEY_TRANSACTION_ID = "transactionId";

        private final String mCorrelationId;

        private final String mChallenge;

        private final String mSignature;

        private final String mPubKeyPEM;

        private final String mTransactionId;

        FinishMessage(String b64Message, BiometricOperation operation) {
            byte[] msgBytes = Base64.getDecoder().decode(b64Message);
            String msgDecoded = new String(msgBytes, StandardCharsets.UTF_8);
            JsonObject msg = new Gson().fromJson(msgDecoded, JsonObject.class);
            mCorrelationId = getCorrelationId(msg);
            mChallenge = getChallenge(msg);
            mSignature = getSignature(msg);
            mTransactionId = getTransactionId(msg);
            mPubKeyPEM = getPubKeyPEM(operation, msg);
        }

        private String getCorrelationId(JsonObject msg) {
            final String mCorrelationId;
            if (!msg.has(KEY_CORRELATION_ID) ||
                    !msg.get(KEY_CORRELATION_ID).isJsonPrimitive() ||
                    !msg.get(KEY_CORRELATION_ID).getAsJsonPrimitive().isString()) {
                throw new RequestValidationException(
                        "Missing correlation id"
                );
            }
            mCorrelationId = msg.get(KEY_CORRELATION_ID).getAsString();
            return mCorrelationId;
        }

        private String getChallenge(JsonObject msg) {
            final String mChallenge;
            if (!msg.has(KEY_CHALLENGE) ||
                    !msg.get(KEY_CHALLENGE).isJsonPrimitive() ||
                    !msg.get(KEY_CHALLENGE).getAsJsonPrimitive().isString()) {

                throw new RequestValidationException(
                        "Missing challenge"
                );
            }
            mChallenge = msg.get(KEY_CHALLENGE).getAsString();
            return mChallenge;
        }

        private String getSignature(JsonObject msg) {
            final String mSignature;
            if (!msg.has(KEY_SIGNATURE) ||
                    !msg.get(KEY_SIGNATURE).isJsonPrimitive() ||
                    !msg.get(KEY_SIGNATURE).getAsJsonPrimitive().isString()) {
                throw new RequestValidationException(
                        "Missing signature"
                );
            }
            mSignature = msg.get(KEY_SIGNATURE).getAsString();
            return mSignature;
        }

        private String getTransactionId(JsonObject msg) {
            final String mTransactionId;
            if (msg.has(KEY_TRANSACTION_ID)) {
                if (!msg.get(KEY_TRANSACTION_ID).isJsonPrimitive() ||
                        !msg.get(KEY_TRANSACTION_ID).getAsJsonPrimitive().isString()) {
                    throw new RequestValidationException(
                            "Invalid transactionId"
                    );
                }
                mTransactionId = msg.get(KEY_TRANSACTION_ID).getAsString();
            } else {
                mTransactionId = null;
            }
            return mTransactionId;
        }

        private String getPubKeyPEM(BiometricOperation operation, JsonObject msg) {
            final String mPubKeyPEM;
            if (operation == BiometricOperation.FINISH_REG) {
                if (!msg.has(KEY_SIGNATURE) ||
                        !msg.get(KEY_SIGNATURE).isJsonPrimitive() ||
                        !msg.get(KEY_SIGNATURE).getAsJsonPrimitive().isString()) {

                    throw new RequestValidationException(
                            "Public Key is invalid"
                    );
                }

                mPubKeyPEM = msg.get(KEY_PUBLIC_KEY).getAsString();
                if (!CryptoUtils.isRSAPublicKey(mPubKeyPEM)) {
                    throw new RequestValidationException(
                            "Public Key is not in PEM format or is not RSA"
                    );
                }
            } else {
                mPubKeyPEM = null;
            }
            return mPubKeyPEM;
        }

        public String getCorrelationId() {
            return mCorrelationId;
        }

        public String getChallenge() {
            return mChallenge;
        }

        public String getSignature() {
            return mSignature;
        }

        public String getPubKeyPEM() {
            return mPubKeyPEM;
        }

        public String getTransactionId() {
            return mTransactionId;
        }
    }
}
