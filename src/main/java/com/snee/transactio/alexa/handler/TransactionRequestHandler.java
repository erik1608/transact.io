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

public class TransactionRequestHandler extends BaseRequestHandler {

    private final PushNotificationService mPushNotificationService;

    private final TransactionService mTransactionService;

    /**
     *
     * @param clientAdapter
     * @param applicationContext
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
     *
     * @param handlerInput input to the request handler
     * @return
     */
    @Override
    public boolean canHandle(HandlerInput handlerInput) {
        return handlerInput.matches(Predicates.intentName("TransactionIntent"));
    }

    /**
     *
     * @param handlerInput input to the request handler
     * @return
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
            return processTransaction(handlerInput, user, transactionAmount, outgoingAccountName, friendAlias);
        }

        return handlerInput.getResponseBuilder()
                .withSpeech("Failed to process the request")
                .withSimpleCard(SKILL_TITLE, "Failed to process the request")
                .build();
    }

    /**
     *
     * @param handlerInput
     * @param user
     * @param txAmount
     * @param outgoingAccountName
     * @param friendAlias
     * @return
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
            responseSpeechBuilder.append(", the requested ")
                    .append(outgoingAccountName)
                    .append(" is unknown to your profile.");
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
                    ", please register a biometric credential" +
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

        sendPush(txAmount, device, transaction);

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
     *
     * @param user
     * @param repo
     * @return
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
     *
     * @param user
     * @param friendAlias
     * @return
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
     *
     * @param transactionAmount
     * @param device
     * @param transaction
     */
    private void sendPush(
            int transactionAmount,
            UserDevice device,
            Transaction transaction
    ) {

        HashMap<String, String> details = new HashMap<>();
        details.put("transactionId", transaction.getTransactionId());
        details.put("type", "transactionConfirmation");
        String message = String.format(
                "Confirm transaction with total amount %d made using Alexa",
                transactionAmount
        );
        mPushNotificationService.sendNotification(
                "Confirm the transaction",
                message,
                details,
                device
        );
    }
}
