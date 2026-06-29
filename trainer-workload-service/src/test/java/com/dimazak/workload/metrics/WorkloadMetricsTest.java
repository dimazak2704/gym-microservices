package com.dimazak.workload.metrics;

import com.dimazak.workload.repository.TrainerWorkloadRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadMetricsTest {

    @Mock private TrainerWorkloadRepository repository;

    private MeterRegistry registry;
    private WorkloadMetrics metrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metrics = new WorkloadMetrics(registry, repository);
    }

    @Test
    void incrementAdd_shouldIncrease() {
        metrics.incrementAdd();
        metrics.incrementAdd();
        assertEquals(2.0, registry.find("workload.operations.add").counter().count());
    }

    @Test
    void incrementDelete_shouldIncrease() {
        metrics.incrementDelete();
        assertEquals(1.0, registry.find("workload.operations.delete").counter().count());
    }

    @Test
    void gauge_shouldReflectRepositoryCount() {
        when(repository.count()).thenReturn(7L);
        assertEquals(7.0, registry.find("workload.records.total").gauge().value());
    }

    @Test
    void gauge_shouldReturnZeroOnException() {
        when(repository.count()).thenThrow(new RuntimeException("db down"));
        assertEquals(0.0, registry.find("workload.records.total").gauge().value());
    }
}