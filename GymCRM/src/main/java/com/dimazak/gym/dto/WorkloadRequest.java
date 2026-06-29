package com.dimazak.gym.dto;

import java.time.LocalDate;

public record WorkloadRequest(
        String trainerUsername,
        String trainerFirstName,
        String trainerLastName,
        boolean isActive,
        LocalDate trainingDate,
        int trainingDuration,
        WorkloadActionType actionType
) {}