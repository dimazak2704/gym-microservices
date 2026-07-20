package com.dimazak.workload.document;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YearSummary {

    private int year;

    @Builder.Default
    private List<MonthSummary> months = new ArrayList<>();
}