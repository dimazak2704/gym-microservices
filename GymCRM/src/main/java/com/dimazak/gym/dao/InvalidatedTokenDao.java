package com.dimazak.gym.dao;

import com.dimazak.gym.model.InvalidatedToken;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public class InvalidatedTokenDao {

    private static final Logger log = LoggerFactory.getLogger(InvalidatedTokenDao.class);

    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession() {
        return entityManager.unwrap(Session.class);
    }

    public void save(InvalidatedToken token) {
        log.debug("Persisting invalidated token jti: {}", token.getJti());
        getSession().persist(token);
    }

    public boolean existsByJti(String jti) {
        Long count = getSession().createQuery(
                        "SELECT COUNT(t) FROM InvalidatedToken t WHERE t.jti = :jti", Long.class)
                .setParameter("jti", jti)
                .uniqueResult();
        return count != null && count > 0;
    }

    public int deleteExpired(Instant now) {
        int deleted = getSession().createMutationQuery(
                        "DELETE FROM InvalidatedToken t WHERE t.expiresAt < :now")
                .setParameter("now", now)
                .executeUpdate();
        if (deleted > 0) {
            log.debug("Cleaned up {} expired invalidated tokens", deleted);
        }
        return deleted;
    }
}