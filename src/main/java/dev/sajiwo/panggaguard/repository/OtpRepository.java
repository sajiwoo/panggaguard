package dev.sajiwo.panggaguard.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.sajiwo.panggaguard.entity.Otp;

public interface OtpRepository extends JpaRepository<Otp, String> {

  Optional<Otp> findByTokenAndReferenceAndIsValidTrue(String token, String reference);

}
