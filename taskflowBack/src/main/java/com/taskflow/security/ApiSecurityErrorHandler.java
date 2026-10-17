package com.taskflow.security;

import com.taskflow.error.LoggerResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ApiSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {


    private final LoggerResponse logResponse;

    public ApiSecurityErrorHandler(LoggerResponse logger) {
        this.logResponse = logger;
    }

    /** 401 : jeton absent, invalide ou expiré. */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        logResponse.logger(request, response, HttpStatus.UNAUTHORIZED,
                "Authentification requise : jeton absent, invalide ou expiré");
    }

    /** 403 : jeton valide mais rôle insuffisant. */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        logResponse.logger(request, response, HttpStatus.FORBIDDEN, "Accès refusé : droits insuffisants");
    }






}
