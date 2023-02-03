package com.snee.transactio.config;

import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.snee.transactio.alexa.AlexaSkillServlet;
import com.snee.transactio.alexa.handler.AccountBalanceRequestHandler;
import com.snee.transactio.alexa.handler.AlexaLaunchRequestHandler;
import com.snee.transactio.alexa.handler.CancelRequestHandler;
import com.snee.transactio.alexa.handler.HelloRequestHandler;
import com.snee.transactio.alexa.handler.HelpRequestHandler;
import com.snee.transactio.alexa.handler.StopRequestHandler;
import com.snee.transactio.alexa.handler.TransactionRequestHandler;
import com.snee.transactio.component.OAuth2;
import com.snee.transactio.oauth2.adapter.OAuthAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServlet;

@Configuration
public class AlexaServletConfig {

	@Bean
	public ServletRegistrationBean<HttpServlet> alexaServlet(
			ApplicationContext applicationContext,
			AlexaConfigs alexaConfigs,
			OAuth2 oAuth2,
			@Value("${api.prefix}") String apiPrefix) {

		OAuthAdapter adapter = oAuth2.getAdapterWithClientCredentials(alexaConfigs.getOauthClientId());
		RequestHandler[] handlers = {
				new StopRequestHandler(adapter, applicationContext),
				new HelpRequestHandler(adapter, applicationContext),
				new AlexaLaunchRequestHandler(adapter, applicationContext),
				new HelloRequestHandler(adapter, applicationContext),
				new TransactionRequestHandler(adapter, applicationContext),
				new CancelRequestHandler(adapter, applicationContext),
				new AccountBalanceRequestHandler(adapter, applicationContext)
		};

		// Construct the servlet registration bean.
		ServletRegistrationBean<HttpServlet> alexaServletRegistrationBean = new ServletRegistrationBean<>();
		alexaServletRegistrationBean.addUrlMappings(apiPrefix + "/alexa/*");
		alexaServletRegistrationBean.setServlet(new AlexaSkillServlet(alexaConfigs.getSkillId(), handlers));
		alexaServletRegistrationBean.setLoadOnStartup(1);
		return alexaServletRegistrationBean;
	}
}
