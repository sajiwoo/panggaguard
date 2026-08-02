package dev.sajiwo.panggaguard.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import dev.sajiwo.panggaguard.entity.Otp;
import dev.sajiwo.panggaguard.entity.User;
import dev.sajiwo.panggaguard.repository.OtpRepository;
import dev.sajiwo.panggaguard.service.OtpService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

  private final OtpRepository otpRepository;
  private final SecureRandom secureRandom = new SecureRandom();

  @Override
  public Mono<String> generateOtp(User user) {
    return Mono.fromCallable(() -> {
      String token = UUID.randomUUID().toString() + String.format("%06d", secureRandom.nextInt(1000000));

      Otp otp = new Otp();
      otp.setToken(token);
      otp.setUserId(user.getId().toString());
      otp.setReference(user.getEmail());
      otpRepository.save(otp);

      return token;
    }).subscribeOn(Schedulers.boundedElastic());
  }

  @Override
  public Mono<Boolean> verifyOtp(String token, String email) {
    return Mono.fromCallable(() -> {
      return otpRepository.findByTokenAndReferenceAndIsValidTrue(token, email)
          .map(otp -> {
            if (otp.getCreatedAt().plusMinutes(15).isBefore(LocalDateTime.now())) {
              return false;
            }
            otp.setIsValid(false);
            otpRepository.save(otp);

            return true;
          }).orElse(false);
    }).subscribeOn(Schedulers.boundedElastic());
  }
}
