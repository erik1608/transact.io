package com.snee.transactio.alexa.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;
import com.snee.transactio.oauth2.adapter.OAuthAdapter;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

public class CancelRequestHandler extends BaseRequestHandler {

	public CancelRequestHandler(OAuthAdapter clientAdapter, ApplicationContext applicationContext) {
		super(clientAdapter, applicationContext);
	}

	@Override
	public boolean canHandle(HandlerInput handlerInput) {
		return handlerInput.matches(Predicates.intentName("AMAZON.CancelIntent"));
	}

	@Override
	public Optional<Response> handle(HandlerInput handlerInput) {
		return handlerInput.getResponseBuilder().withSpeech("Good bye!").build();
	}
}
