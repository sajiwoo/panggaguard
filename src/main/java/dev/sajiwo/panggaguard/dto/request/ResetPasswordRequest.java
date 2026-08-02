package dev.sajiwo.panggaguard.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(@NotBlank String token, @NotBlank String email, @NotBlank String password) {
}
