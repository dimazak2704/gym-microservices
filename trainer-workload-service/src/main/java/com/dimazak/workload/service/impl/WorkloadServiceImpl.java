package com.dimazak.workload.service.impl;

import com.dimazak.workload.document.MonthSummary;
import com.dimazak.workload.document.TrainerWorkloadDocument;
import com.dimazak.workload.document.YearSummary;
import com.dimazak.workload.dto.ActionType;
import com.dimazak.workload.dto.TrainerSummaryResponse;
import com.dimazak.workload.dto.WorkloadRequest;
import com.dimazak.workload.mapper.WorkloadMapper;
import com.dimazak.workload.metrics.WorkloadMetrics;
import com.dimazak.workload.repository.TrainerWorkloadRepository;
import com.dimazak.workload.service.WorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkloadServiceImpl implements WorkloadService {

    private final TrainerWorkloadRepository repository;
    private final WorkloadMapper mapper;
    private final WorkloadMetrics workloadMetrics;

    @Override
    public void processWorkload(WorkloadRequest request) {
        log.info("Processing workload: trainer='{}', date={}, duration={}, action={}",
                request.trainerUsername(), request.trainingDate(),
                request.trainingDuration(), request.actionType());

        int year = request.trainingDate().getYear();
        int month = request.trainingDate().getMonthValue();

        Optional<TrainerWorkloadDocument> existing =
                repository.findByUsername(request.trainerUsername());

        if (request.actionType() == ActionType.ADD) {
            handleAdd(request, year, month, existing);
            workloadMetrics.incrementAdd();
        } else {
            handleDelete(request, year, month, existing);
            workloadMetrics.incrementDelete();
        }
    }

    private void handleAdd(WorkloadRequest request, int year, int month,
                           Optional<TrainerWorkloadDocument> existing) {
        TrainerWorkloadDocument doc = existing.orElseGet(() -> {
            log.debug("No document found for '{}'. Creating new one.",
                    request.trainerUsername());
            return TrainerWorkloadDocument.builder()
                    .username(request.trainerUsername())
                    .years(new ArrayList<>())
                    .build();
        });

        doc.setFirstName(request.trainerFirstName());
        doc.setLastName(request.trainerLastName());
        doc.setActive(request.isActive());

        MonthSummary monthSummary = findOrCreateMonth(doc, year, month);

        int updated = monthSummary.getTotalDuration() + request.trainingDuration();
        monthSummary.setTotalDuration(updated);

        repository.save(doc);

        log.info("ADD processed for '{}' {}-{}: total duration now {} min",
                request.trainerUsername(), year, month, updated);
    }

    private void handleDelete(WorkloadRequest request, int year, int month,
                              Optional<TrainerWorkloadDocument> existing) {
        if (existing.isEmpty()) {
            log.warn("DELETE requested for '{}' {}-{}, but no document found. Ignoring.",
                    request.trainerUsername(), year, month);
            return;
        }

        TrainerWorkloadDocument doc = existing.get();

        Optional<MonthSummary> monthOpt = findMonth(doc, year, month);
        if (monthOpt.isEmpty()) {
            log.warn("DELETE requested for '{}' {}-{}, but no matching month found. Ignoring.",
                    request.trainerUsername(), year, month);
            return;
        }

        MonthSummary monthSummary = monthOpt.get();
        int updated = monthSummary.getTotalDuration() - request.trainingDuration();

        if (updated <= 0) {
            removeMonth(doc, year, month);
            log.info("Removed month record for '{}' {}-{} (reached zero)",
                    request.trainerUsername(), year, month);
        } else {
            monthSummary.setTotalDuration(updated);
            log.info("Decreased workload for '{}' {}-{}: total now {} min",
                    request.trainerUsername(), year, month, updated);
        }

        repository.save(doc);
    }

    private MonthSummary findOrCreateMonth(TrainerWorkloadDocument doc, int year, int month) {
        YearSummary yearSummary = doc.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseGet(() -> {
                    log.debug("Creating new year element: {}", year);
                    YearSummary newYear = YearSummary.builder()
                            .year(year)
                            .months(new ArrayList<>())
                            .build();
                    doc.getYears().add(newYear);
                    return newYear;
                });

        return yearSummary.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElseGet(() -> {
                    log.debug("Creating new month element: {}-{}", year, month);
                    MonthSummary newMonth = MonthSummary.builder()
                            .month(month)
                            .totalDuration(0)
                            .build();
                    yearSummary.getMonths().add(newMonth);
                    return newMonth;
                });
    }

    private Optional<MonthSummary> findMonth(TrainerWorkloadDocument doc, int year, int month) {
        return doc.getYears().stream()
                .filter(y -> y.getYear() == year)
                .flatMap(y -> y.getMonths().stream())
                .filter(m -> m.getMonth() == month)
                .findFirst();
    }

    private void removeMonth(TrainerWorkloadDocument doc, int year, int month) {
        doc.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .ifPresent(y -> {
                    y.getMonths().removeIf(m -> m.getMonth() == month);
                    if (y.getMonths().isEmpty()) {
                        doc.getYears().removeIf(yr -> yr.getYear() == year);
                    }
                });
    }

    @Override
    public TrainerSummaryResponse getSummary(String username, Integer year, Integer month) {
        log.info("Fetching workload summary for trainer: '{}', year={}, month={}",
                username, year, month);

        TrainerWorkloadDocument document = repository.findByUsername(username).orElse(null);
        return mapper.toResponse(username, filter(document, year, month));
    }

    private TrainerWorkloadDocument filter(TrainerWorkloadDocument document, Integer year, Integer month) {
        if (document == null || year == null) {
            return document;
        }

        List<YearSummary> years = document.getYears().stream()
                .filter(yearSummary -> yearSummary.getYear() == year)
                .map(yearSummary -> YearSummary.builder()
                        .year(yearSummary.getYear())
                        .months(yearSummary.getMonths().stream()
                                .filter(monthSummary -> month == null
                                        || monthSummary.getMonth() == month)
                                .map(monthSummary -> MonthSummary.builder()
                                        .month(monthSummary.getMonth())
                                        .totalDuration(monthSummary.getTotalDuration())
                                        .build())
                                .toList())
                        .build())
                .toList();

        return TrainerWorkloadDocument.builder()
                .id(document.getId())
                .username(document.getUsername())
                .firstName(document.getFirstName())
                .lastName(document.getLastName())
                .active(document.isActive())
                .years(years)
                .build();
    }
}
