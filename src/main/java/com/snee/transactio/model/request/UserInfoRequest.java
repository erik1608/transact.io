package com.snee.transactio.model.request;

import com.snee.transactio.exceptions.RequestValidationException;
import com.snee.transactio.model.RequestModel;
import com.snee.transactio.model.Session;
import com.google.gson.annotations.Expose;

public class UserInfoRequest implements RequestModel {
	@Expose
	private Session sessionData;

	public Session getSessionData() {
		return sessionData;
	}

	@Override
	public void validate() {
		if (sessionData == null) {
			throw new RequestValidationException("Session is required");
		}
		sessionData.validate();
	}
}
