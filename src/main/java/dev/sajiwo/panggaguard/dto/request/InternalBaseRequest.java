package dev.sajiwo.panggaguard.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class InternalBaseRequest {

  @JsonIgnore
  private String domain;

}
