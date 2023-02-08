package com.snee.transactio.config;

import com.snee.transactio.alexa.AlexaSkillServlet;
import com.snee.transactio.alexa.handler.BaseRequestHandler;
import com.snee.transactio.component.OAuth2;
import com.snee.transactio.oauth2.adapter.OAuthAdapter;
import org.springframework.beans.factory.annotation.Value;
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
public class AlexaServletConfig {

	@Bean
	public ServletRegistrationBean<HttpServlet> alexaServlet(
			ApplicationContext applicationContext,
			AlexaConfigs alexaConfigs,
			OAuth2 oAuth2,
			@Value("${api.prefix}") String apiPrefix) {

		// Get the client adapter from the facade and distribute them in the handlers.
		OAuthAdapter adapter = oAuth2.getAdapterWithClientCredentials(
				alexaConfigs.getOauthClientId()
		);

		// Initialize the handlers.
		List<BaseRequestHandler> handlers = new ArrayList<>();
		try {
			for (String handlerName : alexaConfigs.getHandlers()) {
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
				new AlexaSkillServlet(alexaConfigs.getSkillId(), handlers)
		);
		alexa.setLoadOnStartup(1);
		return alexa;
	}
}
