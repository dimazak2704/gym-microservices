package com.dimazak.gym.dto;

import java.time.LocalDate;

public record TraineeTrainingResponse(
        String trainingName,
        LocalDate trainingDate,
        String trainingType,
        int trainingDuration,
        String trainerName
) {}