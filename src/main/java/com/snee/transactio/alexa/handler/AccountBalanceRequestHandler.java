package com.snee.transactio.alexa.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.amazon.ask.request.Predicates;
import com.snee.transactio.alexa.constants.SlotName;
import com.snee.transactio.db.entities.user.User;
import com.snee.transactio.db.entities.user.UserAccount;
import com.snee.transactio.oauth2.adapter.OAuthAdapter;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.Optional;

/**
 * The account 3 request handler. <br>
 * <p>
 * Intent - <b>AccountBalanceIntent</b> <br>
 * Slot names - [{@link SlotName#ACCOUNT_TYPE}] <br>
 * <p>
 * Utterance samples:
 * <ul>
 *  <li>Alexa, ask <b>diploma work</b> for my {@link SlotName#ACCOUNT_TYPE} balance.</li>
 * </ul>
 * <p>
 * Slot values: <br>
 * {@link SlotName#ACCOUNT_TYPE} - ["checking account"]
 */
public class AccountBalanceRequestHandler extends BaseRequestHandler {
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
		User user = getUser(handlerInput);
		if (user == null) {
			return handlerInput.getResponseBuilder()
					.withSpeech("Please link your account with me")
					.withLinkAccountCard()
					.build();
		}
		StringBuilder responseSpeechBuilder = new StringBuilder("Dear ")
				.append(user.getFirstname()).append(" ").append(user.getLastname());
		IntentRequest intentRequest = (IntentRequest) handlerInput.getRequestEnvelope().getRequest();
		Map<String, Slot> slots = intentRequest.getIntent().getSlots();
		Slot accountTypeSlot = slots.get(SlotName.ACCOUNT_TYPE);
		String accountType = accountTypeSlot.getValue();
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
