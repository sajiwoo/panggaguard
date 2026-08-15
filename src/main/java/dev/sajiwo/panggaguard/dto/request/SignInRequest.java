package dev.sajiwo.panggaguard.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SignInRequest extends InternalBaseRequest {
  @NotBlank
  @Email
  private String username;

  @NotBlank
  private String password;
}
