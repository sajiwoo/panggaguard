package dev.sajiwo.panggaguard.dto.response;

import java.time.LocalDateTime;

public record UserResponse(String firstName, String lastName, String email, String role, LocalDateTime registerDate) {
};
