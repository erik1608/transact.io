package com.snee.transactio.config;

import com.snee.transactio.alexa.AlexaSkillServlet;
import com.snee.transactio.alexa.handler.BaseRequestHandler;
import com.snee.transactio.component.OAuth2;
import com.snee.transactio.oauth2.adapter.OAuthAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServlet;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "alexa")
public class AlexaConfigs {
	private String defaultLocale;
	private String skillId;
	private String oauthClientId;
	private List<String> handlers;

	/**
	 * Getter for default locale of the Alexa skill.
	 *
	 * @return {@link String} representation of default locale.
	 */
	public String getDefaultLocale() {
		return defaultLocale;
	}

	/**
	 * Sets the {@link String} representation of default locale
	 */
	public void setDefaultLocale(String defaultLocale) {
		this.defaultLocale = defaultLocale;
	}

	/**
	 * Getter for skill identifier of the Alexa skill.
	 *
	 * @return the Alexa skill identifier to be handled.
	 */
	public String getSkillId() {
		return skillId;
	}

	/**
	 * Setter for the Alexa skill identifier to be handled.
	 *
	 * @param skillId the skill identifier to be set.
	 */
	public void setSkillId(String skillId) {
		this.skillId = skillId;
	}

	/**
	 * Getter for Alexa OAuth client identifier.
	 *
	 * @return the OAuth identifier of Alexa client.
	 */
	public String getOAuthClientId() {
		return oauthClientId;
	}

	/**
	 * Setter for Alexa OAuth client identifier.
	 */
	public void setOAuthClientId(String oauthClientId) {
		this.oauthClientId = oauthClientId;
	}

	/**
	 * Getter for the class name list of the Alexa request handlers.
	 *
	 * @return {@link List<String>} the class name list of the Alexa request handlers
	 */
	public List<String> getHandlers() {
		return handlers;
	}

	public void setHandlers(List<String> handlers) {
		this.handlers = handlers;
	}

	/**
	 * Initializes the Alexa request handlers and
	 * maps an endpoint to servlet that handles the Alexa requests.
	 *
	 * @param applicationContext The spring application context.
	 * @param oAuth2             The facade of OAuth2 subsystem.
	 * @param apiPrefix          The base path to register the endpoints with.
	 * @return A {@link ServletRegistrationBean<HttpServlet>} with {@link AlexaSkillServlet} servlet.
	 */
	@Bean
	public ServletRegistrationBean<HttpServlet> alexaServlet(
			ApplicationContext applicationContext,
			OAuth2 oAuth2,
			@Value("${api.prefix}") String apiPrefix) {

		// Get the client adapter from the facade and distribute them in the handlers.
		OAuthAdapter adapter = oAuth2.getAdapterWithClientCredentials(
				getOAuthClientId()
		);

		// Initialize the handlers.
		List<BaseRequestHandler> handlers = new ArrayList<>();
		try {
			for (String handlerName : getHandlers()) {
				Class<?> clazz = Class.forName(handlerName);
				Constructor<?> ctor = clazz.getConstructor(
						OAuthAdapter.class,
						ApplicationContext.class
				);

				handlers.add(
						(BaseRequestHandler) ctor.newInstance(adapter, applicationContext)
				);
			}
		} catch (ClassNotFoundException |
		         ClassCastException |
		         NoSuchMethodException |
		         InstantiationException |
		         IllegalAccessException |
		         InvocationTargetException ignored) {
		}

		// Construct the servlet registration bean.
		ServletRegistrationBean<HttpServlet> alexa = new ServletRegistrationBean<>();
		alexa.addUrlMappings(apiPrefix + "/alexa/*");
		alexa.setServlet(
				new AlexaSkillServlet(getSkillId(), handlers)
		);
		alexa.setLoadOnStartup(1);
		return alexa;
	}
}
