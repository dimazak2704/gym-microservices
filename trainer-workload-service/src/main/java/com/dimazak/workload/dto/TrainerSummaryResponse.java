package com.dimazak.workload.dto;

import java.util.List;

public record TrainerSummaryResponse(
        String username,
        String firstName,
        String lastName,
        boolean active,
        List<YearSummary> years
) {}