package com.snee.transactio.model.response;

import com.snee.transactio.model.ResponseModel;
import com.snee.transactio.model.Session;
import com.google.gson.annotations.Expose;
import org.springframework.http.HttpStatus;

public class RegistrationResponse implements ResponseModel {
	@Expose
	private HttpStatus status;

	@Expose
	private String message;

	@Expose
	private Session sessionData;

	public RegistrationResponse setStatus(HttpStatus status) {
		this.status = status;
		return this;
	}

	public RegistrationResponse setMessage(String message) {
		this.message = message;
		return this;
	}

	public RegistrationResponse setSessionData(Session sessionData) {
		this.sessionData = sessionData;
		return this;
	}
}
