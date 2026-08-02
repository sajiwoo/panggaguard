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
@Table(name = "MAILING")
public class MailingConfig extends AuditEntity<String, LocalDateTime> {

  @Id
  @Column(name = "MAILING_ID")
  private String id;

  @Column(name = "SUBJECT")
  private String subject;

  @Column(name = "SENDER")
  private String sender;

  @Column(name = "MESSAGE")
  private String message;

}
