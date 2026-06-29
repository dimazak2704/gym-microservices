package com.dimazak.workload.dto;

import java.util.List;

public record YearSummary(
        int year,
        List<MonthSummary> months
) {}