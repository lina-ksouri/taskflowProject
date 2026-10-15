package com.taskflow.api.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(
                        org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("admin")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakRolesConverter())))
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    /**
     * Keycloak place les rôles du realm dans realm_access.roles.
     * Spring Security attend des autorités préfixées par ROLE_ pour hasRole(...).
     */
    @Bean
    Converter<Jwt, ? extends AbstractAuthenticationToken> keycloakRolesConverter() {
        return jwt -> {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            Object rawRoles = realmAccess == null ? null : realmAccess.get("roles");
            Collection<SimpleGrantedAuthority> authorities;
            if (rawRoles instanceof Collection<?> roles) {
                authorities = roles.stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .map(role -> {
                            SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority("ROLE_" + role);
                            return simpleGrantedAuthority;
                        })
                        .collect(Collectors.toList());
            } else {
                authorities = List.of();
            }
            return new JwtAuthenticationToken(jwt, authorities, jwt.getClaimAsString("preferred_username"));
        };
    }
}
