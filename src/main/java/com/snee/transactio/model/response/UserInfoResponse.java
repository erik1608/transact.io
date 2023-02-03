package com.snee.transactio.model.response;

import com.snee.transactio.db.entities.user.User;
import com.snee.transactio.model.ResponseModel;
import com.snee.transactio.model.Session;
import com.google.gson.annotations.Expose;

public class UserInfoResponse implements ResponseModel {
	@Expose
	private Session sessionData;

	@Expose
	private User userInfo;

	public Session getSessionData() {
		return sessionData;
	}

	public void setSessionData(Session sessionData) {
		this.sessionData = sessionData;
	}

	public User getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(User userInfo) {
		this.userInfo = userInfo;
	}
}
