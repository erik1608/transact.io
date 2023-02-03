package com.snee.transactio.model.request.oauth2;

import com.snee.transactio.exceptions.RequestValidationException;
import com.snee.transactio.model.RequestModel;

public class TokenRequest implements RequestModel {
	private String grant_type;
	private String code;
	private String redirect_uri;

	@Override
	public void validate() {
		if (grant_type == null || grant_type.isEmpty()) {
			throw new RequestValidationException("Grant type is required");
		} else if (!grant_type.equals("authorization_code")) {
			throw new RequestValidationException("Invalid grant type provided");
		}

		if (code == null || code.isEmpty()) {
			throw new RequestValidationException("Auth code is required");
		}

		if (redirect_uri == null || redirect_uri.isEmpty()) {
			throw new RequestValidationException("Redirect URI is required");
		}
	}

	@Override
	public String toString() {
		return "grant_type=[" + grant_type + "], \n" +
				"code=[" + code + "] \n" +
				"redirect_uri=[" + redirect_uri + "]";
	}

	public String getGrantType() {
		return grant_type;
	}

	public String getCode() {
		return code;
	}

	public String getRedirectUri() {
		return redirect_uri;
	}

	public void setGrantType(String grant_type) {
		this.grant_type = grant_type;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setRedirectUri(String redirect_uri) {
		this.redirect_uri = redirect_uri;
	}
}
