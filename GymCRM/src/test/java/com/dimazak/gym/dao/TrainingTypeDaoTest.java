package com.dimazak.gym.dao;

import com.dimazak.gym.model.TrainingType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DaoTest
class TrainingTypeDaoTest {

    private static final String CARDIO = "Cardio";
    private static final String STRENGTH = "Strength";
    private static final Long NON_EXISTENT_ID = 999L;

    @Autowired private TrainingTypeDao trainingTypeDao;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void findById_shouldReturnTypeWhenExists() {
        TrainingType type = new TrainingType(null, CARDIO);
        entityManager.persist(type);
        entityManager.flush();

        Optional<TrainingType> found = trainingTypeDao.findById(type.getId());

        assertTrue(found.isPresent());
        assertEquals(CARDIO, found.get().getTrainingTypeName());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotExists() {
        assertTrue(trainingTypeDao.findById(NON_EXISTENT_ID).isEmpty());
    }

    @Test
    void findAll_shouldReturnAllTypes() {
        entityManager.persist(new TrainingType(null, CARDIO));
        entityManager.persist(new TrainingType(null, STRENGTH));
        entityManager.flush();

        List<TrainingType> all = trainingTypeDao.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void findAll_shouldReturnEmptyWhenNone() {
        List<TrainingType> all = trainingTypeDao.findAll();

        assertTrue(all.isEmpty());
    }
}