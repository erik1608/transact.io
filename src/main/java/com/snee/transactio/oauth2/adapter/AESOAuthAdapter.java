package com.snee.transactio.oauth2.adapter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.snee.transactio.config.OAuth2Config;
import com.snee.transactio.crypto.AesGcmCipher;
import com.snee.transactio.db.entities.user.User;
import com.snee.transactio.db.repo.UsersRepo;
import com.snee.transactio.exceptions.RequestValidationException;
import com.snee.transactio.model.request.oauth2.AuthCodeRequest;
import com.snee.transactio.model.request.oauth2.TokenRequest;
import com.snee.transactio.oauth2.OAuth2StdErrorResponse;
import com.snee.transactio.oauth2.OAuthAdapterAlgorithm;
import com.snee.transactio.oauth2.model.AccessToken;
import com.snee.transactio.oauth2.model.client.RegisteredClient;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class AESOAuthAdapter extends BaseOAuthAdapter {

	private static final OAuthAdapterAlgorithm ALG = OAuthAdapterAlgorithm.AES_ENC;

	private static final String KEY_USERNAME = "username";
	private static final String KEY_AUTH_CODE_ID = "id";
	private static final String KEY_CLIENT_ID = "client_id";
	private static final String OAUTH2_SCOPE = "oauth2";

	// Maps the authorization code id  with granted authorization code
	private final Map<String, String> mAuthCodeMap = new HashMap<>();

	public AESOAuthAdapter(OAuth2Config oauth2Configurations) {
		super(oauth2Configurations);
	}

	@Override
	public boolean canHandle(String clientId) {
		RegisteredClient client = getRegisteredClient(clientId);
		return canHandle(client);
	}

	@Override
	public boolean canHandle(RegisteredClient client) {
		return client.getSecretKeyAlg().equals(ALG.getName()) && client.getSecretKeyUse().equals(ALG.getUse());
	}

	@Override
	public String generateAuthCode(AuthCodeRequest request) {
		RegisteredClient client = getRegisteredClient(request.getClientId());
		if (!client.getRedirectUris().contains(request.getRedirectUri())) {
			throw OAuth2StdErrorResponse.UNAUTHORIZED_CLIENT.getException();
		}

		if (!OAUTH2_SCOPE.equals(request.getScope())) {
			throw OAuth2StdErrorResponse.INVALID_SCOPE.getException();
		}

		User user = getUserInfo(request.getSessionData().getSubject());
		JsonObject authCodeObject = new JsonObject();
		String authCodeId = UUID.randomUUID().toString();
		authCodeObject.addProperty(KEY_AUTH_CODE_ID, authCodeId);
		authCodeObject.addProperty(KEY_USERNAME, user.getUsername());
		authCodeObject.addProperty(KEY_CLIENT_ID, user.getUsername());

		String authCode = encryptData(authCodeObject.toString(), client);
		// Save the responded auth code, to prevent reply attacks.
		mAuthCodeMap.put(authCodeId, authCode);
		return authCode;
	}

	@Override
	public AccessToken generateAccessToken(TokenRequest request, String clientAuthorization) {
		RegisteredClient client = getRegisteredClient(clientAuthorization);
		JsonObject authCodeDecrypted = new Gson().fromJson(decryptData(request.getCode(), client), JsonObject.class);
		String codeId = authCodeDecrypted.get(KEY_AUTH_CODE_ID).getAsString();
		if (!mAuthCodeMap.containsKey(codeId)) {
			throw new RequestValidationException("Reply attack detected.");
		}

		// Remove the cached auth code.
		mAuthCodeMap.remove(codeId);

		User user = getUserInfo(authCodeDecrypted.get(KEY_USERNAME).getAsString());

		JsonObject accessTokenJson = new JsonObject();
		accessTokenJson.addProperty(KEY_USERNAME, user.getUsername());

		String encData = encryptData(accessTokenJson.toString(), client);
		AccessToken accessToken = new AccessToken(client, encData);
		accessToken.setTokenType(client.getToken().getType());
		return accessToken;
	}

	@Override
	public User getUser(String accessToken) {
		AccessToken token = new AccessToken(accessToken, this);
		JsonObject data = new Gson().fromJson(decryptData(token.getData(), token.getClient()), JsonObject.class);
		if (!data.has(KEY_USERNAME)) {
			throw new SecurityException("The user is unknown");
		}

		return getUserInfo(data.get(KEY_USERNAME).getAsString());
	}

	private User getUserInfo(String username) {
		UsersRepo usersRepo = mRepos.get(UsersRepo.class);
		return usersRepo.findByUsername(username)
				.orElseThrow(OAuth2StdErrorResponse.INVALID_REQUEST::getException);
	}

	protected String encryptData(String data, RegisteredClient client) {
		try {
			byte[] secretKeyBytes = Base64.getDecoder().decode(client.getSecretKey());
			AesGcmCipher cipher = new AesGcmCipher(new SecretKeySpec(secretKeyBytes, ALG.getName()));
			return cipher.encrypt(data, false);
		} catch (GeneralSecurityException e) {
			String message = "Encryption failed.";
			LOG.error(message, e);
			throw new RequestValidationException(message);
		}
	}

	protected String decryptData(String data, RegisteredClient client) {
		try {
			byte[] secretKeyBytes = Base64.getDecoder().decode(client.getSecretKey());
			AesGcmCipher cipher = new AesGcmCipher(new SecretKeySpec(secretKeyBytes, ALG.getName()));
			return cipher.decrypt(data, false);
		} catch (GeneralSecurityException e) {
			String message = "Decryption failed.";
			LOG.error(message, e);
			throw new RequestValidationException(message);
		}
	}
}
