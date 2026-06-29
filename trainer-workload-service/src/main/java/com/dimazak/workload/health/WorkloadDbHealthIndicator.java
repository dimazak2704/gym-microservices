package com.dimazak.workload.health;

import com.dimazak.workload.repository.TrainerWorkloadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkloadDbHealthIndicator implements HealthIndicator {

    private final TrainerWorkloadRepository repository;

    @Override
    public Health health() {
        try {
            long count = repository.count();
            return Health.up()
                    .withDetail("totalWorkloadRecords", count)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}