package dev.sajiwo.panggaguard.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.sajiwo.panggaguard.dto.request.ForgotPassword;
import dev.sajiwo.panggaguard.dto.request.ResetPasswordRequest;
import dev.sajiwo.panggaguard.dto.request.SignInRequest;
import dev.sajiwo.panggaguard.dto.request.SignUpRequest;
import dev.sajiwo.panggaguard.dto.response.DataResponse;
import dev.sajiwo.panggaguard.service.AuthenticationService;
import dev.sajiwo.panggaguard.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/auth")
public class AuthController {

  @Value("${add-config.application.domain}")
  private String AppDomain;

  private final AuthenticationService authenticationService;
  private final UserService userService;

  @PostMapping(path = "/sign-up")
  public Mono<ResponseEntity<DataResponse<?>>> signUp(
      @RequestHeader("x-target-domain") String domain,
      @Valid @RequestBody SignUpRequest request) {
    request.setDomain(domain);
    return userService.signUp(request);
  }

  @GetMapping(path = "/oauth2/provider")
  public Mono<ResponseEntity<DataResponse<?>>> signInMethod(
      @RequestHeader("x-target-domain") String domain) {
    return authenticationService.signInMethod();
  }

  @PostMapping(path = "/sign-in")
  public Mono<ResponseEntity<DataResponse<?>>> signIn(
      @RequestHeader("x-target-domain") String domain,
      @Valid @RequestBody SignInRequest request) {
    request.setDomain(domain);
    return authenticationService.signIn(request);
  }

  @GetMapping(path = "/sign-in/{provider}")
  public Mono<ResponseEntity<Void>> signInWithOauth2Provider(
      @PathVariable String provider,
      @RequestParam("x_target_domain") String xTargetDomain,
      @RequestParam("role") String role,
      @CookieValue(name = "accessToken", required = false) String accessToken) {
    return userService.signInWithOauth2Provider(provider, xTargetDomain, role, accessToken);
  }

  @PostMapping(path = "/forgot-password")
  public Mono<ResponseEntity<DataResponse<?>>> forgotPasswordRequest(
      @RequestHeader("x-target-domain") String domain,
      @RequestBody ForgotPassword request) {
    request.setDomain(domain);
    return userService.forgotPassword(request);
  }

  @PostMapping(path = "/reset-password")
  public Mono<ResponseEntity<DataResponse<?>>> resetPassword(
      @RequestHeader("x-target-domain") String domain,
      @RequestBody ResetPasswordRequest request) {
    request.setDomain(domain);
    return userService.resetPassword(request);
  }

}
