package dev.sajiwo.panggaguard.service;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;

import dev.sajiwo.panggaguard.dto.response.DataResponse;
import reactor.core.publisher.Mono;

public interface Oauth2ProviderService {

  void registerOauth2User(OAuth2User oauth2User);

  Mono<ResponseEntity<DataResponse<?>>> oauth2Login(OAuth2User oauth2User);

}
