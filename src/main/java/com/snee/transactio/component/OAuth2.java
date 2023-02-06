package com.snee.transactio.component;

import com.snee.transactio.db.repo.Repos;
import com.snee.transactio.model.request.oauth2.AuthCodeRequest;
import com.snee.transactio.model.request.oauth2.TokenRequest;
import com.snee.transactio.oauth2.OAuth2StdErrorResponse;
import com.snee.transactio.oauth2.adapter.OAuthAdapter;
import com.snee.transactio.oauth2.model.AccessToken;
import com.snee.transactio.oauth2.model.client.RegisteredClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OAuth2 {
	private final List<OAuthAdapter> mAdapters;
	private final Repos mRepos;

	public OAuth2(List<OAuthAdapter> adapters, Repos repos) {
		mRepos = repos;
		mAdapters = adapters;
		configure();
	}

	/**
	 * Initializes the adapters.
	 */
	private void configure() {
		for (OAuthAdapter adapter : mAdapters) {
			adapter.setRepos(mRepos);
		}
	}

	/**
	 * @param request The request to handle.
	 * @return Returns the generated auth_code.
	 */
	public String generateAuthCode(AuthCodeRequest request) {
		OAuthAdapter adapter = getAdapterWithClientCredentials(request.getClientId());
		return adapter.generateAuthCode(request);
	}

	/**
	 * Generates an {@link AccessToken} for the issued auth_code.
	 *
	 * @param request          The request to handle.
	 * @param clientCredential The client authorization credentials.
	 * @return An initialized {@link AccessToken} instance.
	 */
	public AccessToken generateAccessToken(TokenRequest request, String clientCredential) {
		OAuthAdapter adapter = getAdapterWithClientCredentials(clientCredential);
		return adapter.generateAccessToken(request, clientCredential);
	}

	/**
	 * Returns the adapter for the respective {@link RegisteredClient}.
	 *
	 * @param clientCredential The client identifier.
	 * @return An {@link OAuthAdapter} instance that can handle the requests for client.
	 */
	public OAuthAdapter getAdapterWithClientCredentials(String clientCredential) {
		for (OAuthAdapter adapter : mAdapters) {
			if (adapter.canHandle(clientCredential)) {
				return adapter;
			}
		}

		throw OAuth2StdErrorResponse.UNAUTHORIZED_CLIENT.getException();
	}
}
