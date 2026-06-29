package com.dimazak.gym.dao;

import com.dimazak.gym.model.TrainingType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainingTypeDao {

    private static final Logger log = LoggerFactory.getLogger(TrainingTypeDao.class);

    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession() {
        return entityManager.unwrap(Session.class);
    }

    public Optional<TrainingType> findById(Long id) {
        log.debug("Finding training type by id: {}", id);
        return Optional.ofNullable(getSession().get(TrainingType.class, id));
    }

    public List<TrainingType> findAll() {
        log.debug("Finding all training types");
        return getSession().createQuery("FROM TrainingType", TrainingType.class).list();
    }
}