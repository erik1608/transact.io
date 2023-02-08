package com.snee.transactio.alexa.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.snee.transactio.oauth2.adapter.OAuthAdapter;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

// TODO: 2/8/2023 Implement.
public class StopRequestHandler extends BaseRequestHandler {

    public StopRequestHandler(
            OAuthAdapter clientAdapter,
            ApplicationContext applicationContext
    ) {
        super(clientAdapter, applicationContext);
    }

    @Override
    public boolean canHandle(HandlerInput handlerInput) {
        return false;
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput) {
        return Optional.empty();
    }
}
