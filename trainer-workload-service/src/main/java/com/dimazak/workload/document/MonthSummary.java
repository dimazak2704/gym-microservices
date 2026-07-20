package com.dimazak.workload.document;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthSummary {

    private int month;

    private int totalDuration;
}