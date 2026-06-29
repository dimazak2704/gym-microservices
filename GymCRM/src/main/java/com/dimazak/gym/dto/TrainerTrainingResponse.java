package com.dimazak.gym.dto;

import java.time.LocalDate;

public record TrainerTrainingResponse(
        String trainingName,
        LocalDate trainingDate,
        String trainingType,
        int trainingDuration,
        String traineeName
) {}