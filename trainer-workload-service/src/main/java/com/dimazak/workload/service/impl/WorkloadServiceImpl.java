package com.dimazak.workload.service.impl;

import com.dimazak.workload.dto.ActionType;
import com.dimazak.workload.dto.TrainerSummaryResponse;
import com.dimazak.workload.dto.WorkloadRequest;
import com.dimazak.workload.entity.TrainerWorkload;
import com.dimazak.workload.mapper.WorkloadMapper;
import com.dimazak.workload.metrics.WorkloadMetrics;
import com.dimazak.workload.repository.TrainerWorkloadRepository;
import com.dimazak.workload.service.WorkloadService;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Timed(value = "workload.processing.time",
            description = "Time taken to process a workload request")
    @Transactional
    public void processWorkload(WorkloadRequest request) {
        log.info("Processing workload: trainer='{}', date={}, duration={}, action={}",
                request.trainerUsername(), request.trainingDate(),
                request.trainingDuration(), request.actionType());

        int year = request.trainingDate().getYear();
        int month = request.trainingDate().getMonthValue();

        Optional<TrainerWorkload> existing = repository
                .findByUsernameAndYearAndMonth(request.trainerUsername(), year, month);

        if (request.actionType() == ActionType.ADD) {
            handleAdd(request, year, month, existing);
            workloadMetrics.incrementAdd();
        } else {
            handleDelete(request, year, month, existing);
            workloadMetrics.incrementDelete();
        }
    }

    private void handleAdd(WorkloadRequest request, int year, int month,
                           Optional<TrainerWorkload> existing) {
        if (existing.isPresent()) {
            TrainerWorkload workload = existing.get();
            int updated = workload.getTotalDuration() + request.trainingDuration();
            workload.setTotalDuration(updated);
            workload.setFirstName(request.trainerFirstName());
            workload.setLastName(request.trainerLastName());
            workload.setActive(request.isActive());
            repository.save(workload);
            log.info("Updated workload for '{}' {}-{}: total now {} min",
                    request.trainerUsername(), year, month, updated);
        } else {
            TrainerWorkload workload = TrainerWorkload.builder()
                    .username(request.trainerUsername())
                    .firstName(request.trainerFirstName())
                    .lastName(request.trainerLastName())
                    .active(request.isActive())
                    .year(year)
                    .month(month)
                    .totalDuration(request.trainingDuration())
                    .build();
            repository.save(workload);
            log.info("Created new workload for '{}' {}-{}: {} min",
                    request.trainerUsername(), year, month, request.trainingDuration());
        }
    }

    private void handleDelete(WorkloadRequest request, int year, int month,
                              Optional<TrainerWorkload> existing) {
        if (existing.isEmpty()) {
            log.warn("DELETE requested for '{}' {}-{}, but no record found. Ignoring.",
                    request.trainerUsername(), year, month);
            return;
        }

        TrainerWorkload workload = existing.get();
        int updated = workload.getTotalDuration() - request.trainingDuration();

        if (updated <= 0) {
            repository.delete(workload);
            log.info("Removed workload record for '{}' {}-{} (reached zero)",
                    request.trainerUsername(), year, month);
        } else {
            workload.setTotalDuration(updated);
            repository.save(workload);
            log.info("Decreased workload for '{}' {}-{}: total now {} min",
                    request.trainerUsername(), year, month, updated);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TrainerSummaryResponse getSummary(String username, Integer year, Integer month) {
        log.info("Fetching workload summary for trainer: '{}', year={}, month={}",
                username, year, month);

        List<TrainerWorkload> rows;

        if (year != null && month != null) {
            rows = repository.findByUsernameAndYearAndMonth(username, year, month)
                    .map(List::of)
                    .orElse(List.of());
        } else if (year != null) {
            rows = repository.findByUsernameAndYear(username, year);
        } else {
            rows = repository.findByUsername(username);
        }

        return mapper.toResponse(username, rows);
    }
}