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
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

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

	public BiometryService(UserHandlerService userHandlerService, Repos repos, TransactionService transactionService) {
		// Add the Security provider.
		Security.addProvider(new BouncyCastleProvider());
		mUsersService = userHandlerService;
		mTransactionService = transactionService;
		mBiometricsRepo = repos.get(BiometricsRepo.class);
	}

	@Transactional
	public InitAuthenticationResponse initAuthorization(BiometricRequest request) {
		getBiometricRegistrationIfPresent(request);
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
	public DeleteRegistrationResponse deleteRegistration(BiometricRequest request) {
		DeleteRegistrationResponse response = new DeleteRegistrationResponse();
		response.setStatus("SUCCESS");
		try {
			Biometrics registration = getBiometricRegistrationIfPresent(request);
			mBiometricsRepo.deleteById(registration.getId());
		} catch (EntityNotFoundException ignored) {
		}
		return response;
	}

	@Transactional
	public FinishAuthenticationResponse authorize(BiometricRequest request) {
		Biometrics biometricsRegistration = getBiometricRegistrationIfPresent(request);
		FinishMessage finishMessage = new FinishMessage(request.getMessage(), request.getOperation());
		String correlationId = finishMessage.getCorrelationId();
		BiometryCache cache = mRequestCache.get(correlationId);
		if (cache == null) {
			throw new RequestValidationException("Nothing associated with the correlationId");
		}

		InitAuthenticationResponse initResponse;
		initResponse = (InitAuthenticationResponse) cache.response;


		String originalChallenge = initResponse.getChallenge();
		if (!originalChallenge.equals(finishMessage.getChallenge())) {
			throw new RequestValidationException("Security constraint error");
		}

		if (isChallengeSignatureInvalid(biometricsRegistration.getPubKeyBase64(), finishMessage.getChallenge(), finishMessage.getSignature())) {
			throw new RequestValidationException("Challenge invalidly signed");
		}

		FinishAuthenticationResponse finishAuthResponse = new FinishAuthenticationResponse();
		finishAuthResponse.setStatus("SUCCESS");
		request.setSubject(cache.request.getSubject());
		mRequestCache.remove(correlationId);

		if (finishMessage.getTransactionId() != null) {
			mTransactionService.completeTransaction(finishMessage.getTransactionId());
		}

		// Create session data for requested subject.
		return finishAuthResponse;
	}

	@Transactional
	public InitRegistrationResponse initRegistration(BiometricRequest request) {
		InitRegistrationResponse initResponse = new InitRegistrationResponse();
		String correlationId = UUID.randomUUID().toString();
		initResponse.setChallenge(CryptoUtils.generateChallenge());
		initResponse.setCorrelationId(correlationId);

		BiometryCache cache = new BiometryCache();
		cache.request = request;
		cache.response = initResponse;

		mRequestCache.put(correlationId, cache);
		return initResponse;
	}

	@Transactional
	public FinishRegistrationResponse register(BiometricRequest request) {
		User userInfo = mUsersService.getUser(request.getSubject());

		FinishRegistrationResponse finishRegResponse = new FinishRegistrationResponse();
		FinishMessage finishMessage = new FinishMessage(request.getMessage(), request.getOperation());

		String correlationId = finishMessage.getCorrelationId();
		String challenge = finishMessage.getChallenge();
		String publicKeyString = finishMessage.getPubKeyPEM();
		String signature = finishMessage.getSignature();

		InitRegistrationResponse initRegResponse;
		initRegResponse = (InitRegistrationResponse) mRequestCache.get(correlationId).response;
		if (initRegResponse == null) {
			throw new RequestValidationException("No response associated with the correlationId");
		}

		String originalChallenge = initRegResponse.getChallenge();
		if (!originalChallenge.equals(challenge)) {
			throw new RequestValidationException("Security constraint error");
		}

		if (isChallengeSignatureInvalid(publicKeyString, challenge, signature)) {
			throw new RequestValidationException("Challenge invalidly signed");
		}

		Device deviceInfo = request.getDeviceInfo();
		UserDevice device = mUsersService.updateUserDeviceIfNeededAndGet(userInfo, deviceInfo);

		// Save the biometrics registration.
		Biometrics biometricsRegistration = new Biometrics();
		biometricsRegistration.setUser(userInfo).setPubKeyBase64(publicKeyString).setDevice(device).setPubKeyBase64(publicKeyString);

		finishRegResponse.setRegId(biometricsRegistration.getId());
		mBiometricsRepo.save(biometricsRegistration);

		// Remove the cached request.
		mRequestCache.remove(correlationId);

		finishRegResponse.setStatus("SUCCESS");
		return finishRegResponse;
	}

	private boolean isChallengeSignatureInvalid(String publicKeyPEM, String challenge, String signature) {
		try { // Verify the signature.
			Signature sign = Signature.getInstance("SHA256withRSAandMGF1");
			sign.initVerify(CryptoUtils.rsaPemToPublicKey(publicKeyPEM));
			sign.update(challenge.getBytes(StandardCharsets.UTF_8));
			return !sign.verify(Base64.getDecoder().decode(signature.getBytes()));
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new RequestValidationException("Internal server error", e);
		} catch (InvalidKeyException e) {
			throw new RequestValidationException("Invalid key provided", e);
		} catch (SignatureException e) {
			throw new RequestValidationException("Challenge invalidly signed", e);
		}
	}

	private Biometrics getBiometricRegistrationIfPresent(BiometricRequest request) {
		User userInfo = getUserInfo(request.getSubject());
		Device requestedDevice = request.getDeviceInfo();
		UserDevice device = mUsersService.updateUserDeviceIfNeededAndGet(userInfo, requestedDevice);
		Biometrics biometricsRegistration = mBiometricsRepo.findByUserAndDevice(userInfo, device);
		if (biometricsRegistration == null) {
			throw new RequestValidationException("Requested user does not exist");
		}
		return biometricsRegistration;
	}

	private User getUserInfo(String username) {
		User userInfo = mUsersService.getUser(username);
		if (userInfo == null) {
			throw new RequestValidationException("Requested user does not exist");
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
			JsonObject messageDecoded = new Gson().fromJson(new String(Base64.getDecoder().decode(b64Message), StandardCharsets.UTF_8), JsonObject.class);
			if (!messageDecoded.has(KEY_CORRELATION_ID) || !messageDecoded.get(KEY_CORRELATION_ID).isJsonPrimitive() || !messageDecoded.get(KEY_CORRELATION_ID).getAsJsonPrimitive().isString()) {
				throw new RequestValidationException("Missing correlation id");
			}
			mCorrelationId = messageDecoded.get(KEY_CORRELATION_ID).getAsString();

			if (!messageDecoded.has(KEY_CHALLENGE) || !messageDecoded.get(KEY_CHALLENGE).isJsonPrimitive() || !messageDecoded.get(KEY_CHALLENGE).getAsJsonPrimitive().isString()) {
				throw new RequestValidationException("Missing challenge");
			}
			mChallenge = messageDecoded.get(KEY_CHALLENGE).getAsString();

			if (!messageDecoded.has(KEY_SIGNATURE) || !messageDecoded.get(KEY_SIGNATURE).isJsonPrimitive() || !messageDecoded.get(KEY_SIGNATURE).getAsJsonPrimitive().isString()) {
				throw new RequestValidationException("Missing signature");
			}
			mSignature = messageDecoded.get(KEY_SIGNATURE).getAsString();

			if (messageDecoded.has(KEY_TRANSACTION_ID)) {
				if (!messageDecoded.get(KEY_TRANSACTION_ID).isJsonPrimitive() || !messageDecoded.get(KEY_TRANSACTION_ID).getAsJsonPrimitive().isString()) {
					throw new RequestValidationException("Invalid transactionId");
				}
				mTransactionId = messageDecoded.get(KEY_TRANSACTION_ID).getAsString();
			} else {
				mTransactionId = null;
			}

			if (operation == BiometricOperation.FINISH_REG) {
				if (!messageDecoded.has(KEY_SIGNATURE) || !messageDecoded.get(KEY_SIGNATURE).isJsonPrimitive() || !messageDecoded.get(KEY_SIGNATURE).getAsJsonPrimitive().isString()) {
					throw new RequestValidationException("Public Key is invalid");
				}

				mPubKeyPEM = messageDecoded.get(KEY_PUBLIC_KEY).getAsString();
				if (!CryptoUtils.isRSAPublicKey(mPubKeyPEM)) {
					throw new RequestValidationException("Public Key is not in PEM format or is not RSA");
				}
			} else {
				mPubKeyPEM = null;
			}
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
