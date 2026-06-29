package com.dimazak.workload.metrics;

import com.dimazak.workload.repository.TrainerWorkloadRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkloadMetrics {

    private final Counter addOperations;
    private final Counter deleteOperations;
    private final TrainerWorkloadRepository repository;

    public WorkloadMetrics(MeterRegistry registry,
                           TrainerWorkloadRepository repository) {
        this.repository = repository;

        this.addOperations = Counter.builder("workload.operations.add")
                .description("Total number of ADD workload operations processed")
                .register(registry);

        this.deleteOperations = Counter.builder("workload.operations.delete")
                .description("Total number of DELETE workload operations processed")
                .register(registry);

        Gauge.builder("workload.records.total", this, WorkloadMetrics::countRecords)
                .description("Current number of workload records in the database")
                .register(registry);
    }

    private double countRecords() {
        try {
            return repository.count();
        } catch (Exception e) {
            log.warn("Failed to count workload records for gauge: {}", e.getMessage());
            return 0;
        }
    }

    public void incrementAdd() {
        addOperations.increment();
        log.debug("ADD operation metric incremented");
    }

    public void incrementDelete() {
        deleteOperations.increment();
        log.debug("DELETE operation metric incremented");
    }
}