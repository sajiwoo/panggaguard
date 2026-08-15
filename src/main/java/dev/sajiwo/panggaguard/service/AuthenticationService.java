package dev.sajiwo.panggaguard.service;

import org.springframework.http.ResponseEntity;

import dev.sajiwo.panggaguard.dto.request.SignInRequest;
import dev.sajiwo.panggaguard.dto.response.DataResponse;
import reactor.core.publisher.Mono;

public interface AuthenticationService {

  Mono<ResponseEntity<DataResponse<?>>> signInMethod();

  Mono<ResponseEntity<DataResponse<?>>> signIn(SignInRequest request);

  Mono<ResponseEntity<DataResponse<?>>> signOut(String domain);

}
