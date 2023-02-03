package com.snee.transactio.alexa.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;
import com.snee.transactio.db.entities.user.User;
import com.snee.transactio.oauth2.adapter.OAuthAdapter;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

public class HelloRequestHandler extends BaseRequestHandler {
	public HelloRequestHandler(
			OAuthAdapter clientAdapter,
			ApplicationContext applicationContext
	) {
		super(clientAdapter, applicationContext);
	}

	@Override
	public boolean canHandle(HandlerInput handlerInput) {
		return handlerInput.matches(Predicates.intentName("SayHelloIntent"));
	}

	@Override
	public Optional<Response> handle(HandlerInput handlerInput) {
		User user = getUser(handlerInput);
		if (user == null) {
			return handlerInput.getResponseBuilder().withSpeech("Please link your account with me")
					.withLinkAccountCard()
					.build();
		}
		StringBuilder speech = new StringBuilder("Hello ");
		speech.append(user.getFirstname()).append(" ").append(user.getLastname());
		return handlerInput.getResponseBuilder().withSpeech(speech.toString())
				.withSimpleCard(CARD_TITLE, speech.toString())
				.build();
	}
}
