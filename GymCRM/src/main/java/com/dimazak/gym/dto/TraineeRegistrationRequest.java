package com.dimazak.gym.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TraineeRegistrationRequest(
        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50, message = "First name must be 2-50 characters")
        @Pattern(regexp = "^[a-zA-Z]+$", message = "First name must contain only letters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50, message = "Last name must be 2-50 characters")
        @Pattern(regexp = "^[a-zA-Z]+$", message = "Last name must contain only letters")
        String lastName,

        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @Size(max = 200, message = "Address must not exceed 200 characters")
        String address
) {}