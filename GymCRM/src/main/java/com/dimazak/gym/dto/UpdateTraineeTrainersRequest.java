package com.dimazak.gym.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record UpdateTraineeTrainersRequest(
        @NotEmpty(message = "Trainers list is required")
        List<@NotBlank String> trainerUsernames
) {}