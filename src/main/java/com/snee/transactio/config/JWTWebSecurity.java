package com.snee.transactio.config;

import com.snee.transactio.filter.JWTPreAuthenticationFilter;
import com.snee.transactio.service.AuthMgmtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

@Configuration
@EnableWebSecurity
public class JWTWebSecurity {

    private final AuthMgmtService mAuthService;

    public JWTWebSecurity(AuthMgmtService authMgmtService) {
        mAuthService = authMgmtService;
    }

    /**
     * A bean controlling authorization required paths.
     */
    @Bean
    SecurityFilterChain jwtSecurityChain(HttpSecurity http) throws Exception {
        return http.httpBasic().disable()
                .csrf().disable()
                .cors().disable()
                .antMatcher("/**")
                .addFilterBefore(new JWTPreAuthenticationFilter(mAuthService), AbstractPreAuthenticatedProcessingFilter.class)
                .build();
    }
}
