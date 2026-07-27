package dev.sajiwo.panggaguard.entity;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class AuditEntity<U, T> {

  @CreatedBy
  @Column(name = "CREATED_BY")
  private U createdBy;

  @CreatedDate
  @Column(name = "CREATED_AT")
  private T createdAt;

  @LastModifiedBy
  @Column(name = "UPDATED_BY")
  private U updatedBy;

  @LastModifiedDate
  @Column(name = "UPDATED_AT")
  private T updatedAt;

  @Column(name = "DELETED_BY")
  private U deletedBy;

  @Column(name = "DELETED_AT")
  private T deletedAt;
}
