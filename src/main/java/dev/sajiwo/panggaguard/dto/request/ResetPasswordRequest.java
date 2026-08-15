package dev.sajiwo.panggaguard.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ResetPasswordRequest extends InternalBaseRequest {

  @NotBlank
  private String token;

  @NotBlank
  private String email;

  @NotBlank
  private String password;
}
