package com.dimazak.gym.metrics;

import com.dimazak.gym.service.UserService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class GymMetrics {

    private static final Logger log = LoggerFactory.getLogger(GymMetrics.class);

    private final Counter traineeRegistrations;
    private final Counter trainerRegistrations;
    private final Counter trainingsCreated;
    private final UserService userService;

    public GymMetrics(MeterRegistry registry, UserService userService) {
        this.userService = userService;

        this.traineeRegistrations = Counter.builder("gym.trainee.registrations")
                .description("Total number of registered trainees")
                .register(registry);

        this.trainerRegistrations = Counter.builder("gym.trainer.registrations")
                .description("Total number of registered trainers")
                .register(registry);

        this.trainingsCreated = Counter.builder("gym.trainings.created")
                .description("Total number of created trainings")
                .register(registry);

        Gauge.builder("gym.users.active", this, GymMetrics::getActiveUserCount)
                .description("Current number of active users")
                .register(registry);
    }

    private double getActiveUserCount() {
        try {
            return userService.countActiveUsers();
        } catch (Exception e) {
            log.warn("Failed to count active users for gauge: {}", e.getMessage());
            return 0;
        }
    }

    public void incrementTraineeRegistration() {
        traineeRegistrations.increment();
        log.debug("Trainee registration metric incremented");
    }

    public void incrementTrainerRegistration() {
        trainerRegistrations.increment();
        log.debug("Trainer registration metric incremented");
    }

    public void incrementTrainingCreated() {
        trainingsCreated.increment();
        log.debug("Training creation metric incremented");
    }
}