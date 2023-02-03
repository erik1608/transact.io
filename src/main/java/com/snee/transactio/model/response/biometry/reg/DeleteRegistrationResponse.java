package com.snee.transactio.model.response.biometry.reg;

import com.snee.transactio.model.Session;
import com.snee.transactio.model.response.biometry.BiometryResponse;
import com.google.gson.annotations.Expose;

public class DeleteRegistrationResponse extends BiometryResponse {
	@Expose
	private String status;

	@Override
	public <T extends BiometryResponse> T setSessionData(Session sessionData) {
		return null;
	}

	public String getStatus() {
		return status;
	}

	public DeleteRegistrationResponse setStatus(String status) {
		this.status = status;
		return this;
	}
}
