package dev.sajiwo.panggaguard.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignUpRequest(@NotBlank String firstName, String lastName,
    @NotBlank @Email String email,
    @NotBlank String role,
    @NotBlank String password) {
}
