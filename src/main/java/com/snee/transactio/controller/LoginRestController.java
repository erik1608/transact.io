package com.snee.transactio.controller;

import com.snee.transactio.db.entities.user.User;
import com.snee.transactio.exceptions.RequestValidationException;
import com.snee.transactio.model.Session;
import com.snee.transactio.model.request.LoginRequest;
import com.snee.transactio.model.response.LoginResponse;
import com.snee.transactio.service.AuthMgmtService;
import com.snee.transactio.service.UserHandlerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/login")
public class LoginRestController {

	private final AuthMgmtService mAuthService;
	private final UserHandlerService mUsersService;

	public LoginRestController(
			UserHandlerService userHandlerService,
			AuthMgmtService authMgmtService
	) {
		mUsersService = userHandlerService;
		mAuthService = authMgmtService;
	}

	@PostMapping(
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE
	)
	public ResponseEntity<LoginResponse> loginUser(
			@RequestBody LoginRequest request
	) {
		request.validate();

		LoginResponse response = new LoginResponse();
		response.setStatus(HttpStatus.OK);
		if ("create".equals(request.getMode())) {
			User user = mUsersService.getUser(
					request.getUsernameOrEmail()
			);

			if (user == null) {
				throw new RequestValidationException(
						"The user was not found"
				);
			}

			if (request.getDeviceInfo() != null) {
				mUsersService.updateDeviceAndGet(
						user, request.getDeviceInfo()
				);
			}

			if (mUsersService.isPasswordCorrect(user, request.getPassword())) {
				Session session = mAuthService.createSession(user.getUsername());
				response.setSessionData(session);
			} else {
				response.setStatus(HttpStatus.UNAUTHORIZED);
			}
		} else if ("renew".equals(request.getMode())) {
			Session session = mAuthService.validateSession(request.getSessionData());
			response.setSessionData(session);
		} else {
			throw new RequestValidationException(
					"Unexpected mode"
			);
		}

		return ResponseEntity.status(response.getStatus()).body(response);
	}
}
