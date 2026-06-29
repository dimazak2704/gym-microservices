package com.dimazak.gym.service;

import com.dimazak.gym.client.WorkloadGateway;
import com.dimazak.gym.dao.TraineeDao;
import com.dimazak.gym.dao.TrainerDao;
import com.dimazak.gym.dao.TrainingDao;
import com.dimazak.gym.dao.TrainingTypeDao;
import com.dimazak.gym.exception.EntityNotFoundException;
import com.dimazak.gym.exception.ValidationException;
import com.dimazak.gym.metrics.GymMetrics;
import com.dimazak.gym.model.*;
import com.dimazak.gym.service.impl.TrainingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    private static final String TRAINEE_USERNAME = "John.Doe";
    private static final String TRAINER_USERNAME = "Jane.Smith";
    private static final String TRAINING_NAME = "Morning Cardio";
    private static final LocalDate TRAINING_DATE = LocalDate.of(2024, 4, 1);
    private static final int DURATION = 60;
    private static final String SPECIALIZATION = "Cardio";

    @Mock private TrainingDao trainingDao;
    @Mock private TraineeDao traineeDao;
    @Mock private TrainerDao trainerDao;
    @Mock private TrainingTypeDao trainingTypeDao;
    @Mock private GymMetrics gymMetrics;
    @Mock private WorkloadGateway workloadGateway;

    @InjectMocks private TrainingServiceImpl trainingService;

    private Trainee buildTrainee() {
        return new Trainee(1L, LocalDate.of(1990, 1, 1), "Addr",
                new User(1L, "John", "Doe", TRAINEE_USERNAME, "p", true, Role.TRAINEE));
    }

    private Trainer buildTrainer() {
        return new Trainer(1L, new TrainingType(1L, SPECIALIZATION),
                new User(2L, "Jane", "Smith", TRAINER_USERNAME, "p", true, Role.TRAINER));
    }

    @Test
    void addTraining_shouldCreateUsingTrainerSpecialization() {
        Trainee trainee = buildTrainee();
        Trainer trainer = buildTrainer();
        when(traineeDao.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(trainer));
        when(trainingDao.save(any(Training.class))).thenAnswer(inv -> {
            Training t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        Training result = trainingService.addTraining(
                TRAINEE_USERNAME, TRAINER_USERNAME, TRAINING_NAME, TRAINING_DATE, DURATION);

        assertEquals(SPECIALIZATION, result.getTrainingType().getTrainingTypeName());
        verify(gymMetrics).incrementTrainingCreated();
    }

    @Test
    void addTraining_shouldThrowWhenTraineeNotFound() {
        when(traineeDao.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> trainingService.addTraining(TRAINEE_USERNAME, TRAINER_USERNAME,
                        TRAINING_NAME, TRAINING_DATE, DURATION));
    }

    @Test
    void addTraining_shouldThrowWhenTrainerNotFound() {
        when(traineeDao.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.of(buildTrainee()));
        when(trainerDao.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> trainingService.addTraining(TRAINEE_USERNAME, TRAINER_USERNAME,
                        TRAINING_NAME, TRAINING_DATE, DURATION));
    }

    @Test
    void addTraining_shouldThrowWhenNameBlank() {
        assertThrows(ValidationException.class,
                () -> trainingService.addTraining(TRAINEE_USERNAME, TRAINER_USERNAME,
                        "", TRAINING_DATE, DURATION));
    }

    @Test
    void addTraining_shouldThrowWhenDateNull() {
        assertThrows(ValidationException.class,
                () -> trainingService.addTraining(TRAINEE_USERNAME, TRAINER_USERNAME,
                        TRAINING_NAME, null, DURATION));
    }

    @Test
    void addTraining_shouldThrowWhenDurationZero() {
        assertThrows(ValidationException.class,
                () -> trainingService.addTraining(TRAINEE_USERNAME, TRAINER_USERNAME,
                        TRAINING_NAME, TRAINING_DATE, 0));
    }

    @Test
    void addTraining_shouldThrowWhenDurationNegative() {
        assertThrows(ValidationException.class,
                () -> trainingService.addTraining(TRAINEE_USERNAME, TRAINER_USERNAME,
                        TRAINING_NAME, TRAINING_DATE, -1));
    }

    @Test
    void addTrainingWithType_shouldUseProvidedType() {
        Trainee trainee = buildTrainee();
        Trainer trainer = buildTrainer();
        TrainingType yoga = new TrainingType(2L, "Yoga");
        when(traineeDao.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(trainer));
        when(trainingTypeDao.findById(2L)).thenReturn(Optional.of(yoga));
        when(trainingDao.save(any(Training.class))).thenAnswer(inv -> {
            Training t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        Training result = trainingService.addTraining(
                TRAINEE_USERNAME, TRAINER_USERNAME, TRAINING_NAME, 2L, TRAINING_DATE, DURATION);

        assertEquals("Yoga", result.getTrainingType().getTrainingTypeName());
    }

    @Test
    void addTrainingWithType_shouldThrowWhenTypeNotFound() {
        when(traineeDao.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.of(buildTrainee()));
        when(trainerDao.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(buildTrainer()));
        when(trainingTypeDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> trainingService.addTraining(TRAINEE_USERNAME, TRAINER_USERNAME,
                        TRAINING_NAME, 99L, TRAINING_DATE, DURATION));
    }

    @Test
    void addTraining_shouldNotifyWorkloadService() {
        Trainee trainee = buildTrainee();
        Trainer trainer = buildTrainer();
        when(traineeDao.findByUsername(TRAINEE_USERNAME)).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(trainer));
        when(trainingDao.save(any(Training.class))).thenAnswer(inv -> {
            Training t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        trainingService.addTraining(TRAINEE_USERNAME, TRAINER_USERNAME,
                TRAINING_NAME, TRAINING_DATE, DURATION);

        verify(workloadGateway).notifyTrainingAdded(any(Training.class));
    }
}