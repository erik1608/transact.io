package com.snee.transactio.alexa.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;
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
import java.util.Optional;

/**
 * Handles the account balance requests. <br>
 * <p>
 * Intent - <b>AccountBalanceIntent</b> <br>
 * Slot names -
 * <PRE>
 * [
 * {@link BaseRequestHandler#ACCOUNT_TYPE_SLOT},
 * {@link BaseRequestHandler#RECIPIENT_SLOT},
 * {@link BaseRequestHandler#TRANS_AMOUNT_SLOT}
 * ]
 * </PRE>
 * <p>
 * Utterance samples:
 * <ul>
 *  <li>Alexa, ask <b>transact.io</b>
 *      to send {{@link BaseRequestHandler#TRANS_AMOUNT_SLOT}}
 *      credits to {{@link BaseRequestHandler#RECIPIENT_SLOT}}
 *      using my {{@link BaseRequestHandler#ACCOUNT_TYPE_SLOT}}.</li>
 *      <li>Alexa, ask <b>transact.io</b>
 *      to transfer {{@link BaseRequestHandler#TRANS_AMOUNT_SLOT}}
 *      credits to {{@link BaseRequestHandler#RECIPIENT_SLOT}}
 *      using my {{@link BaseRequestHandler#ACCOUNT_TYPE_SLOT}}.</li>
 * </ul>
 * <p>
 * Slot values: <br>
 * {@link BaseRequestHandler#ACCOUNT_TYPE_SLOT} - ["checking account"].
 * {@link BaseRequestHandler#RECIPIENT_SLOT} - The user that is friends with you.
 * {@link BaseRequestHandler#TRANS_AMOUNT_SLOT} - The transaction amount.
 */
@SuppressWarnings("unused")
public class TransactionRequestHandler extends BaseRequestHandler {

    private final PushNotificationService mPushNotificationService;

    private final TransactionService mTransactionService;

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
    public TransactionRequestHandler(
            OAuthAdapter clientAdapter,
            ApplicationContext applicationContext
    ) {
        super(clientAdapter, applicationContext);
        mPushNotificationService = applicationContext.getBean(
                PushNotificationService.class
        );

        mTransactionService = applicationContext.getBean(
                TransactionService.class
        );
    }

    /**
     * Handles the transaction requests.
     * Intent name - "TransactionIntent".
     *
     * @param handlerInput input to the request handler
     * @return true if eligible, false otherwise.
     */
    @Override
    public boolean canHandle(HandlerInput handlerInput) {
        return handlerInput.matches(
                Predicates.intentName("TransactionIntent")
        );
    }

    /**
     * Handles the transaction request,
     * If a transaction confirmation is required for the request,
     * sends a push notification for confirmation to the user that made the request.
     *
     * @param handlerInput input to the request handler
     * @return The response to the handled operation.
     */
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
        int transactionAmount = Integer.parseInt(
                slots.get(TRANS_AMOUNT_SLOT).getValue()
        );

        String outgoingAccountName = slots.get(ACCOUNT_TYPE_SLOT).getValue();
        String friendAlias = slots.get(RECIPIENT_SLOT).getValue();
        if (friendAlias != null) {
            return processTransaction(
                    handlerInput,
                    user,
                    transactionAmount,
                    outgoingAccountName,
                    friendAlias
            );
        }

