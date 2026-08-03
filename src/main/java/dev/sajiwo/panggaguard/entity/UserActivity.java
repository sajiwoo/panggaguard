package dev.sajiwo.panggaguard.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "USER_ACTIVITIES")
public class UserActivity extends AuditEntity<String, LocalDateTime> {

  @Id
  @Column(name = "USER_ACTIVITIES_ID")
  private String id;

  @Column(name = "USER_ID")
  private String userId;

  @Column(name = "TYPE")
  private String type;

  @Column(name = "IS_VALID", nullable = false)
  private Boolean isValid = true;

  @Column(name = "PLATFORM")
  private String platform;

  @Column(name = "OAUTH2_TOKEN")
  private String oatuh2Token;

}
