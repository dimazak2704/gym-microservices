package com.dimazak.gym.metrics;

import com.dimazak.gym.service.UserService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
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
class GymMetricsTest {

    @Mock
    private UserService userService;

    private MeterRegistry registry;
    private GymMetrics gymMetrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        gymMetrics = new GymMetrics(registry, userService);
    }

    // ==================== Counters ====================

    @Test
    void incrementTraineeRegistration_shouldIncrementCounter() {
        gymMetrics.incrementTraineeRegistration();
        gymMetrics.incrementTraineeRegistration();

        Counter counter = registry.find("gym.trainee.registrations").counter();
        assertNotNull(counter);
        assertEquals(2.0, counter.count());
    }

    @Test
    void incrementTrainerRegistration_shouldIncrementCounter() {
        gymMetrics.incrementTrainerRegistration();

        Counter counter = registry.find("gym.trainer.registrations").counter();
        assertNotNull(counter);
        assertEquals(1.0, counter.count());
    }

    @Test
    void incrementTrainingCreated_shouldIncrementCounter() {
        gymMetrics.incrementTrainingCreated();
        gymMetrics.incrementTrainingCreated();
        gymMetrics.incrementTrainingCreated();

        Counter counter = registry.find("gym.trainings.created").counter();
        assertNotNull(counter);
        assertEquals(3.0, counter.count());
    }

    @Test
    void counters_shouldStartAtZero() {
        Counter traineeCounter = registry.find("gym.trainee.registrations").counter();
        Counter trainerCounter = registry.find("gym.trainer.registrations").counter();
        Counter trainingCounter = registry.find("gym.trainings.created").counter();

        assertNotNull(traineeCounter);
        assertNotNull(trainerCounter);
        assertNotNull(trainingCounter);
        assertEquals(0.0, traineeCounter.count());
        assertEquals(0.0, trainerCounter.count());
        assertEquals(0.0, trainingCounter.count());
    }

    // ==================== Gauge ====================

    @Test
    void activeUsersGauge_shouldReturnCountFromService() {
        when(userService.countActiveUsers()).thenReturn(42L);

        Gauge gauge = registry.find("gym.users.active").gauge();
        assertNotNull(gauge);
        assertEquals(42.0, gauge.value());
    }

    @Test
    void activeUsersGauge_shouldReturnZeroOnException() {
        when(userService.countActiveUsers()).thenThrow(new RuntimeException("DB error"));

        Gauge gauge = registry.find("gym.users.active").gauge();
        assertNotNull(gauge);
        assertEquals(0.0, gauge.value());
    }

    @Test
    void activeUsersGauge_shouldReturnZeroWhenNoActiveUsers() {
        when(userService.countActiveUsers()).thenReturn(0L);

        Gauge gauge = registry.find("gym.users.active").gauge();
        assertNotNull(gauge);
        assertEquals(0.0, gauge.value());
    }

    @Test
    void activeUsersGauge_shouldUpdateDynamically() {
        when(userService.countActiveUsers()).thenReturn(5L);
        Gauge gauge = registry.find("gym.users.active").gauge();
        assertEquals(5.0, gauge.value());

        when(userService.countActiveUsers()).thenReturn(10L);
        assertEquals(10.0, gauge.value());
    }

    // ==================== Registration ====================

    @Test
    void allMetrics_shouldBeRegisteredOnConstruction() {
        assertNotNull(registry.find("gym.trainee.registrations").counter());
        assertNotNull(registry.find("gym.trainer.registrations").counter());
        assertNotNull(registry.find("gym.trainings.created").counter());
        assertNotNull(registry.find("gym.users.active").gauge());
    }

    @Test
    void counters_shouldHaveDescriptions() {
        Counter trainee = registry.find("gym.trainee.registrations").counter();
        Counter trainer = registry.find("gym.trainer.registrations").counter();
        Counter training = registry.find("gym.trainings.created").counter();

        assertNotNull(trainee);
        assertNotNull(trainer);
        assertNotNull(training);
        assertNotNull(trainee.getId().getDescription());
        assertNotNull(trainer.getId().getDescription());
        assertNotNull(training.getId().getDescription());
    }
}