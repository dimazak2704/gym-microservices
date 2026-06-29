package com.dimazak.gym.dao;

import com.dimazak.gym.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DaoTest
class TrainingDaoTest {

    private static final String TRAINEE_USERNAME = "John.Doe";
    private static final String TRAINER_USERNAME = "Jane.Smith";
    private static final String PASSWORD = "pass123456";
    private static final String CARDIO = "Cardio";
    private static final String YOGA = "Yoga";
    private static final LocalDate MARCH_DATE = LocalDate.of(2024, 3, 15);
    private static final LocalDate APRIL_DATE = LocalDate.of(2024, 4, 15);
    private static final LocalDate APRIL_START = LocalDate.of(2024, 4, 1);
    private static final LocalDate APRIL_END = LocalDate.of(2024, 4, 30);
    private static final int DURATION_60 = 60;
    private static final int DURATION_45 = 45;

    @Autowired private TrainingDao trainingDao;
    @Autowired private TraineeDao traineeDao;
    @Autowired private TrainerDao trainerDao;

    @PersistenceContext
    private EntityManager entityManager;

    private TrainingType cardioType;
    private TrainingType yogaType;
    private Trainee trainee;
    private Trainer trainer;

    @BeforeEach
    void setUp() {
        cardioType = new TrainingType(null, CARDIO);
        yogaType = new TrainingType(null, YOGA);
        entityManager.persist(cardioType);
        entityManager.persist(yogaType);

        User traineeUser = new User(null, "John", "Doe", TRAINEE_USERNAME, PASSWORD, true, Role.TRAINEE);
        trainee = traineeDao.save(new Trainee(null, LocalDate.of(1990, 1, 1), "Addr", traineeUser));

        User trainerUser = new User(null, "Jane", "Smith", TRAINER_USERNAME, PASSWORD, true, Role.TRAINER);
        trainer = trainerDao.save(new Trainer(null, cardioType, trainerUser));

        entityManager.flush();
    }

    @Test
    void save_shouldPersistTraining() {
        Training training = new Training(null, trainee, trainer,
                "Morning Cardio", cardioType, APRIL_DATE, DURATION_60);

        Training saved = trainingDao.save(training);

        assertNotNull(saved.getId());
        assertEquals("Morning Cardio", saved.getTrainingName());
    }

    @Test
    void findByTrainee_shouldReturnAll() {
        trainingDao.save(new Training(null, trainee, trainer, "S1", cardioType, MARCH_DATE, DURATION_60));
        trainingDao.save(new Training(null, trainee, trainer, "S2", cardioType, APRIL_DATE, DURATION_45));

        List<Training> found = trainingDao.findByTraineeWithFilters(
                TRAINEE_USERNAME, null, null, null, null);

        assertEquals(2, found.size());
    }

    @Test
    void findByTrainee_shouldFilterByDate() {
        trainingDao.save(new Training(null, trainee, trainer, "March", cardioType, MARCH_DATE, DURATION_60));
        trainingDao.save(new Training(null, trainee, trainer, "April", cardioType, APRIL_DATE, DURATION_60));

        List<Training> found = trainingDao.findByTraineeWithFilters(
                TRAINEE_USERNAME, APRIL_START, APRIL_END, null, null);

        assertEquals(1, found.size());
        assertEquals("April", found.get(0).getTrainingName());
    }

    @Test
    void findByTrainee_shouldFilterByTrainerName() {
        User trainerUser2 = new User(null, "Bob", "Jones", "Bob.Jones", PASSWORD, true, Role.TRAINER);
        Trainer trainer2 = trainerDao.save(new Trainer(null, cardioType, trainerUser2));

        trainingDao.save(new Training(null, trainee, trainer, "With Jane", cardioType, APRIL_DATE, DURATION_60));
        trainingDao.save(new Training(null, trainee, trainer2, "With Bob", cardioType, APRIL_DATE, DURATION_60));

        List<Training> found = trainingDao.findByTraineeWithFilters(
                TRAINEE_USERNAME, null, null, "Bob", null);

        assertEquals(1, found.size());
        assertEquals("With Bob", found.get(0).getTrainingName());
    }

    @Test
    void findByTrainee_shouldFilterByType() {
        trainingDao.save(new Training(null, trainee, trainer, "Cardio S", cardioType, APRIL_DATE, DURATION_60));
        trainingDao.save(new Training(null, trainee, trainer, "Yoga S", yogaType, APRIL_DATE, DURATION_45));

        List<Training> found = trainingDao.findByTraineeWithFilters(
                TRAINEE_USERNAME, null, null, null, YOGA);

        assertEquals(1, found.size());
        assertEquals("Yoga S", found.get(0).getTrainingName());
    }

    @Test
    void findByTrainee_shouldReturnEmptyWhenNoMatch() {
        trainingDao.save(new Training(null, trainee, trainer, "S1", cardioType, MARCH_DATE, DURATION_60));

        List<Training> found = trainingDao.findByTraineeWithFilters(
                TRAINEE_USERNAME, APRIL_START, APRIL_END, null, null);

        assertTrue(found.isEmpty());
    }

    @Test
    void findByTrainer_shouldReturnAll() {
        trainingDao.save(new Training(null, trainee, trainer, "Session", cardioType, APRIL_DATE, DURATION_60));

        List<Training> found = trainingDao.findByTrainerWithFilters(
                TRAINER_USERNAME, null, null, null);

        assertEquals(1, found.size());
    }

    @Test
    void findByTrainer_shouldFilterByTraineeName() {
        User traineeUser2 = new User(null, "Alice", "Brown", "Alice.Brown", PASSWORD, true, Role.TRAINEE);
        Trainee trainee2 = traineeDao.save(new Trainee(null, null, null, traineeUser2));

        trainingDao.save(new Training(null, trainee, trainer, "With John", cardioType, APRIL_DATE, DURATION_60));
        trainingDao.save(new Training(null, trainee2, trainer, "With Alice", cardioType, APRIL_DATE, DURATION_60));

        List<Training> found = trainingDao.findByTrainerWithFilters(
                TRAINER_USERNAME, null, null, "Alice");

        assertEquals(1, found.size());
        assertEquals("With Alice", found.get(0).getTrainingName());
    }

    @Test
    void findByTrainer_shouldFilterByDateRange() {
        trainingDao.save(new Training(null, trainee, trainer, "Early", cardioType, MARCH_DATE, DURATION_60));
        trainingDao.save(new Training(null, trainee, trainer, "Late", cardioType, APRIL_DATE, DURATION_60));

        List<Training> found = trainingDao.findByTrainerWithFilters(
                TRAINER_USERNAME, APRIL_START, APRIL_END, null);

        assertEquals(1, found.size());
        assertEquals("Late", found.get(0).getTrainingName());
    }
}