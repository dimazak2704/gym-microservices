package com.dimazak.gym.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record AddTrainingRequest(
        @NotBlank(message = "Trainee username is required") String traineeUsername,
        @NotBlank(message = "Trainer username is required") String trainerUsername,
        @NotBlank(message = "Training name is required") String trainingName,
        @NotNull(message = "Training date is required") LocalDate trainingDate,
        @NotNull(message = "Training duration is required")
        @Positive(message = "Training duration must be positive") Integer trainingDuration
) {}