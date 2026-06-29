package com.dimazak.gym.dto;

import jakarta.validation.constraints.NotNull;

public record ActivateDeactivateRequest(
        @NotNull(message = "Is Active is required") Boolean isActive
) {}