package com.snee.transactio.alexa.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.snee.transactio.db.entities.user.User;
import com.snee.transactio.oauth2.adapter.OAuthAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.Optional;

/**
 * Base class for the Skill request handlers.
 * Common methods for all request handlers must be implemented here,
 * and should have protected access.
 */
public abstract class BaseRequestHandler implements RequestHandler {
    protected static final String SKILL_TITLE = "Transact.IO";

    protected static final String TRANS_AMOUNT_SLOT = "transactionAmount";

    protected static final String RECIPIENT_SLOT = "recipient";

    protected static final String ACCOUNT_TYPE_SLOT = "accountType";

    protected final Logger LOG = LogManager.getLogger(this.getClass());

    // The OAuth client adapter.
    protected final OAuthAdapter clientAdapter;

    // The Spring application context.
    protected final ApplicationContext applicationContext;

    // The slots map with key as slotName and Slot object as the value.
    protected Map<String, Slot> slots;

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
    public BaseRequestHandler(
            OAuthAdapter clientAdapter,
            ApplicationContext applicationContext
    ) {
        this.clientAdapter = clientAdapter;
        this.applicationContext = applicationContext;
    }

    @Override
    public abstract boolean canHandle(HandlerInput handlerInput);

    /**
     * Base implementation of the {@link RequestHandler#handle(Object)}
     * method which just sets the {@link #slots},
     * obtained from the {@link HandlerInput}.
     *
     * @param handlerInput input to the request handler
     * @return {@link Optional#empty()}.
     */
    @Override
    public Optional<Response> handle(HandlerInput handlerInput) {
        // The correct value should be returned by the class that overrides this method.
        IntentRequest intentRequest = (IntentRequest) handlerInput.getRequestEnvelope().getRequest();
        slots = intentRequest.getIntent().getSlots();
        return Optional.empty();
    }

    /**
     * Gets the linked user information using the access token.
     *
     * @param input The incoming request.
     * @return {@link User} information.
     */
    protected User getUser(HandlerInput input) {
        String accessTokenStr = input.getRequestEnvelope()
                .getSession()
                .getUser()
                .getAccessToken();
        LOG.info("Received request with access token: " + accessTokenStr);
        if (accessTokenStr == null) {
            return null;
        }
        return clientAdapter.getUser(accessTokenStr);
    }
}
