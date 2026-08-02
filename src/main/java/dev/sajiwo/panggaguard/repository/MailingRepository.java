package dev.sajiwo.panggaguard.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.sajiwo.panggaguard.entity.MailingConfig;

public interface MailingRepository extends JpaRepository<MailingConfig, String> {
}
