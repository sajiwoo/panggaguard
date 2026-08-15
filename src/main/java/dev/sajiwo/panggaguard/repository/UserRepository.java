package dev.sajiwo.panggaguard.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.sajiwo.panggaguard.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

  Optional<User> findByDomainAndEmail(String domain, String email);

  boolean existsByDomainAndEmail(String domain, String email);
}
