package com.snee.transactio.alexa.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;
import com.snee.transactio.oauth2.adapter.OAuthAdapter;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

/**
 * Handles the skill launch request. <br>
 * <p>
 * Intent - <b>AccountBalanceIntent</b> <br>
 * Slot names - N/A <br>
 * <p>
 * Utterance samples:
 * <ul>
 *  <li>Alexa, open <b>transact.io</b></li>
 * </ul>
 * <p>
 * Slot values: <br>
 * N/A
 */
public class AlexaLaunchRequestHandler extends BaseRequestHandler {

    private static final String LAUNCH_RESPONSE =
            "Welcome to " + SKILL_TITLE + "." +
            " You can ask for your account balance information," +
            " or to send money to any configured recipient." +
            " What action would you like to perform?";

    public AlexaLaunchRequestHandler(
            OAuthAdapter clientAdapter,
            ApplicationContext applicationContext
    ) {
        super(clientAdapter, applicationContext);
    }

    @Override
    public boolean canHandle(HandlerInput handlerInput) {
        return handlerInput.matches(
                Predicates.requestType(LaunchRequest.class)
        );
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput) {
        return handlerInput.getResponseBuilder()
                .withReprompt(LAUNCH_RESPONSE)
                .withSpeech(LAUNCH_RESPONSE)
                .withSimpleCard(SKILL_TITLE, LAUNCH_RESPONSE)
                .build();
    }
}
