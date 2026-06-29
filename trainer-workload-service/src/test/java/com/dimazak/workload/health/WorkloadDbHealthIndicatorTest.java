package com.dimazak.workload.health;

import com.dimazak.workload.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadDbHealthIndicatorTest {

    @Mock private TrainerWorkloadRepository repository;

    @InjectMocks private WorkloadDbHealthIndicator indicator;

    @Test
    void health_shouldReturnUpWithCount() {
        when(repository.count()).thenReturn(5L);

        Health health = indicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals(5L, health.getDetails().get("totalWorkloadRecords"));
    }

    @Test
    void health_shouldReturnDownOnException() {
        when(repository.count()).thenThrow(new RuntimeException("db down"));

        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("db down", health.getDetails().get("error"));
    }
}