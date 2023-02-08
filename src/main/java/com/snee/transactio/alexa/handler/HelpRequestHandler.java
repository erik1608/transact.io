package com.snee.transactio.alexa.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;
import com.snee.transactio.oauth2.adapter.OAuthAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

// TODO: 2/8/2023 Implement.
public class HelpRequestHandler extends BaseRequestHandler {
	private static Logger LOG = LogManager.getLogger(HelpRequestHandler.class);

	public HelpRequestHandler(
			OAuthAdapter clientAdapter,
			ApplicationContext applicationContext
	) {
		super(clientAdapter, applicationContext);
	}

	@Override
	public boolean canHandle(HandlerInput handlerInput) {
		return handlerInput.matches(Predicates.intentName("AMAZON.CancelIntent"));
	}

	@Override
	public Optional<Response> handle(HandlerInput handlerInput) {
		return Optional.empty();
	}
}
