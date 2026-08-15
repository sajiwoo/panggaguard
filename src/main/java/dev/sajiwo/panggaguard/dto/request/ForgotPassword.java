package dev.sajiwo.panggaguard.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ForgotPassword extends InternalBaseRequest {

  @NotBlank
  private String email;

}
