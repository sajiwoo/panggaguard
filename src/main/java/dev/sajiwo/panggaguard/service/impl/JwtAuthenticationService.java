package dev.sajiwo.panggaguard.service.impl;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.stereotype.Service;

import dev.sajiwo.panggaguard.dto.JsonWebToken;
import dev.sajiwo.panggaguard.dto.request.SignInRequest;
import dev.sajiwo.panggaguard.dto.response.DataResponse;
import dev.sajiwo.panggaguard.dto.response.SignInMethodResponse;
import dev.sajiwo.panggaguard.entity.User;
import dev.sajiwo.panggaguard.entity.UserActivity;
import dev.sajiwo.panggaguard.exception.ErrorResponseException;
import dev.sajiwo.panggaguard.repository.UserActivityRepository;
import dev.sajiwo.panggaguard.repository.UserRepository;
import dev.sajiwo.panggaguard.service.AuthenticationService;
import dev.sajiwo.panggaguard.service.JwtService;
import dev.sajiwo.panggaguard.utilities.DataResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RequiredArgsConstructor
@Service
public class JwtAuthenticationService implements AuthenticationService {

  @Value("${add-config.application.domain}")
  private String APP_DOMAIN;

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final UserActivityRepository activityRepository;
  private final ReactiveClientRegistrationRepository clientRegistrationRepository;

  @SuppressWarnings("unchecked")
  @Override
  public Mono<ResponseEntity<DataResponse<?>>> signInMethod() {
    List<SignInMethodResponse> providers = new ArrayList<>();
    if (clientRegistrationRepository instanceof Iterable) {
      for (ClientRegistration registration : (Iterable<ClientRegistration>) clientRegistrationRepository) {
        SignInMethodResponse provider = new SignInMethodResponse(registration.getClientName(), "",
            APP_DOMAIN + "/auth/sign-in/" + registration.getClientName().toLowerCase());
        providers.add(provider);
      }
    }
    return Mono.just(providers)
        .map(DataResponses::ok)
        .map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<DataResponse<?>>> signIn(SignInRequest request) {
    return Mono.fromCallable(() -> userRepository.findByDomainAndEmail(request.getDomain(), request.getUsername()))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(Mono::justOrEmpty)
        .switchIfEmpty(Mono
            .defer(() -> Mono.error(new ErrorResponseException(HttpStatus.UNAUTHORIZED, "Username password salah"))))
        .flatMap(userExists -> {
          boolean isMatch = passwordEncoder.matches(request.getPassword(), userExists.getPassword());
          if (!isMatch) {
            return Mono.error(new ErrorResponseException(HttpStatus.UNAUTHORIZED, "Username password salah"));
          }

          JsonWebToken jwt = jwtService.generateToken(userExists);

          UserActivity activity = new UserActivity();
          activity.setId(jwt.getJti());
          activity.setUserId(userExists.getId().toString());
          activity.setType("signin");
          activityRepository.save(activity);

          return Mono.just(jwt);
        })
        .map(jwt -> {
          ResponseCookie accessToken = ResponseCookie
              .from("accessToken", jwt.getBearerToken())
              .httpOnly(true)
              .secure(false)
              .path("/")
              .maxAge(Duration.ofHours(5))
              .build();

          return ResponseEntity.ok()
              .header(HttpHeaders.SET_COOKIE, accessToken.toString())
              .body(DataResponses.ok(new Object()));
        });
  }

  @Override
  public Mono<ResponseEntity<DataResponse<?>>> signOut(String domain) {
    return Mono.fromCallable(() -> SecurityContextHolder.getContext().getAuthentication())
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(Mono::justOrEmpty)
        .switchIfEmpty(
            Mono.defer(() -> Mono.error(new ErrorResponseException(HttpStatus.UNAUTHORIZED, "User not authenticated"))))
        .flatMap(authentication -> {
          Optional<User> user = userRepository.findByDomainAndEmail(domain, authentication.getName());
          if (user.isEmpty()) {
            return Mono.error(new ErrorResponseException(HttpStatus.UNAUTHORIZED, "User not found"));
          }

          return Mono.just(new Object());
        })
        .map(data -> new DataResponse<>(data))
        .map(ResponseEntity::ok);
  }
}
