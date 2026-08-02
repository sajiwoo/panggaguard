package dev.sajiwo.panggaguard.service;

import dev.sajiwo.panggaguard.entity.User;
import reactor.core.publisher.Mono;

public interface OtpService {

  Mono<String> generateOtp(User user);

  Mono<Boolean> verifyOtp(String token, String email);

}
