package com.snee.transactio.controller;

import com.snee.transactio.exceptions.RequestValidationException;
import com.snee.transactio.model.request.oauth2.AuthCodeRequest;
import com.snee.transactio.model.request.oauth2.TokenRequest;
import com.snee.transactio.model.response.oauth2.AuthCodeResponse;
import com.snee.transactio.model.response.oauth2.TokenResponse;
import com.snee.transactio.oauth2.OAuth2StdErrorResponse;
import com.snee.transactio.service.AuthMgmtService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/o2/auth")
public class AuthorizationRestController {
    private final static Logger LOG = LogManager.getLogger(AuthorizationRestController.class);

    private final static String OAUTH_KEY_GRANT_TYPE = "grant_type";

    private final static String OAUTH_KEY_REDIRECT_URI = "redirect_uri";

    private final static String OAUTH_KEY_CODE = "code";

    private final AuthMgmtService mAuthService;

    public AuthorizationRestController(
            AuthMgmtService authMgmtService
    ) {
        mAuthService = authMgmtService;
    }

    @PostMapping(
            path = "/code",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody ResponseEntity<AuthCodeResponse> getAuthToken(
            @RequestBody AuthCodeRequest request
    ) {
        request.validate();
        return ResponseEntity.ok(
                mAuthService.generateOAuthAuthCode(request)
        );
    }

    @CrossOrigin(
            originPatterns = {"*"},
            allowCredentials = "true",
            methods = {RequestMethod.POST, RequestMethod.OPTIONS}
    )
    @RequestMapping(
            method = RequestMethod.POST,
            path = "/token",
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    public ResponseEntity<TokenResponse> getAccessToken(
            @RequestHeader(name = "Authorization") String clientAuth,
            HttpServletRequest httpServletRequest
    ) {
        // Since the Spring web has a bug with form submission (I.e. x-www-form-urlencoded requests).
        // This is a workaround for marshalling the request into the respective POJO
        try (ServletInputStream inStream = httpServletRequest.getInputStream()) {
            byte[] httpInData = new byte[httpServletRequest.getContentLength()];
            StringBuilder httpURLEncodedDataBuilder = new StringBuilder();
            int retVal;
            while ((retVal = inStream.read(httpInData)) != -1) {
                for (int i = 0; i < retVal; i++) {
                    httpURLEncodedDataBuilder.append((char) httpInData[i]);
                }
            }

            String formData = httpURLEncodedDataBuilder.toString();
            String[] formDataParts = formData.split("&");
            Map<String, String> parameters = new HashMap<>();
            for (String formDataPart : formDataParts) {
                String[] parts = formDataPart.split("=");
                parameters.put(parts[0], URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
            }

            TokenRequest request = new TokenRequest();
            {
                request.setGrantType(parameters.get(OAUTH_KEY_GRANT_TYPE));
                request.setCode(parameters.get(OAUTH_KEY_CODE));
                request.setRedirectUri(parameters.get(OAUTH_KEY_REDIRECT_URI));
            }

            LOG.info("Client[" + clientAuth + "] requested access token: " + request);
            request.validate();
            String[] credential = clientAuth.split("Basic ");
            if (credential.length < 1) {
                throw OAuth2StdErrorResponse.UNAUTHORIZED_CLIENT.getException();
            }
            String clientCombinedIdSecret = new String(
                    Base64.getDecoder().decode(credential[1]),
                    StandardCharsets.UTF_8
            );

            TokenResponse response = mAuthService.generateOAuthAccessToken(
                    request,
                    clientCombinedIdSecret
            );
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            throw new RequestValidationException(e.getMessage(), e);
        }
    }

    @GetMapping
    public ModelAndView modelAndView() {
        return new ModelAndView("oauth_login_web");
    }
}
