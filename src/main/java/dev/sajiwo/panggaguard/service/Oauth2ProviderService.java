package dev.sajiwo.panggaguard.service;

import org.springframework.security.oauth2.core.user.OAuth2User;

import dev.sajiwo.panggaguard.dto.JsonWebToken;
import dev.sajiwo.panggaguard.dto.request.Oauth2UserRegistration;
import reactor.core.publisher.Mono;

public interface Oauth2ProviderService {

  void registerOauth2User(Oauth2UserRegistration registration);

  Mono<JsonWebToken> oauth2Login(OAuth2User oauth2User);

}
