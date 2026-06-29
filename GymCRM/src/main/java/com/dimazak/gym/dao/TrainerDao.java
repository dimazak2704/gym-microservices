package com.dimazak.gym.dao;

import com.dimazak.gym.model.Trainer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainerDao {

    private static final Logger log = LoggerFactory.getLogger(TrainerDao.class);

    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession() {
        return entityManager.unwrap(Session.class);
    }

    public Trainer save(Trainer trainer) {
        Session session = getSession();
        if (trainer.getId() == null) {
            session.persist(trainer);
            log.debug("Persisted new trainer with id: {}", trainer.getId());
        } else {
            trainer = session.merge(trainer);
            log.debug("Merged trainer with id: {}", trainer.getId());
        }
        return trainer;
    }

    public Optional<Trainer> findByUsername(String username) {
        log.debug("Finding trainer by username: {}", username);
        return getSession().createQuery(
                        "FROM Trainer t JOIN FETCH t.user JOIN FETCH t.specialization WHERE t.user.username = :username",
                        Trainer.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    public List<Trainer> findUnassignedByTraineeUsername(String traineeUsername) {
        log.debug("Finding unassigned trainers for trainee: {}", traineeUsername);
        return getSession().createQuery(
                        """
                        FROM Trainer t JOIN FETCH t.user JOIN FETCH t.specialization
                        WHERE t.user.isActive = true
                        AND t NOT IN (
                            SELECT tr FROM Trainee te JOIN te.trainers tr
                            WHERE te.user.username = :username
                        )
                        """, Trainer.class)
                .setParameter("username", traineeUsername)
                .list();
    }
}