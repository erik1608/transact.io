package com.snee.transactio.service;

import com.snee.transactio.db.entities.transaction.Transaction;
import com.snee.transactio.db.entities.user.User;
import com.snee.transactio.db.entities.user.UserAccount;
import com.snee.transactio.db.entities.user.UserDevice;
import com.snee.transactio.db.repo.Repos;
import com.snee.transactio.db.repo.TransactionRepo;
import com.snee.transactio.db.repo.UsersAccountRepo;
import com.snee.transactio.exceptions.RequestValidationException;
import com.snee.transactio.model.response.TransactionResponse;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.ignoreCase;


@Service
public class TransactionService {

    private final static ExampleMatcher TRANSACTION_ID_MATCHER =
            ExampleMatcher.matchingAny()
                    .withIgnorePaths("id", "amount")
                    .withMatcher("transactionId", ignoreCase());

    private final UsersAccountRepo mAccountsRepo;

    private final TransactionRepo mTransactionRepo;

    private final PushNotificationService mPNS;


    /**
     * A microservice that performs transactions.
     *
     * @param repos The repositories.
     * @param pns   The push notification service.
     */
    public TransactionService(
            Repos repos,
            PushNotificationService pns
    ) {
        mAccountsRepo = repos.get(UsersAccountRepo.class);
        mTransactionRepo = repos.get(TransactionRepo.class);
        mPNS = pns;
    }

    /**
     * Starts a transaction in a pending state.
     *
     * @param sender           The sender user.
     * @param outgoingAccount  The sender account.
     * @param recipient        The recipient user.
     * @param recipientAccount The recipient account.
     * @param amount           The transaction amount.
     * @return {@link Transaction} object in a pending state.
     */
    @Transactional
    public Transaction performTransaction(
            User sender, UserAccount outgoingAccount,
            User recipient, UserAccount recipientAccount,
            int amount
    ) {

        // Validate the incoming data.
        validateParams(sender, outgoingAccount, recipient, amount);

        TransactionResponse.Status status = TransactionResponse.Status.PENDING;
        String transactionId = UUID.randomUUID().toString();
        Transaction transaction = new Transaction()
                .setTransactionId(transactionId)
                .setSender(sender)
                .setOutgoingAccount(outgoingAccount)
                .setAmount(amount)
                .setRecipient(recipient)
                .setRecipientAccount(recipientAccount)
                .setStatus(status.name());

        mTransactionRepo.save(transaction);
        return transaction;
    }

    /**
     * Completes the transaction with the specified id, if it exists.
     * Once the transaction found a push notification will be sent.
     *
     * @param transactionId The transaction id to complete.
     * @return {@link TransactionResponse} the response of the transaction.
     */
    @Transactional
    public TransactionResponse completeTransaction(String transactionId) {
        TransactionResponse response = new TransactionResponse();
        Example<Transaction> transactionExample = Example.of(
                new Transaction().setTransactionId(transactionId),
                TRANSACTION_ID_MATCHER
        );

        Transaction transaction;
        Optional<Transaction> tx = mTransactionRepo.findOne(transactionExample);
        if (tx.isPresent()) {
            transaction = tx.get();
        } else {
            throw new RequestValidationException(
                    "Unknown transactionId"
            );
        }

        synchronized (this) {
            int amount = transaction.getAmount();
            UserAccount outgoingAccount = transaction.getOutgoingAccount();
            if (amount > outgoingAccount.getBalance()) {
                throw new RequestValidationException("Insufficient Balance");
            }

            UserAccount recipientAccount = transaction.getRecipientAccount();
            recipientAccount.setBalance(recipientAccount.getBalance() + amount);
            outgoingAccount.setBalance(outgoingAccount.getBalance() - amount);

            mAccountsRepo.saveAll(Arrays.asList(recipientAccount, outgoingAccount));
            transaction.setStatus(TransactionResponse.Status.COMPLETED.name());
            mTransactionRepo.save(transaction);

            // Send a notification with the transaction details.
            User recipient = transaction.getRecipient();
            User sender = transaction.getSender();
            List<UserDevice> recipientDevices = recipient.getUserDevices();
            String confirmationText = sender.getUsername() +
                    " has sent you " + transaction.getAmount() +
                    " credits to your " + transaction.getRecipientAccount().getNumber() + " account";

            // Construct the transaction details for push notification.
            HashMap<String, String> transactionDetails = new HashMap<>();
            transactionDetails.put("type", "transactionDetails");
            transactionDetails.put("transactionId", transactionId);
            transactionDetails.put("sender", transaction.getOutgoingAccount().getNumber());
            transactionDetails.put("amount", Integer.toString(transaction.getAmount()));
            transactionDetails.put("receivedTo", transaction.getRecipientAccount().getNumber());

            for (UserDevice recipientDevice : recipientDevices) {
                mPNS.sendNotification(
                        "Credits received",
                        confirmationText,
                        transactionDetails,
                        recipientDevice
                );
            }

            return response.setAmount(transaction.getAmount())
                    .setTransactionId(transaction.getTransactionId())
                    .setStatus(TransactionResponse.Status.COMPLETED)
                    .setRecipientAccountNumber(recipientAccount.getNumber());
        }
    }

    private static void validateParams(
            User sender,
            UserAccount account,
            User recipient,
            int amount
    ) {

        String recipientUser = recipient.getUsername();
        String senderUser = sender.getUsername();
        int senderBalance = account.getBalance();

        // Check if the user sends money to himself.
        if (recipient.equals(sender) || recipientUser.equals(senderUser)) {
            throw new RequestValidationException(
                    "Invalid recipient account"
            );
        }

        // Check if there is sufficient amount of money in the account.
        if (amount == 0 || amount > senderBalance) {
            throw new RequestValidationException(
                    "Insufficient account balance"
            );
        }
    }
}
