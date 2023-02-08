package com.snee.transactio.filter;

import com.amazonaws.HttpMethod;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.snee.transactio.exceptions.BadSessionException;
import com.snee.transactio.exceptions.RequestValidationException;
import com.snee.transactio.model.Session;
import com.snee.transactio.service.AuthMgmtService;
import org.apache.http.entity.ContentType;
import org.springframework.lang.NonNull;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;

public class JWTPreAuthenticationFilter extends OncePerRequestFilter {
    private static final String SESSION_DATA_KEY = "sessionData";

    private static final String REQ_ATTR_JWT_SUB = "com.etd.transactio.filter.subject";

    private static final String USER_INFO_PATH =
            "/api/*/user/**";

    private static final String BIOMETRY_REG_PATH =
            "/api/*/biometry/reg**";

    private static final String USER_ASSOCIATION_PATH =
            "/api/*/user/association**";

    private static final RequestMatcher USER_INFO_AUTHORIZED_REQUEST_GET_MATCHER =
            new AntPathRequestMatcher(
                    USER_INFO_PATH, HttpMethod.GET.name()
            );

    private static final RequestMatcher USER_INFO_AUTHORIZED_REQUEST_POST_MATCHER =
            new AntPathRequestMatcher(
                    USER_INFO_PATH, HttpMethod.POST.name()
            );

    private static final RequestMatcher BIOMETRY_AUTHORIZED_REQUEST_POST_MATCHER =
            new AntPathRequestMatcher(
                    BIOMETRY_REG_PATH, HttpMethod.POST.name()
            );

    private static final RequestMatcher USER_ASSOCIATION_REQUEST_POST_MATCHER =
            new AntPathRequestMatcher(
                    USER_ASSOCIATION_PATH, HttpMethod.POST.name()
            );

    private static final RequestMatcher USER_ASSOCIATION_REQUEST_GET_MATCHER =
            new AntPathRequestMatcher(
                    USER_ASSOCIATION_PATH, HttpMethod.GET.name()
            );

    private static final RequestMatcher AUTH_REQUEST_MATCHER = new OrRequestMatcher(
            USER_INFO_AUTHORIZED_REQUEST_GET_MATCHER,
            USER_INFO_AUTHORIZED_REQUEST_POST_MATCHER,
            BIOMETRY_AUTHORIZED_REQUEST_POST_MATCHER,
            USER_ASSOCIATION_REQUEST_POST_MATCHER,
            USER_ASSOCIATION_REQUEST_GET_MATCHER
    );

    private final AuthMgmtService mAuthMgmtService;

    public JWTPreAuthenticationFilter(final AuthMgmtService authMgmtService) {
        mAuthMgmtService = authMgmtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        CachedRequest requestWrapper = new CachedRequest(request);
        if (!AUTH_REQUEST_MATCHER.matches(request)) {
            filterChain.doFilter(requestWrapper, response);
            return;
        }

        JsonObject requestJson = new Gson().fromJson(
                new InputStreamReader(requestWrapper.getInputStream()),
                JsonObject.class
        );

        if (requestJson == null || !requestJson.has(SESSION_DATA_KEY)) {
            unauthorized(response);
            return;
        }

        try {
            Session sessionData = new Gson().fromJson(
                    requestJson.get(SESSION_DATA_KEY),
                    Session.class
            );

            sessionData.validate();
            Session newSession = mAuthMgmtService.validateSession(sessionData);
            requestWrapper.setAttribute(REQ_ATTR_JWT_SUB, newSession.getSubject());
            filterChain.doFilter(requestWrapper, response);
        } catch (BadSessionException | RequestValidationException e) {
            unauthorized(response);
        }
    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        JsonObject responseJson = new JsonObject();
        int status = HttpServletResponse.SC_UNAUTHORIZED;
        responseJson.addProperty(
                "message", "A session is required for this endpoint"
        );
        responseJson.addProperty(
                "status", status
        );
        writeResponse(response, status, responseJson.toString());
    }

    private void writeResponse(
            HttpServletResponse response,
            int status,
            String json
    ) throws IOException {
        response.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        response.setStatus(status);
        response.setContentLength(json.length());
        response.getWriter().write(json);
        response.getWriter().flush();
    }
}
