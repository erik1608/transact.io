package com.snee.transactio.db.entities.transaction;

import com.snee.transactio.db.entities.user.User;
import com.snee.transactio.db.entities.user.UserAccount;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String transactionId;

    @OneToOne
    private User sender;

    @OneToOne
    private UserAccount outgoingAccount;

    @OneToOne
    private User recipient;

    @OneToOne
    private UserAccount recipientAccount;

    private int amount;

    private String status;

    public Integer getId() {
        return id;
    }

    public User getSender() {
        return sender;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public User getRecipient() {
        return recipient;
    }

    public UserAccount getOutgoingAccount() {
        return outgoingAccount;
    }

    public int getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public UserAccount getRecipientAccount() {
        return recipientAccount;
    }

    public Transaction setTransactionId(String transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public Transaction setRecipient(User recipient) {
        this.recipient = recipient;
        return this;
    }

    public Transaction setOutgoingAccount(UserAccount outgoingAccount) {
        this.outgoingAccount = outgoingAccount;
        return this;
    }

    public Transaction setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public Transaction setStatus(String status) {
        this.status = status;
        return this;
    }

    public Transaction setRecipientAccount(UserAccount recipientAccount) {
        this.recipientAccount = recipientAccount;
        return this;
    }

    public Transaction setSender(User sender) {
        this.sender = sender;
        return this;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", transactionId='" + transactionId + '\'' +
                ", sender=" + sender +
                ", outgoingAccount=" + outgoingAccount +
                ", recipient=" + recipient +
                ", recipientAccount=" + recipientAccount +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                '}';
    }
}
