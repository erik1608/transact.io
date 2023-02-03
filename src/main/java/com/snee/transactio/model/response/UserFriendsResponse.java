package com.snee.transactio.model.response;

import com.snee.transactio.model.ResponseModel;
import com.google.gson.annotations.Expose;

import java.util.List;

public class UserFriendsResponse implements ResponseModel {
	@Expose
	private String status;

	@Expose
	private List<Friend> friends;

	public void setStatus(String status) {
		this.status = status;
	}

	public void setFriends(List<Friend> friends) {
		this.friends = friends;
	}

	public String getStatus() {
		return status;
	}

	public List<Friend> getFriends() {
		return friends;
	}

	public static final class Friend {
		@Expose
		private String alias;

		@Expose
		private String username;

		public void setAlias(String alias) {
			this.alias = alias;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getAlias() {
			return alias;
		}

		public String getUsername() {
			return username;
		}
	}
}
