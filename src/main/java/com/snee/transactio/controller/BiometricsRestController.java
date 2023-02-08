package com.snee.transactio.controller;

import com.snee.transactio.model.request.BiometricRequest;
import com.snee.transactio.model.response.biometry.BiometryResponse;
import com.snee.transactio.service.AuthMgmtService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/biometry")
public class BiometricsRestController {

    private final AuthMgmtService mAuthSvc;

    public BiometricsRestController(AuthMgmtService authMgmtService) {
        mAuthSvc = authMgmtService;
    }

    @PostMapping(
            path = "/reg",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<BiometryResponse> enrollBiometrics(
            @RequestBody BiometricRequest biometricRequest
    ) {
        biometricRequest.validate();
        BiometryResponse response = mAuthSvc.processBiometryOp(
                biometricRequest,
                AuthMgmtService.BiometryOp.REG
        );

        response.setSessionData(
                mAuthSvc.validateSession(biometricRequest.getSessionData())
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping(
            path = "/auth",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<BiometryResponse> authorizeWithBiometrics(
            @RequestBody BiometricRequest biometricRequest
    ) {
        biometricRequest.validate();
        BiometryResponse response = mAuthSvc.processBiometryOp(
                biometricRequest,
                AuthMgmtService.BiometryOp.AUTH
        );

        return ResponseEntity.ok(response);
    }
}
