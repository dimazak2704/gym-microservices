package com.dimazak.gym.dao;

import com.dimazak.gym.model.InvalidatedToken;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DaoTest
class InvalidatedTokenDaoTest {

    private static final String JTI_1 = "jti-1111";
    private static final String JTI_2 = "jti-2222";

    @Autowired
    private InvalidatedTokenDao invalidatedTokenDao;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void save_shouldPersistToken() {
        Instant expires = Instant.now().plusSeconds(3600);

        invalidatedTokenDao.save(new InvalidatedToken(JTI_1, expires));
        entityManager.flush();

        assertTrue(invalidatedTokenDao.existsByJti(JTI_1));
    }

    @Test
    void existsByJti_shouldReturnFalseWhenAbsent() {
        assertFalse(invalidatedTokenDao.existsByJti("non-existent-jti"));
    }

    @Test
    void deleteExpired_shouldRemoveOnlyExpired() {
        Instant past = Instant.now().minusSeconds(3600);
        Instant future = Instant.now().plusSeconds(3600);

        invalidatedTokenDao.save(new InvalidatedToken(JTI_1, past));
        invalidatedTokenDao.save(new InvalidatedToken(JTI_2, future));
        entityManager.flush();

        int removed = invalidatedTokenDao.deleteExpired(Instant.now());

        assertEquals(1, removed);
        assertFalse(invalidatedTokenDao.existsByJti(JTI_1));
        assertTrue(invalidatedTokenDao.existsByJti(JTI_2));
    }

    @Test
    void deleteExpired_shouldReturnZeroWhenNoneExpired() {
        invalidatedTokenDao.save(new InvalidatedToken(JTI_1, Instant.now().plusSeconds(3600)));
        entityManager.flush();

        int removed = invalidatedTokenDao.deleteExpired(Instant.now());

        assertEquals(0, removed);
    }
}