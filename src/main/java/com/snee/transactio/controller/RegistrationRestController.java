package com.snee.transactio.controller;

import com.snee.transactio.db.entities.user.User;
import com.snee.transactio.model.request.RegistrationRequest;
import com.snee.transactio.model.response.RegistrationResponse;
import com.snee.transactio.service.AuthMgmtService;
import com.snee.transactio.service.UserHandlerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/reg")
public class RegistrationRestController {
    private static final Logger LOG = LogManager.getLogger(
            RegistrationRestController.class
    );

    private final UserHandlerService mUserHandlerService;
    private final AuthMgmtService mAuthMgmtService;


    public RegistrationRestController(
            UserHandlerService userHandlerService,
            AuthMgmtService authMgmtService
    ) {
        mUserHandlerService = userHandlerService;
        mAuthMgmtService = authMgmtService;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<RegistrationResponse> registerUser(
            @RequestBody RegistrationRequest regRequest
    ) {
        LOG.info(regRequest);
        regRequest.validate();
        RegistrationResponse regResponse = new RegistrationResponse();
        User createdUser = mUserHandlerService.registerUser(
                regRequest,
                regResponse
        );

        regResponse.setSessionData(
                mAuthMgmtService.createSession(createdUser.getUsername())
        );
        return ResponseEntity.ok(regResponse);
    }
}
