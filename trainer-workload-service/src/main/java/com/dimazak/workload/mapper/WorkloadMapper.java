package com.dimazak.workload.mapper;

import com.dimazak.workload.dto.*;
import com.dimazak.workload.entity.TrainerWorkload;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class WorkloadMapper {

    public TrainerSummaryResponse toResponse(String username, List<TrainerWorkload> rows) {
        if (rows.isEmpty()) {
            return new TrainerSummaryResponse(username, null, null, false, List.of());
        }

        TrainerWorkload first = rows.get(0);

        Map<Integer, List<TrainerWorkload>> byYear = rows.stream()
                .collect(Collectors.groupingBy(TrainerWorkload::getYear));

        List<YearSummary> years = byYear.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new YearSummary(
                        entry.getKey(),
                        entry.getValue().stream()
                                .sorted(Comparator.comparingInt(TrainerWorkload::getMonth))
                                .map(w -> new MonthSummary(w.getMonth(), w.getTotalDuration()))
                                .toList()
                ))
                .toList();

        return new TrainerSummaryResponse(
                first.getUsername(),
                first.getFirstName(),
                first.getLastName(),
                first.isActive(),
                years
        );
    }
}