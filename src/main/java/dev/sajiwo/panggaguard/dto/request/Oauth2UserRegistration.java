package dev.sajiwo.panggaguard.dto.request;

import org.springframework.security.oauth2.core.user.OAuth2User;

public record Oauth2UserRegistration(OAuth2User user, String role) {
}
