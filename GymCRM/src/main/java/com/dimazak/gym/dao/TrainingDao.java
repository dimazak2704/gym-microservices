package com.dimazak.gym.dao;

import com.dimazak.gym.model.Training;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class TrainingDao {

    private static final Logger log = LoggerFactory.getLogger(TrainingDao.class);

    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession() {
        return entityManager.unwrap(Session.class);
    }

    public Training save(Training training) {
        Session session = getSession();
        if (training.getId() == null) {
            session.persist(training);
            log.debug("Persisted new training with id: {}", training.getId());
        } else {
            training = session.merge(training);
            log.debug("Merged training with id: {}", training.getId());
        }
        return training;
    }

    public List<Training> findByTraineeWithFilters(
            String traineeUsername, LocalDate fromDate, LocalDate toDate,
            String trainerName, String trainingTypeName) {

        log.debug("Finding trainings for trainee '{}'", traineeUsername);

        StringBuilder hql = new StringBuilder("""
                FROM Training t
                JOIN FETCH t.trainer tr JOIN FETCH tr.user
                JOIN FETCH t.trainingType
                WHERE t.trainee.user.username = :username
                """);

        if (fromDate != null) hql.append(" AND t.trainingDate >= :fromDate");
        if (toDate != null) hql.append(" AND t.trainingDate <= :toDate");
        if (trainerName != null && !trainerName.isBlank())
            hql.append(" AND tr.user.firstName LIKE :trainerName");
        if (trainingTypeName != null && !trainingTypeName.isBlank())
            hql.append(" AND t.trainingType.trainingTypeName = :typeName");

        Query<Training> query = getSession().createQuery(hql.toString(), Training.class);
        query.setParameter("username", traineeUsername);

        if (fromDate != null) query.setParameter("fromDate", fromDate);
        if (toDate != null) query.setParameter("toDate", toDate);
        if (trainerName != null && !trainerName.isBlank())
            query.setParameter("trainerName", "%" + trainerName + "%");
        if (trainingTypeName != null && !trainingTypeName.isBlank())
            query.setParameter("typeName", trainingTypeName);

        return query.list();
    }

    public List<Training> findByTrainerWithFilters(
            String trainerUsername, LocalDate fromDate, LocalDate toDate,
            String traineeName) {

        log.debug("Finding trainings for trainer '{}'", trainerUsername);

        StringBuilder hql = new StringBuilder("""
                FROM Training t
                JOIN FETCH t.trainee te JOIN FETCH te.user
                JOIN FETCH t.trainingType
                WHERE t.trainer.user.username = :username
                """);

        if (fromDate != null) hql.append(" AND t.trainingDate >= :fromDate");
        if (toDate != null) hql.append(" AND t.trainingDate <= :toDate");
        if (traineeName != null && !traineeName.isBlank())
            hql.append(" AND te.user.firstName LIKE :traineeName");

        Query<Training> query = getSession().createQuery(hql.toString(), Training.class);
        query.setParameter("username", trainerUsername);

        if (fromDate != null) query.setParameter("fromDate", fromDate);
        if (toDate != null) query.setParameter("toDate", toDate);
        if (traineeName != null && !traineeName.isBlank())
            query.setParameter("traineeName", "%" + traineeName + "%");

        return query.list();
    }
}