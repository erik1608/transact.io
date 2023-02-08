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

    private final static ExampleMatcher TRANSACTION_ID_MATCHER = ExampleMatcher.matchingAny()
            .withIgnorePaths("id", "amount")
            .withMatcher("transactionId", ignoreCase());

    private final UsersAccountRepo mAccountsRepo;
    private final TransactionRepo mTransactionRepo;
    private final PushNotificationService mPNS;

    public TransactionService(Repos repos,
                              PushNotificationService pns) {
        mAccountsRepo = repos.get(UsersAccountRepo.class);
        mTransactionRepo = repos.get(TransactionRepo.class);
        mPNS = pns;
    }

    @Transactional
    public Transaction performTransaction(User sender, UserAccount outgoingAccount,
                                          User recipient, UserAccount recipientAccount,
                                          int amount) {

        if (recipient.equals(sender) || recipient.getUsername().equals(sender.getUsername())) {
            throw new RequestValidationException("Invalid recipient account");
        }

        if (amount == 0 || amount > outgoingAccount.getBalance()) {
            throw new RequestValidationException("Insufficient account balance");
        }

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

    @Transactional
    public TransactionResponse completeTransaction(String transactionId) {
        TransactionResponse response = new TransactionResponse();
        Transaction transaction;
        Example<Transaction> transactionExample = Example.of(new Transaction().setTransactionId(transactionId), TRANSACTION_ID_MATCHER);
        Optional<Transaction> foundTransaction = mTransactionRepo.findOne(transactionExample);
        if (foundTransaction.isPresent()) {
            transaction = foundTransaction.get();
        } else {
            throw new RequestValidationException("Unknown transactionId");
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

            HashMap<String, String> transactionDetails = new HashMap<>();
            transactionDetails.put("type", "transactionDetails");
            transactionDetails.put("transactionId", transactionId);
            transactionDetails.put("sender", transaction.getOutgoingAccount().getNumber());
            transactionDetails.put("amount", Integer.toString(transaction.getAmount()));
            transactionDetails.put("receivedTo", transaction.getRecipientAccount().getNumber());

            for (UserDevice recipientDevice : recipientDevices) {
                mPNS.sendNotification("Credits received", confirmationText, transactionDetails, recipientDevice);
            }

            return response.setAmount(transaction.getAmount())
                    .setTransactionId(transaction.getTransactionId())
                    .setStatus(TransactionResponse.Status.COMPLETED)
                    .setRecipientAccountNumber(recipientAccount.getNumber());
        }
    }
}
