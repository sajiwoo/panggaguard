package dev.sajiwo.panggaguard.dto;

import lombok.Data;

@Data
public class JsonWebToken {

  private String jti;

  private String bearerToken;

  private String refreshToken;
}
