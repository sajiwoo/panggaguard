package dev.sajiwo.panggaguard.controller;

import java.net.URI;
import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.sajiwo.panggaguard.dto.JsonWebToken;
import dev.sajiwo.panggaguard.dto.request.Oauth2UserRegistration;
import dev.sajiwo.panggaguard.entity.Route;
import dev.sajiwo.panggaguard.repository.RouteRepository;
import dev.sajiwo.panggaguard.service.Oauth2ProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RequiredArgsConstructor
@RestController
public class Oauth2LandingController {

  private final Oauth2ProviderService oauth2ProviderService;
  private final RouteRepository routeRepository;

  @GetMapping
  public Mono<ResponseEntity<Void>> oauth2LandingHandler(
      @AuthenticationPrincipal OAuth2User user,
      @CookieValue(name = "x_target_domain") String xTargetDomain,
      @CookieValue(name = "x_role") String role) {

    Oauth2UserRegistration reg = new Oauth2UserRegistration();
    reg.setUser(user);
    reg.setDomain(xTargetDomain);
    reg.setRole(role);

    Mono<JsonWebToken> jwtToken = Mono
        .fromRunnable(() -> oauth2ProviderService.registerOauth2User(reg))
        .subscribeOn(Schedulers.boundedElastic())
        .then(oauth2ProviderService.oauth2Login(xTargetDomain, user));

    Mono<String> route = Mono.fromCallable(() -> routeRepository.findUserAuthUrlRedirection(xTargetDomain, role))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(Mono::justOrEmpty)
        .switchIfEmpty(Mono.error(new ErrorResponseException(HttpStatus.METHOD_NOT_ALLOWED)))
        .map(Route::getUri);

    return Mono.zip(jwtToken, route).map(tuple -> {
      JsonWebToken jwt = tuple.getT1();

      ResponseCookie accessToken = ResponseCookie
          .from("accessToken", jwt.getBearerToken())
          .httpOnly(true)
          .secure(false)
          .path("/")
          .maxAge(Duration.ofHours(24))
          .build();

      return ResponseEntity.status(HttpStatus.FOUND)
          .location(URI.create(tuple.getT2()))
          .header(HttpHeaders.SET_COOKIE, accessToken.toString())
          .build();
    });
  }
}
