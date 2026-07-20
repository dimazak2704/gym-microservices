package com.dimazak.workload.mapper;

import com.dimazak.workload.document.TrainerWorkloadDocument;
import com.dimazak.workload.dto.MonthSummary;
import com.dimazak.workload.dto.TrainerSummaryResponse;
import com.dimazak.workload.dto.YearSummary;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class WorkloadMapper {

    public TrainerSummaryResponse toResponse(String username, TrainerWorkloadDocument doc) {
        if (doc == null) {
            return new TrainerSummaryResponse(username, null, null, false, List.of());
        }

        List<YearSummary> years = doc.getYears().stream()
                .sorted(Comparator.comparingInt(com.dimazak.workload.document.YearSummary::getYear))
                .map(y -> new YearSummary(
                        y.getYear(),
                        y.getMonths().stream()
                                .sorted(Comparator.comparingInt(
                                        com.dimazak.workload.document.MonthSummary::getMonth))
                                .map(m -> new MonthSummary(m.getMonth(), m.getTotalDuration()))
                                .toList()
                ))
                .toList();

        return new TrainerSummaryResponse(
                doc.getUsername(),
                doc.getFirstName(),
                doc.getLastName(),
                doc.isActive(),
                years
        );
    }
}