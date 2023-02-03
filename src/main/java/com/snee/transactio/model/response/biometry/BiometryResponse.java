package com.snee.transactio.model.response.biometry;

import com.snee.transactio.model.ResponseModel;
import com.snee.transactio.model.Session;
import com.google.gson.annotations.Expose;

public abstract class BiometryResponse implements ResponseModel {
	@Expose
	protected Session sessionData;

	public abstract <T extends BiometryResponse> T setSessionData(Session sessionData);
}
