package com.dimazak.workload.controller;

import com.dimazak.workload.dto.TrainerSummaryResponse;
import com.dimazak.workload.dto.WorkloadRequest;
import com.dimazak.workload.service.WorkloadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/workload")
@RequiredArgsConstructor
@Tag(name = "Workload", description = "Trainer workload management")
public class WorkloadController {

    private final WorkloadService workloadService;

    @PostMapping
    @Operation(summary = "Update trainer workload",
            description = "Accepts ADD/DELETE action to update trainer's monthly hours")
    public ResponseEntity<Void> updateWorkload(@Valid @RequestBody WorkloadRequest request) {
        log.info("Received workload request for trainer: '{}', action: {}",
                request.trainerUsername(), request.actionType());
        workloadService.processWorkload(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get trainer workload summary",
            description = "Returns monthly training hours. Optionally filter by year and month.")
    public ResponseEntity<TrainerSummaryResponse> getSummary(
            @PathVariable String username,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        log.info("Received summary request for trainer: '{}', year={}, month={}",
                username, year, month);
        return ResponseEntity.ok(workloadService.getSummary(username, year, month));
    }
}