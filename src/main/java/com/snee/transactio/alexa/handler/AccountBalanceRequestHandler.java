package com.snee.transactio.alexa.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.amazon.ask.request.Predicates;
import com.snee.transactio.db.entities.user.User;
import com.snee.transactio.db.entities.user.UserAccount;
import com.snee.transactio.oauth2.adapter.OAuthAdapter;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

/**
 * Handles the account balance requests. <br>
 * <p>
 * Intent - <b>AccountBalanceIntent</b> <br>
 * Slot names - [{@link BaseRequestHandler#ACCOUNT_TYPE_SLOT}] <br>
 * <p>
 * Utterance samples:
 * <ul>
 *  <li>Alexa, ask <b>transact.io</b> for my {{@link BaseRequestHandler#ACCOUNT_TYPE_SLOT}} balance.</li>
 * </ul>
 * <p>
 * Slot values: <br>
 * {{@link BaseRequestHandler#ACCOUNT_TYPE_SLOT}} - ["checking account"]
 */
@SuppressWarnings("unused")
public class AccountBalanceRequestHandler extends BaseRequestHandler {

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
    public AccountBalanceRequestHandler(
            OAuthAdapter clientAdapter,
            ApplicationContext applicationContext
    ) {
        super(clientAdapter, applicationContext);
    }

    @Override
    public boolean canHandle(HandlerInput handlerInput) {
        return handlerInput.matches(Predicates.intentName("AccountBalanceIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput) {
        super.handle(handlerInput);
        User user = getUser(handlerInput);
        if (user == null) {
            return handlerInput.getResponseBuilder()
                    .withSpeech("Please link your account with me")
                    .withLinkAccountCard()
                    .build();
        }
        Slot accountTypeSlot = slots.get(ACCOUNT_TYPE_SLOT);
        String accountType = accountTypeSlot.getValue();
        StringBuilder responseSpeechBuilder = new StringBuilder("Dear ")
                .append(user.getFirstname())
                .append(" ")
                .append(user.getLastname());
        UserAccount foundAccount = user.getAccountByName(accountType);
        if (foundAccount == null) {
            responseSpeechBuilder.append(", the requested ")
                    .append(accountType)
                    .append(" is unknown to your profile.");
        } else {
            responseSpeechBuilder.append(", you have ")
                    .append(foundAccount.getBalance())
                    .append(" credits on your ").append(accountType).append(".");
        }

        return handlerInput.getResponseBuilder()
                .withSpeech(responseSpeechBuilder.toString())
                .withSimpleCard(SKILL_TITLE, responseSpeechBuilder.toString())
                .build();
    }
}
