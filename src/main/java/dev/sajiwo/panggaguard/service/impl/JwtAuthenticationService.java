package dev.sajiwo.panggaguard.service.impl;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dev.sajiwo.panggaguard.dto.JsonWebToken;
import dev.sajiwo.panggaguard.dto.request.SignInRequest;
import dev.sajiwo.panggaguard.dto.response.DataResponse;
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

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final UserActivityRepository activityRepository;

  @Override
  public Mono<ResponseEntity<DataResponse<?>>> signIn(SignInRequest request) {
    return Mono.fromCallable(() -> userRepository.findByEmail(request.username()))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(Mono::justOrEmpty)
        .switchIfEmpty(Mono
            .defer(() -> Mono.error(new ErrorResponseException(HttpStatus.UNAUTHORIZED, "Username password salah"))))
        .flatMap(userExists -> {
          boolean isMatch = passwordEncoder.matches(request.password(), userExists.getPassword());
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
        .map(DataResponses::ok)
        .map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<DataResponse<?>>> signOut() {
    return Mono.fromCallable(() -> SecurityContextHolder.getContext().getAuthentication())
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(Mono::justOrEmpty)
        .switchIfEmpty(
            Mono.defer(() -> Mono.error(new ErrorResponseException(HttpStatus.UNAUTHORIZED, "User not authenticated"))))
        .flatMap(authentication -> {
          Optional<User> user = userRepository.findByEmail(authentication.getName());
          if (user.isEmpty()) {
            return Mono.error(new ErrorResponseException(HttpStatus.UNAUTHORIZED, "User not found"));
          }

          return Mono.just(new Object());
        })
        .map(data -> new DataResponse<>(data))
        .map(ResponseEntity::ok);
  }
}
