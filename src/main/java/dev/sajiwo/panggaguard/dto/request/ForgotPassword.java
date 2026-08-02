package dev.sajiwo.panggaguard.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ForgotPassword(@NotBlank String email) {
}
