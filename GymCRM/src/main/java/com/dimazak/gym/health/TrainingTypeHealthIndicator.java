package com.dimazak.gym.health;

import com.dimazak.gym.service.TrainingTypeService;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class TrainingTypeHealthIndicator implements HealthIndicator {

    private final TrainingTypeService trainingTypeService;

    public TrainingTypeHealthIndicator(TrainingTypeService trainingTypeService) {
        this.trainingTypeService = trainingTypeService;
    }

    @Override
    public Health health() {
        try {
            int count = trainingTypeService.getAllTrainingTypes().size();
            if (count == 0) {
                return Health.down()
                        .withDetail("reason", "No training types found in database")
                        .build();
            }
            return Health.up()
                    .withDetail("trainingTypesCount", count)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
