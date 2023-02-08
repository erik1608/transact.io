package com.snee.transactio.config;

import com.snee.transactio.oauth2.model.client.RegisteredClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "oauth2")
public class OAuth2Config {
    private List<RegisteredClient> clients;

    public List<RegisteredClient> getClients() {
        return clients;
    }

    public void setClients(List<RegisteredClient> clients) {
        this.clients = clients;
    }
}
