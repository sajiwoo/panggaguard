package dev.sajiwo.panggaguard.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ROUTES")
public class Route extends AuditEntity<String, LocalDateTime> {

  @Id
  @Column(name = "ROUTE_ID")
  private UUID id;

  @Column(name = "DOMAIN", unique = true)
  private String domain;

  @Column(name = "URI")
  private String uri;

}