        return handlerInput.getResponseBuilder()
                .withSpeech("Failed to process the request")
                .withSimpleCard(SKILL_TITLE, "Failed to process the request")
                .build();
    }

    /**
     * @param handlerInput        The handler input.
     * @param user                The user that performs the transaction.
     * @param txAmount            The transaction amount.
     * @param outgoingAccountName The account name to transfer the funds from.
     * @param friendAlias         The alias of the friend to send the transaction.
     * @return The response of the proceeded operation.
     */
    private Optional<Response> processTransaction(
            HandlerInput handlerInput,
            User user,
            int txAmount,
            String outgoingAccountName,
            String friendAlias
    ) {
        UserRelationMapping friend = getFriend(user, friendAlias);
        if (friend == null) {
            String response = String.format(
                    "The user with alias %s is not associated with you. " +
                            "Make sure that the user you are looking" +
                            " for transaction is in " +
                            "your friends list and has pronounceable " +
                            "alias.", friendAlias
            );
            return handlerInput.getResponseBuilder()
                    .withSpeech(response)
                    .withSimpleCard(SKILL_TITLE, response)
                    .build();
        }

        StringBuilder responseSpeechBuilder = new StringBuilder("Dear ")
                .append(user.getFirstname())
                .append(" ")
                .append(user.getLastname());
        UserAccount account = user.getAccountByName(outgoingAccountName);
        if (account == null) {
            if (outgoingAccountName != null) {
                responseSpeechBuilder.append(", the requested ")
                        .append(outgoingAccountName)
                        .append(" is unknown to your profile.");
            } else {
                responseSpeechBuilder.append(", you did not specify ")
                        .append("the outgoing account name.");
            }

            return handlerInput.getResponseBuilder()
                    .withSpeech(responseSpeechBuilder.toString())
                    .withSimpleCard(
                            SKILL_TITLE,
                            responseSpeechBuilder.toString()
                    )
                    .build();
        }

        User recipient = friend.getFriend();
        UserAccount recipientAccount = recipient.getPrimaryAccount();
        BiometricsRepo repo = applicationContext
                .getBean(Repos.class)
                .get(BiometricsRepo.class);
        UserDevice device = getUserDevice(user, repo);
        if (device == null) {
            String message = "You don't have a device," +
                    " that is eligible for transaction confirmation" +
                    ", please reg a biometric credential" +
                    " and try again.";

            return handlerInput.getResponseBuilder()
                    .withSpeech(message)
                    .withSimpleCard(SKILL_TITLE, message)
                    .build();
        }

        Transaction transaction = mTransactionService.performTransaction(
                user,
                account,
                recipient,
                recipientAccount,
                txAmount
        );

        sendPush(device, transaction);

        responseSpeechBuilder.append(", a push notification has been " +
                "sent to your device," +
                " please complete the transaction" +
                " for the funds to be transferred.");

        return handlerInput.getResponseBuilder()
                .withSpeech(responseSpeechBuilder.toString())
                .withSimpleCard(
                        SKILL_TITLE,
                        responseSpeechBuilder.toString()
                )
                .build();
    }

    /**
     * Finds a device bound to the user that has a biometric registration.
     *
     * @param user The user that performs the transaction.
     * @param repo The {@link Biometrics} repository.
     * @return The user device that has a biometric registration.
     */
    private static UserDevice getUserDevice(User user, BiometricsRepo repo) {
        for (UserDevice uDevice : user.getUserDevices()) {
            Biometrics reg = repo.findByUserAndDevice(user, uDevice);
            if (reg != null) {
                return uDevice;
            }
        }
        return null;
    }

    /**
     * Gets the friend from the {@link User} friends list.
     *
     * @param user        The user that made the request.
     * @param friendAlias the friend alias that is mentioned in the request.
     * @return The found {@link UserRelationMapping}.
     */
    private UserRelationMapping getFriend(User user, String friendAlias) {
        List<UserRelationMapping> friends = user.getFriends();
        for (UserRelationMapping friend : friends) {
            if (friend.getAlias().equalsIgnoreCase(friendAlias)) {
                return friend;
            }
        }
        return null;
    }

    /**
     * Sends a push notification to the user,
     * that has sent the transaction request,
     * to complete the transaction using the biometry registration.
     *
     * @param device      The device to send the push to.
     * @param transaction The transaction object.
     */
    private void sendPush(
            UserDevice device,
            Transaction transaction
    ) {

        HashMap<String, String> details = new HashMap<>();
        details.put("transactionId", transaction.getTransactionId());
        details.put("type", "transactionConfirmation");
        String message = String.format(
                "Confirm transaction with total amount %d made using Alexa",
                transaction.getAmount()
        );
        mPushNotificationService.sendNotification(
                "Confirm the transaction",
                message,
                details,
                device
        );
    }
}
