package com.snee.transactio.oauth2.model;

import com.snee.transactio.exceptions.OAuth2Exception;
import com.snee.transactio.oauth2.OAuth2StdErrorResponse;
import com.snee.transactio.oauth2.adapter.OAuthAdapter;
import com.snee.transactio.oauth2.model.client.RegisteredClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AccessToken {
	private String tokenContentDelim = ":";

	private String tokenType;
	private String initializedToken;

	private RegisteredClient client;
	private String data;

	private OAuthAdapter adapter;

	/**
	 * Initialize an instance with an already generated token. <br>
	 * This ctor will deserialize the token into the 3 parts.
	 * <ul>
	 *     <li>tokenPrefix - set by the {@link RegisteredClient} configuration (optional).</li>
	 *     <li>clientId - the {@link RegisteredClient} identifier.</li>
	 *     <li>data - an encrypted data used to very the {@link RegisteredClient} using its server secret.</li>
	 * </ul>
	 *
	 * @param initializedToken a String representation of the access token.
	 */
	public AccessToken(String initializedToken, OAuthAdapter adapter) {
		this.initializedToken = initializedToken;
		this.adapter = adapter;
		deserialize();
	}

	/**
	 * Initialize an instance with required properties to generated token.
	 * The {@link #getToken()} will be initialized and can be used once the instance is created.
	 *
	 * @param client        The {@link RegisteredClient} instance.
	 * @param encryptedData The data that has been encrypted with the client's server key.
	 * @throws OAuth2Exception in case the token cannot be deserialized.
	 */
	public AccessToken(RegisteredClient client, String encryptedData) {
		this.client = client;
		this.data = encryptedData;
		serialize();
	}

	private void serialize() {
		StringBuilder buffer = new StringBuilder();
		String prefix = client.getToken().getPrefix();
		if (prefix != null && !prefix.isEmpty()) {
			buffer.append(prefix).append(tokenContentDelim);
		}
		buffer.append(client.getId()).append(tokenContentDelim);
		buffer.append(data);
		initializedToken = Base64.getEncoder().encodeToString(buffer.toString().getBytes(StandardCharsets.UTF_8));
	}

	private void deserialize() {
		if (initializedToken == null || initializedToken.isEmpty()) {
			throw OAuth2StdErrorResponse.INVALID_REQUEST.getException();
		}

		String[] accessTokenComponents = new String(Base64.getDecoder().decode(initializedToken), StandardCharsets.UTF_8).split(":");
		if (accessTokenComponents.length < 3) {
			throw OAuth2StdErrorResponse.ACCESS_DENIED.getException();
		}

		String tokenPfx = accessTokenComponents[0];
		client = adapter.getRegisteredClient(accessTokenComponents[1]);
		data = accessTokenComponents[2];

		if (client == null) {
			throw OAuth2StdErrorResponse.ACCESS_DENIED.getException();
		}

		if (!client.getToken().getPrefix().equals(tokenPfx)) {
			throw OAuth2StdErrorResponse.ACCESS_DENIED.getException();
		}
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public String getToken() {
		return initializedToken;
	}

	public String getData() {
		return data;
	}

	public void setToken(String token) {
		this.initializedToken = token;
	}

	public String getTokenContentDelim() {
		return tokenContentDelim;
	}

	/**
	 * Sets the token delimiter used while token generation.
	 *
	 * @param tokenContentDelim The delimiter for the decoded token.
	 */
	public void setTokenContentDelim(String tokenContentDelim) {
		this.tokenContentDelim = tokenContentDelim;
		serialize();
	}

	public RegisteredClient getClient() {
		return client;
	}
}
