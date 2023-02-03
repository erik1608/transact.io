package com.snee.transactio.model.response.oauth2;

import com.google.gson.annotations.Expose;

public class AuthCodeResponse {
	@Expose
	private String code;

	@Expose
	private String state;

	@Expose
	private String error;

	@Expose
	private String error_description;

	public String getCode() {
		return code;
	}

	public String getState() {
		return state;
	}

	public String getError() {
		return error;
	}

	public String getErrorDescription() {
		return error_description;
	}


	public AuthCodeResponse setCode(String code) {
		this.code = code;
		return this;
	}

	public AuthCodeResponse setState(String state) {
		this.state = state;
		return this;
	}

	public AuthCodeResponse setError(String error) {
		this.error = error;
		return this;
	}

	public AuthCodeResponse setErrorDescription(String error_description) {
		this.error_description = error_description;
		return this;
	}
}
