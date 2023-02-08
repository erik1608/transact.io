package com.snee.transactio.alexa.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.snee.transactio.db.entities.user.User;
import com.snee.transactio.oauth2.adapter.OAuthAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

/**
 * Base class for the Skill request handlers.
 * Common methods for all request handlers must be implmented here,
 * and should have protected access.
 */
public abstract class BaseRequestHandler implements RequestHandler {
	protected static final String SKILL_TITLE = "Transact.IO";

	protected final Logger LOG = LogManager.getLogger(this.getClass());
	protected final OAuthAdapter clientAdapter;
	protected final ApplicationContext applicationContext;

	public BaseRequestHandler(
			OAuthAdapter clientAdapter,
			ApplicationContext applicationContext
	) {
		this.clientAdapter = clientAdapter;
		this.applicationContext = applicationContext;
	}

	@Override
	public abstract boolean canHandle(HandlerInput handlerInput);

	@Override
	public abstract Optional<Response> handle(HandlerInput handlerInput);

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
