package dev.sajiwo.panggaguard.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignUpRequest(@JsonProperty("first_name") @NotBlank String firstName,
    @JsonProperty("last_name") String lastName,
    @NotBlank @Email String email,
    @NotBlank String role,
    @NotBlank String password) {
}
