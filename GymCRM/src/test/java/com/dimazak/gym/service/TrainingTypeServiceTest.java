package com.dimazak.gym.service;

import com.dimazak.gym.dao.TrainingTypeDao;
import com.dimazak.gym.dto.TrainingTypeResponse;
import com.dimazak.gym.model.TrainingType;
import com.dimazak.gym.service.impl.TrainingTypeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceTest {

    @Mock private TrainingTypeDao trainingTypeDao;

    @InjectMocks private TrainingTypeServiceImpl service;

    @Test
    void getAllTrainingTypes_shouldMapEntities() {
        when(trainingTypeDao.findAll()).thenReturn(List.of(
                new TrainingType(1L, "Cardio"),
                new TrainingType(2L, "Strength")));

        List<TrainingTypeResponse> result = service.getAllTrainingTypes();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals("Cardio", result.get(0).trainingType());
    }

    @Test
    void getAllTrainingTypes_shouldReturnEmptyWhenNone() {
        when(trainingTypeDao.findAll()).thenReturn(List.of());
        assertTrue(service.getAllTrainingTypes().isEmpty());
    }
}