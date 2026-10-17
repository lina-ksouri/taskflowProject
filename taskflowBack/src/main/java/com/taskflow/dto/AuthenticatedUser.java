package com.taskflow.dto;

import org.springframework.security.oauth2.jwt.Jwt;

public record AuthenticatedUser(String id, String username) {

    public static AuthenticatedUser from(Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        return new AuthenticatedUser(jwt.getSubject(), username != null ? username : jwt.getSubject());
    }
}
