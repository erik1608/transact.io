package com.snee.transactio.alexa.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;
import com.snee.transactio.oauth2.adapter.OAuthAdapter;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

@SuppressWarnings("unused")
public class CancelRequestHandler extends BaseRequestHandler {

    /**
     * A constructor that creates a new Object of the request handler,
     * with {@link OAuthAdapter} and {@link ApplicationContext}.
     *
     * @param clientAdapter      the client adapter used
     *                           for verification of provided access token,
     *                           if the handler give access to protected resource,
     *                           or performs protected operation.
     * @param applicationContext The Spring app context.
     *                           If the handler needs additional functionality
     *                           from the app.
     */
    public CancelRequestHandler(
            OAuthAdapter clientAdapter,
            ApplicationContext applicationContext
    ) {
        super(clientAdapter, applicationContext);
    }

    @Override
    public boolean canHandle(HandlerInput handlerInput) {
        return handlerInput.matches(
                Predicates.intentName("AMAZON.CancelIntent")
        );
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput) {
        return handlerInput.getResponseBuilder()
                .withSpeech("Good bye!")
                .build();
    }
}
