package com.snee.transactio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "alexa")
public class AlexaConfigs {
	private String defaultLocale;
	private String skillId;
	private String oauthClientId;

	public String getDefaultLocale() {
		return defaultLocale;
	}

	public void setDefaultLocale(String defaultLocale) {
		this.defaultLocale = defaultLocale;
	}

	public String getSkillId() {
		return skillId;
	}

	public void setSkillId(String skillId) {
		this.skillId = skillId;
	}

	public String getOauthClientId() {
		return oauthClientId;
	}

	public void setOauthClientId(String oauthClientId) {
		this.oauthClientId = oauthClientId;
	}
}
