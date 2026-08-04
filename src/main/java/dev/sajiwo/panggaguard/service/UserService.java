package dev.sajiwo.panggaguard.service;

import org.springframework.http.ResponseEntity;

import dev.sajiwo.panggaguard.dto.request.ForgotPassword;
import dev.sajiwo.panggaguard.dto.request.ResetPasswordRequest;
import dev.sajiwo.panggaguard.dto.request.SignUpRequest;
import dev.sajiwo.panggaguard.dto.response.DataResponse;
import dev.sajiwo.panggaguard.dto.response.UserProfileResponse;
import reactor.core.publisher.Mono;

public interface UserService {

  Mono<ResponseEntity<DataResponse<?>>> signUp(SignUpRequest request);

  Mono<ResponseEntity<DataResponse<?>>> forgotPassword(ForgotPassword request);

  Mono<ResponseEntity<DataResponse<?>>> resetPassword(ResetPasswordRequest request);

  Mono<ResponseEntity<Void>> signInWithOauth2Provider(String provider, String domain, String role, String token);

  Mono<ResponseEntity<DataResponse<UserProfileResponse>>> profile();
}
