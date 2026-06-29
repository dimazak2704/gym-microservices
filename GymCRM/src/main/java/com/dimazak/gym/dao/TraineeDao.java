package com.dimazak.gym.dao;

import com.dimazak.gym.model.Trainee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TraineeDao {

    private static final Logger log = LoggerFactory.getLogger(TraineeDao.class);

    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession() {
        return entityManager.unwrap(Session.class);
    }

    public Trainee save(Trainee trainee) {
        Session session = getSession();
        if (trainee.getId() == null) {
            session.persist(trainee);
            log.debug("Persisted new trainee with id: {}", trainee.getId());
        } else {
            trainee = session.merge(trainee);
            log.debug("Merged trainee with id: {}", trainee.getId());
        }
        return trainee;
    }

    public Optional<Trainee> findByUsername(String username) {
        log.debug("Finding trainee by username: {}", username);
        return getSession().createQuery(
                        "FROM Trainee t JOIN FETCH t.user WHERE t.user.username = :username",
                        Trainee.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    public void delete(Trainee trainee) {
        log.debug("Deleting trainee with id: {}", trainee.getId());
        getSession().remove(trainee);
    }

    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }
}