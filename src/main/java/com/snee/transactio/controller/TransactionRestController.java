package com.snee.transactio.controller;

import com.snee.transactio.db.entities.transaction.Transaction;
import com.snee.transactio.db.entities.user.User;
import com.snee.transactio.db.entities.user.UserAccount;
import com.snee.transactio.exceptions.RequestValidationException;
import com.snee.transactio.model.Session;
import com.snee.transactio.model.request.TransactionRequest;
import com.snee.transactio.model.response.TransactionResponse;
import com.snee.transactio.service.AuthMgmtService;
import com.snee.transactio.service.TransactionService;
import com.snee.transactio.service.UserHandlerService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("${api.prefix}/user/transact")
public class TransactionRestController {

    private final UserHandlerService mUserHandlerService;
    private final AuthMgmtService mAuthService;
    private final TransactionService mTransactionService;

    public TransactionRestController(UserHandlerService userHandlerService,
                                     AuthMgmtService authService,
                                     TransactionService transactionService) {
        mAuthService = authService;
        mUserHandlerService = userHandlerService;
        mTransactionService = transactionService;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<TransactionResponse> transact(@RequestBody TransactionRequest request) {
        // Validate the incoming request.
        request.validate();

        // Get the transaction recipient.
        UserAccount recipientAccount = new UserAccount();
        recipientAccount.setNumber(request.getRecipientAccountNumber());
        User transactionRecipient = mUserHandlerService.getUserByAccount(recipientAccount);
        if (transactionRecipient == null) {
            throw new RequestValidationException("Recipient user not found");
        }

        for (UserAccount userAccount : transactionRecipient.getUserAccounts()) {
            if (userAccount.getNumber().equals(recipientAccount.getNumber())) {
                recipientAccount = userAccount;
                break;
            }
        }

        // Get the outgoing account and validate it.
        Session session = mAuthService.validateSession(request.getSessionData());
        User userInfo = mUserHandlerService.getUser(session.getSubject());
        String accountNumber = request.getOutgoingAccountNumber();
        AtomicReference<UserAccount> outgoingAccount = new AtomicReference<>();
        userInfo.getUserAccounts().forEach((account) -> {
            if (accountNumber.equals(account.getNumber())) {
                outgoingAccount.set(account);
            }
        });

        if (outgoingAccount.get() == null) {
            throw new RequestValidationException("Unknown outgoing account selected");
        }

        Transaction responseTransaction = mTransactionService.performTransaction(
                userInfo, outgoingAccount.get(),
                transactionRecipient, recipientAccount,
                Integer.parseInt(request.getAmount())
        );

        TransactionResponse response = new TransactionResponse();
        if (TransactionResponse.Status.PENDING.equals(TransactionResponse.Status.valueOf(responseTransaction.getStatus()))) {
            // Since the transaction is done within the application API call, complete it right away.
            response = mTransactionService.completeTransaction(responseTransaction.getTransactionId());
        }

        response.setSessionData(session);
        return ResponseEntity.of(Optional.of(response));
    }
}
