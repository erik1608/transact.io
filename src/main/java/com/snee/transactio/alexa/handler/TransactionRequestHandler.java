package com.snee.transactio.alexa.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.amazon.ask.request.Predicates;
import com.snee.transactio.alexa.constants.SlotName;
import com.snee.transactio.db.entities.transaction.Transaction;
import com.snee.transactio.db.entities.user.Biometrics;
import com.snee.transactio.db.entities.user.User;
import com.snee.transactio.db.entities.user.UserAccount;
import com.snee.transactio.db.entities.user.UserDevice;
import com.snee.transactio.db.entities.user.UserRelationMapping;
import com.snee.transactio.db.repo.BiometricsRepo;
import com.snee.transactio.db.repo.Repos;
import com.snee.transactio.oauth2.adapter.OAuthAdapter;
import com.snee.transactio.service.PushNotificationService;
import com.snee.transactio.service.TransactionService;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TransactionRequestHandler extends BaseRequestHandler {

	private final PushNotificationService mPushNotificationService;
	private final TransactionService mTransactionService;

	public TransactionRequestHandler(
			OAuthAdapter clientAdapter,
			ApplicationContext applicationContext
	) {
		super(clientAdapter, applicationContext);
		mPushNotificationService = applicationContext.getBean(PushNotificationService.class);
		mTransactionService = applicationContext.getBean(TransactionService.class);
	}

	@Override
	public boolean canHandle(HandlerInput handlerInput) {
		return handlerInput.matches(Predicates.intentName("TransactionIntent"));
	}

	@Override
	public Optional<Response> handle(HandlerInput handlerInput) {
		User user = getUser(handlerInput);
		if (user == null) {
			return handlerInput.getResponseBuilder()
					.withSpeech("Please link your account with me")
					.withLinkAccountCard().build();
		}
		StringBuilder responseSpeechBuilder = new StringBuilder("Dear ")
				.append(user.getFirstname())
				.append(" ")
				.append(user.getLastname());
		IntentRequest intentRequest = (IntentRequest) handlerInput.getRequestEnvelope().getRequest();
		Map<String, Slot> slots = intentRequest.getIntent().getSlots();
		int transactionAmount = Integer.parseInt(slots.get(SlotName.TRANS_AMOUNT).getValue());
		String outgoingAccountName = slots.get(SlotName.ACCOUNT_TYPE).getValue();
		String recipientAlias = slots.get(SlotName.RECIPIENT).getValue();
		if (recipientAlias != null) {
			UserRelationMapping friendMap = null;
			List<UserRelationMapping> userRelationMappingList = user.getFriends();
			for (UserRelationMapping friend : userRelationMappingList) {
				if (friend.getAlias().equalsIgnoreCase(recipientAlias)) {
					friendMap = friend;
				}
			}

			if (friendMap == null) {
				String response = String.format(
						"The user with alias %s is not associated with you. " +
						"Make sure that the user you are looking for transaction is in " +
								"your friends list and has pronounceable alias.", recipientAlias
				);
				return handlerInput.getResponseBuilder()
						.withSpeech(response)
						.withSimpleCard(CARD_TITLE, response)
						.build();
			}

			UserAccount account = user.getAccountByName(outgoingAccountName);
			if (account == null) {
				responseSpeechBuilder.append(", the requested ").append(outgoingAccountName).append(" is unknown to your profile.");
				return handlerInput.getResponseBuilder()
						.withSpeech(responseSpeechBuilder.toString())
						.withSimpleCard(CARD_TITLE, responseSpeechBuilder.toString())
						.build();
			}

			User recipient = friendMap.getFriend();
			UserAccount recipientAccount = recipient.getPrimaryAccount();
			BiometricsRepo repo = applicationContext.getBean(Repos.class).get(BiometricsRepo.class);
			UserDevice biometryEligibleDevice = null;
			for (UserDevice device : user.getUserDevices()) {
				Biometrics biometrics = repo.findByUserAndDevice(user, device);
				if (biometrics != null) {
					biometryEligibleDevice = device;
					break;
				}
			}

			if (biometryEligibleDevice == null) {
				String message = "You don't have a device that is eligible for transaction " +
						"confirmation, please register a biometric credential and try again.";

				return handlerInput.getResponseBuilder()
						.withSpeech(message)
						.withSimpleCard(CARD_TITLE, message).build();
			}

			Transaction transaction = mTransactionService.performTransaction(
					user,
					account,
					recipient,
					recipientAccount,
					transactionAmount
			);

			HashMap<String, String> details = new HashMap<>();
			details.put("transactionId", transaction.getTransactionId());
			details.put("type", "transactionConfirmation");

			mPushNotificationService.sendNotification(
					"Confirm the transaction",
					String.format("Confirm transaction with total amount %d made using Alexa", transactionAmount),
					details,
					biometryEligibleDevice
			);
			responseSpeechBuilder.append(", a push notification has been sent to your device, please complete the transaction for the funds to be transferred.");
			return handlerInput.getResponseBuilder()
					.withSpeech(responseSpeechBuilder.toString())
					.withSimpleCard(CARD_TITLE, responseSpeechBuilder.toString())
					.build();
		}

		return handlerInput.getResponseBuilder()
				.withSpeech("Failed to process the request")
				.withSimpleCard(CARD_TITLE, "Failed to process the request")
				.build();
	}
}
