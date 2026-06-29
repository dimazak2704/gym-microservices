package com.dimazak.gym.dto;

import jakarta.validation.constraints.*;

public record TrainerRegistrationRequest(
        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50, message = "First name must be 2-50 characters")
        @Pattern(regexp = "^[a-zA-Z]+$", message = "First name must contain only letters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50, message = "Last name must be 2-50 characters")
        @Pattern(regexp = "^[a-zA-Z]+$", message = "Last name must contain only letters")
        String lastName,

        @NotNull(message = "Specialization is required")
        @Positive(message = "Specialization ID must be positive")
        Long specializationId
) {}