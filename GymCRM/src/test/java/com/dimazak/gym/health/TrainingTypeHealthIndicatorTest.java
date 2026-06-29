package com.dimazak.gym.health;

import com.dimazak.gym.dto.TrainingTypeResponse;
import com.dimazak.gym.service.TrainingTypeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingTypeHealthIndicatorTest {

    @Mock
    private TrainingTypeService trainingTypeService;

    @InjectMocks
    private TrainingTypeHealthIndicator indicator;

    @Test
    void health_shouldReturnUpWhenTypesExist() {
        List<TrainingTypeResponse> types = List.of(
                new TrainingTypeResponse(1L, "Cardio"),
                new TrainingTypeResponse(2L, "Strength"),
                new TrainingTypeResponse(3L, "Yoga"));
        when(trainingTypeService.getAllTrainingTypes()).thenReturn(types);

        Health health = indicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals(3, health.getDetails().get("trainingTypesCount"));
    }

    @Test
    void health_shouldReturnDownWhenNoTypesFound() {
        when(trainingTypeService.getAllTrainingTypes()).thenReturn(List.of());

        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("No training types found in database", health.getDetails().get("reason"));
    }

    @Test
    void health_shouldReturnDownWhenExceptionThrown() {
        when(trainingTypeService.getAllTrainingTypes())
                .thenThrow(new RuntimeException("DB connection failed"));

        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("DB connection failed", health.getDetails().get("error"));
    }

    @Test
    void health_shouldReturnUpWithSingleType() {
        when(trainingTypeService.getAllTrainingTypes())
                .thenReturn(List.of(new TrainingTypeResponse(1L, "Cardio")));

        Health health = indicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals(1, health.getDetails().get("trainingTypesCount"));
    }
}