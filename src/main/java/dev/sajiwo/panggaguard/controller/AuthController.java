package dev.sajiwo.panggaguard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

  private final AuthenticationService authenticationService;
  private final UserService userService;

  @GetMapping(path = "/sign-in/method")
  public Mono<ResponseEntity<DataResponse<?>>> signInMethod() {
    return Mono.just(ResponseEntity.ok(new DataResponse<>("/google")));
  }

  @PostMapping(path = "/sign-in")
  public Mono<ResponseEntity<DataResponse<?>>> signIn(@Valid @RequestBody SignInRequest request) {
    return authenticationService.signIn(request);
  }

  @PostMapping(path = "/sign-up")
  public Mono<ResponseEntity<DataResponse<?>>> signUp(@Valid @RequestBody SignUpRequest request) {
    return userService.signUp(request);
  }

  @PostMapping(path = "/sing-out")
  public Mono<ResponseEntity<DataResponse<?>>> signOut(Authentication authentication) {
    return authenticationService.signOut();
  }

}
