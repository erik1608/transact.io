package com.snee.transactio.oauth2.adapter;

import com.snee.transactio.config.OAuth2Config;
import com.snee.transactio.db.repo.Repos;
import com.snee.transactio.oauth2.OAuth2StdErrorResponse;
import com.snee.transactio.oauth2.model.client.RegisteredClient;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseOAuthAdapter implements OAuthAdapter {
    protected final Logger LOG = LogManager.getLogger(this.getClass());

    protected final List<RegisteredClient> mClients = new ArrayList<>();

    protected Repos mRepos;

    public BaseOAuthAdapter(OAuth2Config oauth2Config) {
        configureClients(oauth2Config.getClients());
    }

    @Override
    public void configureClients(List<RegisteredClient> clients) {
        for (RegisteredClient registeredClient : clients) {
            // Call can handle to add only the clients that
            if (canHandle(registeredClient)) {
                mClients.add(registeredClient);
            }
        }
    }

    @Override
    public void setRepos(Repos repos) {
        mRepos = repos;
    }

    /**
     * Finds and returns the registered client object with its respective adapter.
     *
     * @param clientCred a token identifying the client, must be the client id or <clientId>:<clientSecret>.
     * @return the instance of {@link RegisteredClient}.
     * @throws IllegalArgumentException If the clientCred param is null or empty.
     */
    public RegisteredClient getRegisteredClient(String clientCred) {
        if (clientCred == null || clientCred.isEmpty()) {
            throw new IllegalArgumentException("The client credential must not be null or empty");
        }

        String clientId;
        String clientSecret = null;
        if (clientCred.contains(":")) {
            String[] clientCredsCombined = clientCred.split(":");
            clientId = clientCredsCombined[0];
            clientSecret = clientCredsCombined[1];
        } else {
            clientId = clientCred;
        }

        if (clientId == null) {
            throw OAuth2StdErrorResponse.UNAUTHORIZED_CLIENT.getException();
        }

        // Try to find the registered client with the provided clientId
        RegisteredClient client = null;
        for (RegisteredClient registeredClient : mClients) {
            if (registeredClient.getId().equals(clientId)) {
                client = registeredClient;
                break;
            }
        }

        // If the client was not found throw an UNAUTHORIZED exception.
        if (client == null) {
            throw OAuth2StdErrorResponse.UNAUTHORIZED_CLIENT.getException();
        }

        // If the found client secret did not match throw an UNAUTHORIZED exception.
        if (clientSecret != null && !client.getSecret().equals(clientSecret)) {
            throw OAuth2StdErrorResponse.UNAUTHORIZED_CLIENT.getException();
        }

        return client;
    }
}
