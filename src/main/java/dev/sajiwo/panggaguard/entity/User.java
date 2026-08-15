package dev.sajiwo.panggaguard.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "USERS")
public class User extends AuditEntity<String, LocalDateTime> {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "USER_ID")
  private UUID id;

  @Column(name = "EMAIL")
  private String email;

  @Column(name = "FIRST_NAME")
  private String firstName;

  @Column(name = "LAST_NAME")
  private String lastName;

  @Column(name = "PASSWORD")
  private String password;

  @Column(name = "ROLE")
  private String role;

  @Transient
  @EqualsAndHashCode.Exclude
  private boolean isNew = true;

  @Column(name = "DOMAIN")
  private String domain;

}
