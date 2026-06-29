package com.dimazak.gym.dao;

import com.dimazak.gym.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DaoTest
class TrainerDaoTest {

    private static final String TRAINER_USERNAME = "Jane.Doe";
    private static final String TRAINER_FIRST = "Jane";
    private static final String TRAINER_LAST = "Doe";
    private static final String PASSWORD = "pass123456";
    private static final String SPECIALIZATION = "Cardio";
    private static final String NON_EXISTENT = "Nobody";
    private static final String TRAINEE_USERNAME = "Trainee.One";

    @Autowired private TrainerDao trainerDao;
    @Autowired private TraineeDao traineeDao;

    @PersistenceContext
    private EntityManager entityManager;

    private TrainingType cardioType;

    @BeforeEach
    void setUp() {
        cardioType = new TrainingType(null, SPECIALIZATION);
        entityManager.persist(cardioType);
        entityManager.flush();
    }

    @Test
    void save_shouldPersistNewTrainer() {
        User user = new User(null, TRAINER_FIRST, TRAINER_LAST, TRAINER_USERNAME, PASSWORD, true, Role.TRAINER);
        Trainer saved = trainerDao.save(new Trainer(null, cardioType, user));

        assertNotNull(saved.getId());
        assertEquals(cardioType, saved.getSpecialization());
        assertEquals(Role.TRAINER, saved.getUser().getRole());
    }

    @Test
    void findByUsername_shouldReturnTrainer() {
        User user = new User(null, TRAINER_FIRST, TRAINER_LAST, TRAINER_USERNAME, PASSWORD, true, Role.TRAINER);
        trainerDao.save(new Trainer(null, cardioType, user));

        Optional<Trainer> found = trainerDao.findByUsername(TRAINER_USERNAME);

        assertTrue(found.isPresent());
        assertEquals(SPECIALIZATION, found.get().getSpecialization().getTrainingTypeName());
    }

    @Test
    void findByUsername_shouldReturnEmptyWhenNotExists() {
        assertTrue(trainerDao.findByUsername(NON_EXISTENT).isEmpty());
    }

    @Test
    void findUnassigned_shouldExcludeAssigned() {
        User u1 = new User(null, "T1", "Last", "T1.Last", PASSWORD, true, Role.TRAINER);
        User u2 = new User(null, "T2", "Last", "T2.Last", PASSWORD, true, Role.TRAINER);
        Trainer trainer1 = trainerDao.save(new Trainer(null, cardioType, u1));
        trainerDao.save(new Trainer(null, cardioType, u2));

        User traineeUser = new User(null, "Trainee", "One", TRAINEE_USERNAME, PASSWORD, true, Role.TRAINEE);
        Trainee trainee = new Trainee(null, null, null, traineeUser);
        trainee.getTrainers().add(trainer1);
        traineeDao.save(trainee);

        entityManager.flush();
        entityManager.clear();

        List<Trainer> unassigned = trainerDao.findUnassignedByTraineeUsername(TRAINEE_USERNAME);

        assertEquals(1, unassigned.size());
        assertEquals("T2.Last", unassigned.get(0).getUser().getUsername());
    }

    @Test
    void findUnassigned_shouldExcludeInactive() {
        User activeUser = new User(null, "Active", "T", "Active.T", PASSWORD, true, Role.TRAINER);
        User inactiveUser = new User(null, "Inactive", "T", "Inactive.T", PASSWORD, false, Role.TRAINER);
        trainerDao.save(new Trainer(null, cardioType, activeUser));
        trainerDao.save(new Trainer(null, cardioType, inactiveUser));

        User traineeUser = new User(null, "Solo", "T", "Solo.T", PASSWORD, true, Role.TRAINEE);
        traineeDao.save(new Trainee(null, null, null, traineeUser));

        entityManager.flush();
        entityManager.clear();

        List<Trainer> unassigned = trainerDao.findUnassignedByTraineeUsername("Solo.T");

        assertEquals(1, unassigned.size());
        assertEquals("Active.T", unassigned.get(0).getUser().getUsername());
    }

    @Test
    void findUnassigned_shouldReturnAllWhenNoneAssigned() {
        User u1 = new User(null, "T1", "A", "T1.A", PASSWORD, true, Role.TRAINER);
        User u2 = new User(null, "T2", "B", "T2.B", PASSWORD, true, Role.TRAINER);
        trainerDao.save(new Trainer(null, cardioType, u1));
        trainerDao.save(new Trainer(null, cardioType, u2));

        User traineeUser = new User(null, "Solo", "Trainee", "Solo.Trainee", PASSWORD, true, Role.TRAINEE);
        traineeDao.save(new Trainee(null, null, null, traineeUser));

        entityManager.flush();
        entityManager.clear();

        List<Trainer> unassigned = trainerDao.findUnassignedByTraineeUsername("Solo.Trainee");

        assertEquals(2, unassigned.size());
    }
}