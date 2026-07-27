package dev.sajiwo.panggaguard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import dev.sajiwo.panggaguard.entity.UserActivity;
import jakarta.transaction.Transactional;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, String> {

  @Modifying
  @Transactional
  @Query("UPDATE UserActivity u SET u.isValid = false WHERE u.id = :tokenId")
  int invalidatedTokenId(String tokenId);
}
