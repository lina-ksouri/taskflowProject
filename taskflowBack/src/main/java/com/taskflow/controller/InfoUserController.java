package com.taskflow.controller;

import com.taskflow.dto.MeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/me")
@Tag(name = "Information user connecté")
public class InfoUserController {

    @GetMapping
    @Operation(summary = "Nom, email et rôles de l'utilisateur connecté")
    public MeResponse me(@AuthenticationPrincipal Jwt jwt, Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority().replaceFirst("^ROLE_", ""))
                .sorted()
                .toList();
        String username = jwt.getClaimAsString("preferred_username");
        String name = jwt.getClaimAsString("name");
        return new MeResponse(jwt.getSubject(), username, name != null ? name : username,
                jwt.getClaimAsString("email"), roles);
    }
}
