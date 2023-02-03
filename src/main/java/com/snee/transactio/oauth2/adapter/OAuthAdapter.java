package com.snee.transactio.oauth2.adapter;

import com.snee.transactio.db.entities.user.User;
import com.snee.transactio.db.repo.Repos;
import com.snee.transactio.model.request.oauth2.AuthCodeRequest;
import com.snee.transactio.model.request.oauth2.TokenRequest;
import com.snee.transactio.oauth2.model.AccessToken;
import com.snee.transactio.oauth2.model.client.RegisteredClient;

import java.util.List;

public interface OAuthAdapter {
	String generateAuthCode(AuthCodeRequest request);

	AccessToken generateAccessToken(TokenRequest request, String clientAuthorization);

	void configureClients(List<RegisteredClient> clients);

	boolean canHandle(String clientId);

	boolean canHandle(RegisteredClient client);

	User getUser(String accessToken);

	void setRepos(Repos repos);

	RegisteredClient getRegisteredClient(String clientCredential);
}
