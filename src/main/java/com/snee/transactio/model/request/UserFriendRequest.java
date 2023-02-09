package com.snee.transactio.model.request;

import com.google.gson.annotations.Expose;
import com.snee.transactio.exceptions.RequestValidationException;
import com.snee.transactio.model.RequestModel;
import com.snee.transactio.model.Session;

import java.util.Arrays;
import java.util.List;

public class UserFriendRequest implements RequestModel {
    public static final String REQUEST_MODE_ADD = "add";

    public static final String REQUEST_MODE_GET = "get";

    public static final String REQUEST_MODE_UPDATE = "update";

    public static final String REQUEST_MODE_DELETE = "delete";

    public static final String REQUEST_STATUS_PENDING = "PENDING";

    public static final String REQUEST_STATUS_ACCEPTED = "ACCEPTED";

    public static final String REQUEST_STATUS_DECLINED = "DECLINED";

    public static final List<String> FRIEND_REQUEST_STATUSES = Arrays.asList(
            REQUEST_STATUS_PENDING,
            REQUEST_STATUS_DECLINED,
            REQUEST_STATUS_ACCEPTED
    );

    private String mode;

    @Expose
    private String userName;

    @Expose
    private String alias;

    @Expose
    private String status;

    @Expose
    private String action;

    @Expose
    private Session sessionData;

    @Override
    public void validate() {
        if (REQUEST_MODE_ADD.equals(mode)) {
            validateAddRequest();
        }

        if (REQUEST_MODE_GET.equals(mode)) {
            validateGetRequest();
        }

        if (REQUEST_MODE_UPDATE.equals(mode)) {
            validateUpdateRequest();
        }

        if (sessionData == null) {
            throw new RequestValidationException(
                    "Session is required for this endpoint"
            );
        }

        sessionData.validate();
    }

    private void validateAddRequest() {
        if (userName == null || userName.isEmpty()) {
            throw new RequestValidationException(
                    "Username is required"
            );
        }

        if (alias != null && alias.isEmpty()) {
            throw new RequestValidationException(
                    "If alias is provided it should not be empty."
            );
        }
    }

    private void validateGetRequest() {
        if (status == null || status.isEmpty()) {
            throw new RequestValidationException(
                    "status is required"
            );
        }

        if (!FRIEND_REQUEST_STATUSES.contains(status)) {
            throw new RequestValidationException(
                    "Unknown status provided"
            );
        }
    }

    private void validateUpdateRequest() {
        if (action == null || action.isEmpty()) {
            throw new RequestValidationException(
                    "action is required"
            );
        }

        if (status == null || status.isEmpty()) {
            throw new RequestValidationException(
                    "status is required"
            );
        }

        if (!FRIEND_REQUEST_STATUSES.contains(status)) {
            throw new RequestValidationException(
                    "Unknown status provided"
            );
        }
    }


    public String getUserName() {
        return userName;
    }

    public Session getSessionData() {
        return sessionData;
    }

    public String getAlias() {
        return alias;
    }

    public String getStatus() {
        return status;
    }

    public String getAction() {
        return action;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
