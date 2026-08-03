package dev.sajiwo.panggaguard.service.impl;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import dev.sajiwo.panggaguard.dto.request.ForgotPassword;
import dev.sajiwo.panggaguard.dto.request.ResetPasswordRequest;
import dev.sajiwo.panggaguard.dto.request.SignUpRequest;
import dev.sajiwo.panggaguard.dto.response.DataResponse;
import dev.sajiwo.panggaguard.entity.Route;
import dev.sajiwo.panggaguard.entity.User;
import dev.sajiwo.panggaguard.entity.UserActivity;
import dev.sajiwo.panggaguard.enumeration.Mailing;
import dev.sajiwo.panggaguard.exception.ErrorResponseException;
import dev.sajiwo.panggaguard.mapper.UserMapper;
import dev.sajiwo.panggaguard.repository.RouteRepository;
import dev.sajiwo.panggaguard.repository.UserActivityRepository;
import dev.sajiwo.panggaguard.repository.UserRepository;
import dev.sajiwo.panggaguard.service.EmailService;
import dev.sajiwo.panggaguard.service.JwtService;
import dev.sajiwo.panggaguard.service.OtpService;
import dev.sajiwo.panggaguard.service.UserService;
import dev.sajiwo.panggaguard.utilities.DataResponses;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserActivityRepository activityRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final EmailService emailService;
  private final OtpService otpService;
  private final JwtService jwtService;
  private final RouteRepository routeRepository;

  @Override
  public Mono<ResponseEntity<DataResponse<?>>> signUp(SignUpRequest request) {
    return Mono.fromCallable(() -> userRepository.findByEmail(request.email()))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(Mono::justOrEmpty)
        .flatMap(user -> Mono.<User>error(new ErrorResponseException(HttpStatus.CONFLICT, "Email sudah terdaftar")))
        .switchIfEmpty(Mono.defer(() -> Mono.fromCallable(() -> {
          User user = userMapper.map(request);
          user.setPassword(passwordEncoder.encode(request.password()));
          return userRepository.save(user);
        }).subscribeOn(Schedulers.boundedElastic())))
        .map(userMapper::map)
        .map(DataResponses::ok)
        .map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<DataResponse<?>>> forgotPassword(ForgotPassword request) {
    return Mono.fromCallable(() -> userRepository.findByEmail(request.email()))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(Mono::justOrEmpty)
        .flatMap(user -> {
          UserActivity newActivity = new UserActivity();
          newActivity.setId(UUID.randomUUID().toString());
          newActivity.setUserId(user.getId().toString());
          newActivity.setType("init forgot password");
          activityRepository.save(newActivity);

          return otpService.generateOtp(user)
              .doOnNext(token -> {
                Map<String, Object> params = new java.util.HashMap<>();
                params.put("OTP", token);
                params.put("EMAIL", user.getEmail());

                emailService.sendMail(Mailing.FORGOT_PASSWORD, user.getEmail(), params)
                    .subscribe(null, error -> {
                    });
              });
        })
        .thenReturn(DataResponses.ok("Link reset password sudah di kirim ke email anda "))
        .map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<DataResponse<?>>> resetPassword(ResetPasswordRequest request) {
    return otpService.verifyOtp(request.token(), request.email())
        .flatMap(isValid -> {
          if (!isValid) {
            return Mono.error(new ErrorResponseException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP token"));
          }
          return Mono.fromCallable(() -> userRepository.findByEmail(request.email()))
              .subscribeOn(Schedulers.boundedElastic())
              .flatMap(Mono::justOrEmpty)
              .switchIfEmpty(Mono.error(new ErrorResponseException(HttpStatus.NOT_FOUND, "User not found")))
              .flatMap(user -> {
                user.setPassword(passwordEncoder.encode(request.password()));
                userRepository.save(user);
                return Mono.just(DataResponses.ok("Password has been reset successfully"));
              });
        })
        .map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<Void>> signInWithOauth2Provider(String provider, String domain, String role,
      String token) {

    if (StringUtils.hasText(token)) {
      try {
        jwtService.decode(token);
        Optional<Route> home = routeRepository.findUserAuthUrlRedirection(domain, role);
        if (home.isEmpty()) {
          return Mono.just(ResponseEntity.status(HttpStatus.FOUND).location(URI.create("https://google.com")).build());
        }

        return Mono.just(ResponseEntity.status(HttpStatus.FOUND).location(URI.create(home.get().getUri())).build());

      } catch (Exception e) {
      }
    }

    ResponseCookie roleCookie = ResponseCookie
        .from("x_role", role)
        .path("/")
        .maxAge(Duration.ofMinutes(5))
        .build();

    ResponseCookie routeCookie = ResponseCookie
        .from("x_target_domain", domain)
        .path("/")
        .maxAge(Duration.ofMinutes(5))
        .build();

    return Mono.just(ResponseEntity.status(HttpStatus.FOUND)
        .location(URI.create("/oauth2/authorization/" + provider))
        .header(HttpHeaders.SET_COOKIE, roleCookie.toString())
        .header(HttpHeaders.SET_COOKIE, routeCookie.toString())
        .build());
  }

}
