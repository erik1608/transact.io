package com.snee.transactio.controller;

import com.snee.transactio.db.entities.user.User;
import com.snee.transactio.model.Session;
import com.snee.transactio.model.request.UserInfoRequest;
import com.snee.transactio.model.response.UserInfoResponse;
import com.snee.transactio.service.AuthMgmtService;
import com.snee.transactio.service.UserHandlerService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/user")
public class UserInfoRestController {

    private final UserHandlerService mUserHandlerService;

    private final AuthMgmtService mAuthService;

    public UserInfoRestController(
            UserHandlerService userHandlerService,
            AuthMgmtService authService
    ) {
        mAuthService = authService;
        mUserHandlerService = userHandlerService;
    }

    @PostMapping(
            path = "/info",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserInfoResponse> getUserInfo(
            @RequestBody UserInfoRequest request
    ) {
        request.validate();
        UserInfoResponse response = new UserInfoResponse();
        {
            Session session = mAuthService.validateSession(
                    request.getSessionData()
            );
            User userInfoResponse = new User();
            User userInfo = mUserHandlerService.getUser(
                    session.getSubject()
            );
            userInfoResponse.copyFrom(userInfo);
            response.setUserInfo(userInfoResponse);
            response.setSessionData(session);
        }

        return ResponseEntity.ok(response);
    }
}
