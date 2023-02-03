package com.snee.transactio.model.request;

import com.snee.transactio.exceptions.RequestValidationException;
import com.snee.transactio.model.RequestModel;
import com.snee.transactio.model.Session;
import com.google.gson.annotations.Expose;

public class LoginRequest implements RequestModel {

	@Expose
	private String usernameOrEmail;

	@Expose
	private String password;

	@Expose
	private String mode;

	@Expose
	private Session sessionData;

	@Expose
	private Device deviceInfo;

	public void validate() {
		if ("create".equals(mode)) {
			if (usernameOrEmail == null || usernameOrEmail.isEmpty()) {
				throw new RequestValidationException("Username or email is required");
			}

			if (password == null || password.isEmpty()) {
				throw new RequestValidationException("Password is required");
			}

			if (deviceInfo != null) {
				deviceInfo.validate();
			}
		} else if ("renew".equals(mode)) {
			if (sessionData == null) {
				throw new RequestValidationException("Session data is required");
			}

			sessionData.validate();
		}
	}

	public String getUsernameOrEmail() {
		return usernameOrEmail;
	}

	public String getPassword() {
		return password;
	}

	public String getMode() {
		return mode;
	}

	public Session getSessionData() {
		return sessionData;
	}

	public Device getDeviceInfo() {
		return deviceInfo;
	}
}
