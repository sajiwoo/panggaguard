package dev.sajiwo.panggaguard.service;

import org.springframework.http.ResponseEntity;

import dev.sajiwo.panggaguard.dto.request.SignUpRequest;
import dev.sajiwo.panggaguard.dto.response.DataResponse;
import reactor.core.publisher.Mono;

public interface UserService {

  Mono<ResponseEntity<DataResponse<?>>> signUp(SignUpRequest request);

}
