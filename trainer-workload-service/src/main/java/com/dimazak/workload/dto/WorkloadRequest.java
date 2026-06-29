package com.dimazak.workload.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record WorkloadRequest(

        @NotBlank(message = "Trainer username is required")
        String trainerUsername,

        @NotBlank(message = "Trainer first name is required")
        String trainerFirstName,

        @NotBlank(message = "Trainer last name is required")
        String trainerLastName,

        @NotNull(message = "Active status is required")
        Boolean isActive,

        @NotNull(message = "Training date is required")
        LocalDate trainingDate,

        @NotNull(message = "Training duration is required")
        @Positive(message = "Training duration must be positive")
        Integer trainingDuration,

        @NotNull(message = "Action type is required")
        ActionType actionType
) {}