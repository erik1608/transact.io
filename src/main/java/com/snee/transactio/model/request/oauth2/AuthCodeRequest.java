package com.snee.transactio.model.request.oauth2;

import com.google.gson.annotations.Expose;
import com.snee.transactio.exceptions.RequestValidationException;
import com.snee.transactio.model.RequestModel;
import com.snee.transactio.model.Session;

public class AuthCodeRequest implements RequestModel {
	@Expose
	private String client_id;

	@Expose
	private String redirect_uri;

	@Expose
	private String scope;

	@Expose
	private String state;

	@Expose
	private Session sessionData;

	public String getClientId() {
		return client_id;
	}

	public String getRedirectUri() {
		return redirect_uri;
	}

	public String getScope() {
		return scope;
	}

	public String getState() {
		return state;
	}

	public Session getSessionData() {
		return sessionData;
	}

	@Override
	public void validate() {
		if (client_id == null || client_id.isEmpty()) {
			throw new RequestValidationException(
					"client_id is required"
			);
		}

		if (redirect_uri == null || redirect_uri.isEmpty()) {
			throw new RequestValidationException(
					"redirect_uri is required"
			);
		}

		if (scope == null || scope.isEmpty()) {
			throw new RequestValidationException(
					"scope is required"
			);
		}

		if (state == null || state.isEmpty()) {
			throw new RequestValidationException(
					"state is required"
			);
		}


		if (sessionData == null) {
			throw new RequestValidationException(
					"sessionData is required"
			);
		}
	}
}
