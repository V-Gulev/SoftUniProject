package com.fittrack.mainapp.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuthenticationFailureHandler.class);

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String username = request.getParameter("username");
        String redirectUrl = "/login-error";

        if (exception instanceof DisabledException) {
            redirectUrl = "/blocked";
            LOGGER.warn("Blocked user login attempt: {}", username);
        } else if (username != null) {
            redirectUrl += "?username=" + java.net.URLEncoder.encode(username, StandardCharsets.UTF_8);
            LOGGER.debug("Failed login attempt for user: {}", username);
        }

        response.sendRedirect(redirectUrl);
    }
}
