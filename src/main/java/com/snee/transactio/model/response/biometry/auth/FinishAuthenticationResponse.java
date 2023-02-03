package com.snee.transactio.model.response.biometry.auth;

import com.snee.transactio.model.Session;
import com.snee.transactio.model.response.biometry.BiometryResponse;
import com.google.gson.annotations.Expose;

public class FinishAuthenticationResponse extends BiometryResponse {
	@Expose
	private String status;


	@Override
	public <T extends BiometryResponse> T setSessionData(Session sessionData) {
		this.sessionData = sessionData;
		return (T) this;
	}

	public String getStatus() {
		return status;
	}

	public FinishAuthenticationResponse setStatus(String status) {
		this.status = status;
		return this;
	}
}
