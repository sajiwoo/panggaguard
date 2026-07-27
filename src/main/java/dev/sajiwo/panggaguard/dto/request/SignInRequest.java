package dev.sajiwo.panggaguard.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignInRequest(@NotBlank @Email String username, @NotBlank String password) {
}
