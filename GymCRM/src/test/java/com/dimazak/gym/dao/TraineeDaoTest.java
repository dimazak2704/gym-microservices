package com.dimazak.gym.dao;

import com.dimazak.gym.model.Role;
import com.dimazak.gym.model.Trainee;
import com.dimazak.gym.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DaoTest
class TraineeDaoTest {

    private static final String FIRST_NAME = "Alice";
    private static final String LAST_NAME = "Johnson";
    private static final String USERNAME = "Alice.Johnson";
    private static final String PASSWORD = "pass123456";
    private static final LocalDate BIRTH_DATE = LocalDate.of(1990, 5, 15);
    private static final String ADDRESS = "123 Main St";
    private static final String NON_EXISTENT = "NonExistent";

    @Autowired
    private TraineeDao traineeDao;

    @Test
    void save_shouldPersistNewTrainee() {
        User user = new User(null, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true, Role.TRAINEE);
        Trainee trainee = new Trainee(null, BIRTH_DATE, ADDRESS, user);

        Trainee saved = traineeDao.save(trainee);

        assertNotNull(saved.getId());
        assertEquals(USERNAME, saved.getUser().getUsername());
        assertEquals(Role.TRAINEE, saved.getUser().getRole());
    }

    @Test
    void save_shouldMergeExistingTrainee() {
        User user = new User(null, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true, Role.TRAINEE);
        Trainee saved = traineeDao.save(new Trainee(null, BIRTH_DATE, ADDRESS, user));

        saved.setAddress("New Address");
        Trainee merged = traineeDao.save(saved);

        assertEquals(saved.getId(), merged.getId());
        assertEquals("New Address", merged.getAddress());
    }

    @Test
    void findByUsername_shouldReturnTrainee() {
        User user = new User(null, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true, Role.TRAINEE);
        traineeDao.save(new Trainee(null, BIRTH_DATE, ADDRESS, user));

        Optional<Trainee> found = traineeDao.findByUsername(USERNAME);

        assertTrue(found.isPresent());
        assertEquals(USERNAME, found.get().getUser().getUsername());
    }

    @Test
    void findByUsername_shouldReturnEmptyWhenNotExists() {
        assertTrue(traineeDao.findByUsername(NON_EXISTENT).isEmpty());
    }

    @Test
    void existsByUsername_shouldReturnTrue() {
        User user = new User(null, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true, Role.TRAINEE);
        traineeDao.save(new Trainee(null, null, null, user));

        assertTrue(traineeDao.existsByUsername(USERNAME));
    }

    @Test
    void existsByUsername_shouldReturnFalse() {
        assertFalse(traineeDao.existsByUsername(NON_EXISTENT));
    }

    @Test
    void delete_shouldRemoveTrainee() {
        User user = new User(null, "Del", "Me", "Del.Me", PASSWORD, true, Role.TRAINEE);
        Trainee saved = traineeDao.save(new Trainee(null, null, null, user));

        traineeDao.delete(saved);

        assertTrue(traineeDao.findByUsername("Del.Me").isEmpty());
    }

    @Test
    void save_shouldAllowNullOptionalFields() {
        User user = new User(null, "Min", "Data", "Min.Data", PASSWORD, true, Role.TRAINEE);
        Trainee saved = traineeDao.save(new Trainee(null, null, null, user));

        assertNotNull(saved.getId());
        assertNull(saved.getDateOfBirth());
        assertNull(saved.getAddress());
    }
}