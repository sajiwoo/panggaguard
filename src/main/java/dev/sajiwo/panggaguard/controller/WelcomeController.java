package dev.sajiwo.panggaguard.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.sajiwo.panggaguard.dto.response.DataResponse;
import dev.sajiwo.panggaguard.service.Oauth2ProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RequiredArgsConstructor
@RestController
public class WelcomeController {

  private final Oauth2ProviderService oauth2ProviderService;

  /**
   * When the user has Authenticated by Auth Provider e.g Google
   * It will redirect to the 'https://mydomain.com', the one that we have to
   * configure in google console dashboard
   * <p>
   * Google only take the link redirection without additional slash after the
   * domain
   * This controller comes to address this, and redirect to where ever we need to
   */
  @GetMapping
  public Mono<ResponseEntity<?>> redirectFirstTime(@AuthenticationPrincipal OAuth2User user) {
    return Mono.just(ResponseEntity.status(HttpStatus.FOUND).header("x-target-domain", "nusasilat")
        .header("Location", "http://localhost:8080/oauth2/token")
        .build());
  }

  @GetMapping(path = "/oauth2/token")
  public Mono<ResponseEntity<DataResponse<?>>> welcome(@AuthenticationPrincipal OAuth2User user) {
    return Mono.fromRunnable(() -> oauth2ProviderService.registerOauth2User(user))
        .subscribeOn(Schedulers.boundedElastic())
        .then(oauth2ProviderService.oauth2Login(user));
  }

}
