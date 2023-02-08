package com.snee.transactio.controller;

import com.snee.transactio.model.Session;
import com.snee.transactio.model.request.UserFriendRequest;
import com.snee.transactio.model.response.UserFriendsResponse;
import com.snee.transactio.service.AuthMgmtService;
import com.snee.transactio.service.UserHandlerService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        value = "${api.prefix}/user/friends",
        consumes = {MediaType.APPLICATION_JSON_VALUE},
        produces = {MediaType.APPLICATION_JSON_VALUE}
)
public class UserFriendsRestController {
    private final UserHandlerService mUserHandlerService;
    private final AuthMgmtService mAuthMgmtService;

    public UserFriendsRestController(UserHandlerService userHandlerService, AuthMgmtService authMgmtService) {
        mUserHandlerService = userHandlerService;
        mAuthMgmtService = authMgmtService;
    }

    @PostMapping(path = "/add")
    public ResponseEntity<UserFriendsResponse> addFriend(@RequestBody UserFriendRequest request) {
        request.setMode(UserFriendRequest.REQUEST_MODE_ADD);
        request.validate();

        Session sessionData = mAuthMgmtService.validateSession(request.getSessionData());
        UserFriendsResponse response = mUserHandlerService.sendFriendRequest(sessionData, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/get")
    public ResponseEntity<UserFriendsResponse> getLoggedUserFriends(@RequestBody UserFriendRequest request) {
        request.setMode(UserFriendRequest.REQUEST_MODE_GET);
        request.validate();

        Session sessionData = mAuthMgmtService.validateSession(request.getSessionData());
        UserFriendsResponse response = mUserHandlerService.getFriendsList(sessionData, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/update")
    public ResponseEntity<UserFriendsResponse> updateFriend(@RequestBody UserFriendRequest request) {
        request.setMode(UserFriendRequest.REQUEST_MODE_UPDATE);
        request.validate();

        Session sessionData = mAuthMgmtService.validateSession(request.getSessionData());
        UserFriendsResponse response = mUserHandlerService.updateUsersFriend(sessionData, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/delete")
    public ResponseEntity<UserFriendsResponse> deleteFriend(@RequestBody UserFriendRequest request) {
        request.setMode(UserFriendRequest.REQUEST_MODE_DELETE);
        request.validate();

        Session sessionData = mAuthMgmtService.validateSession(request.getSessionData());
        UserFriendsResponse response = mUserHandlerService.deleteUsersFriend(sessionData, request);
        return ResponseEntity.ok(response);
    }
}
