package com.dimazak.gym.mapper;

import com.dimazak.gym.dto.*;
import com.dimazak.gym.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EntityMapperTest {

    private static final Long TRAINEE_ID = 1L;
    private static final Long TRAINER_ID = 2L;
    private static final Long TRAINING_ID = 3L;
    private static final String TRAINEE_FIRST_NAME = "John";
    private static final String TRAINEE_LAST_NAME = "Doe";
    private static final String TRAINEE_USERNAME = "John.Doe";
    private static final String TRAINER_FIRST_NAME = "Jane";
    private static final String TRAINER_LAST_NAME = "Smith";
    private static final String TRAINER_USERNAME = "Jane.Smith";
    private static final String SPECIALIZATION = "Cardio";
    private static final LocalDate BIRTH_DATE = LocalDate.of(1990, 5, 15);
    private static final String ADDRESS = "123 St";
    private static final String TRAINING_NAME = "Morning Run";
    private static final LocalDate TRAINING_DATE = LocalDate.of(2024, 4, 1);
    private static final int TRAINING_DURATION = 60;

    private final EntityMapper mapper = new EntityMapper();

    private final TrainingType cardioType = new TrainingType(1L, SPECIALIZATION);
    private final User traineeUser = new User(1L, TRAINEE_FIRST_NAME, TRAINEE_LAST_NAME,
            TRAINEE_USERNAME, "pass", true, Role.TRAINEE);
    private final User trainerUser = new User(2L, TRAINER_FIRST_NAME, TRAINER_LAST_NAME,
            TRAINER_USERNAME, "pass", true, Role.TRAINER);

    @Test
    void toTraineeProfileResponse_shouldMapAllFields() {
        Trainee trainee = new Trainee(TRAINEE_ID, BIRTH_DATE, ADDRESS, traineeUser);
        Trainer trainer = new Trainer(TRAINER_ID, cardioType, trainerUser);
        trainee.setTrainers(List.of(trainer));

        TraineeProfileResponse response = mapper.toTraineeProfileResponse(trainee);

        assertEquals(TRAINEE_FIRST_NAME, response.firstName());
        assertEquals(TRAINEE_LAST_NAME, response.lastName());
        assertEquals(BIRTH_DATE, response.dateOfBirth());
        assertEquals(ADDRESS, response.address());
        assertTrue(response.isActive());
        assertEquals(1, response.trainers().size());
        assertEquals(TRAINER_USERNAME, response.trainers().get(0).username());
    }

    @Test
    void toTraineeProfileResponse_shouldHandleEmptyTrainers() {
        Trainee trainee = new Trainee(TRAINEE_ID, null, null, traineeUser);
        trainee.setTrainers(List.of());

        TraineeProfileResponse response = mapper.toTraineeProfileResponse(trainee);

        assertNull(response.dateOfBirth());
        assertNull(response.address());
        assertTrue(response.trainers().isEmpty());
    }

    @Test
    void toUpdateTraineeResponse_shouldIncludeUsername() {
        Trainee trainee = new Trainee(TRAINEE_ID, BIRTH_DATE, ADDRESS, traineeUser);
        trainee.setTrainers(List.of());

        UpdateTraineeResponse response = mapper.toUpdateTraineeResponse(trainee);

        assertEquals(TRAINEE_USERNAME, response.username());
        assertEquals(TRAINEE_FIRST_NAME, response.firstName());
        assertEquals(BIRTH_DATE, response.dateOfBirth());
        assertEquals(ADDRESS, response.address());
        assertTrue(response.isActive());
    }

    @Test
    void toTrainerProfileResponse_shouldMapAllFields() {
        Trainer trainer = new Trainer(TRAINER_ID, cardioType, trainerUser);
        Trainee trainee = new Trainee(TRAINEE_ID, null, null, traineeUser);
        trainer.setTrainees(List.of(trainee));

        TrainerProfileResponse response = mapper.toTrainerProfileResponse(trainer);

        assertEquals(TRAINER_FIRST_NAME, response.firstName());
        assertEquals(TRAINER_LAST_NAME, response.lastName());
        assertEquals(SPECIALIZATION, response.specialization());
        assertTrue(response.isActive());
        assertEquals(1, response.trainees().size());
        assertEquals(TRAINEE_USERNAME, response.trainees().get(0).username());
    }

    @Test
    void toTrainerProfileResponse_shouldHandleEmptyTrainees() {
        Trainer trainer = new Trainer(TRAINER_ID, cardioType, trainerUser);
        trainer.setTrainees(List.of());

        TrainerProfileResponse response = mapper.toTrainerProfileResponse(trainer);

        assertTrue(response.trainees().isEmpty());
    }

    @Test
    void toUpdateTrainerResponse_shouldIncludeUsername() {
        Trainer trainer = new Trainer(TRAINER_ID, cardioType, trainerUser);
        trainer.setTrainees(List.of());

        UpdateTrainerResponse response = mapper.toUpdateTrainerResponse(trainer);

        assertEquals(TRAINER_USERNAME, response.username());
        assertEquals(SPECIALIZATION, response.specialization());
        assertTrue(response.isActive());
    }

    @Test
    void toTrainerSummary_shouldMapCorrectly() {
        Trainer trainer = new Trainer(TRAINER_ID, cardioType, trainerUser);

        TrainerSummary summary = mapper.toTrainerSummary(trainer);

        assertEquals(TRAINER_USERNAME, summary.username());
        assertEquals(TRAINER_FIRST_NAME, summary.firstName());
        assertEquals(TRAINER_LAST_NAME, summary.lastName());
        assertEquals(SPECIALIZATION, summary.specialization());
    }

    @Test
    void toTraineeSummary_shouldMapCorrectly() {
        Trainee trainee = new Trainee(TRAINEE_ID, null, null, traineeUser);

        TraineeSummary summary = mapper.toTraineeSummary(trainee);

        assertEquals(TRAINEE_USERNAME, summary.username());
        assertEquals(TRAINEE_FIRST_NAME, summary.firstName());
        assertEquals(TRAINEE_LAST_NAME, summary.lastName());
    }

    @Test
    void toTraineeTrainingResponse_shouldMapCorrectly() {
        Trainee trainee = new Trainee(TRAINEE_ID, null, null, traineeUser);
        Trainer trainer = new Trainer(TRAINER_ID, cardioType, trainerUser);
        Training training = new Training(TRAINING_ID, trainee, trainer,
                TRAINING_NAME, cardioType, TRAINING_DATE, TRAINING_DURATION);

        TraineeTrainingResponse response = mapper.toTraineeTrainingResponse(training);

        assertEquals(TRAINING_NAME, response.trainingName());
        assertEquals(TRAINING_DATE, response.trainingDate());
        assertEquals(SPECIALIZATION, response.trainingType());
        assertEquals(TRAINING_DURATION, response.trainingDuration());
        assertEquals(TRAINER_FIRST_NAME + " " + TRAINER_LAST_NAME, response.trainerName());
    }

    @Test
    void toTrainerTrainingResponse_shouldMapCorrectly() {
        Trainee trainee = new Trainee(TRAINEE_ID, null, null, traineeUser);
        Trainer trainer = new Trainer(TRAINER_ID, cardioType, trainerUser);
        Training training = new Training(TRAINING_ID, trainee, trainer,
                TRAINING_NAME, cardioType, TRAINING_DATE, TRAINING_DURATION);

        TrainerTrainingResponse response = mapper.toTrainerTrainingResponse(training);

        assertEquals(TRAINING_NAME, response.trainingName());
        assertEquals(TRAINEE_FIRST_NAME + " " + TRAINEE_LAST_NAME, response.traineeName());
    }
}