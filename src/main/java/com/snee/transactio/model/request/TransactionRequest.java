package com.snee.transactio.model.request;

import com.snee.transactio.exceptions.RequestValidationException;
import com.snee.transactio.model.RequestModel;
import com.snee.transactio.model.Session;
import com.google.gson.annotations.Expose;

public class TransactionRequest implements RequestModel {
	@Expose
	private String outgoingAccountNumber;

	@Expose
	private String recipientAccountNumber;

	@Expose
	private String amount;

	@Expose
	private Session sessionData;

	@Override
	public void validate() {
		if (outgoingAccountNumber == null || outgoingAccountNumber.isEmpty()) {
			throw new RequestValidationException("Account number is required.");
		}

		if (recipientAccountNumber == null || recipientAccountNumber.isEmpty()) {
			throw new RequestValidationException("Recipient account number is required.");
		}

		if (amount == null || amount.isEmpty() || !isNumeric(amount)) {
			throw new RequestValidationException("Invalid amount provided.");
		}

		sessionData.validate();
	}

	public String getRecipientAccountNumber() {
		return recipientAccountNumber;
	}

	public String getOutgoingAccountNumber() {
		return outgoingAccountNumber;
	}

	public String getAmount() {
		return amount;
	}

	public Session getSessionData() {
		return sessionData;
	}

	private boolean isNumeric(String strNum) {
		if (strNum == null) {
			return false;
		}
		try {
			Double.parseDouble(strNum);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
}
