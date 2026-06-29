package com.dimazak.gym.dao;

import com.dimazak.gym.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession() {
        return entityManager.unwrap(Session.class);
    }

    public User save(User user) {
        Session session = getSession();
        if (user.getId() == null) {
            session.persist(user);
            log.debug("Persisted new user with id: {}", user.getId());
        } else {
            user = session.merge(user);
            log.debug("Merged user with id: {}", user.getId());
        }
        return user;
    }

    public Optional<User> findByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        return getSession().createQuery(
                        "FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    public List<User> findAll() {
        log.debug("Finding all users");
        return getSession().createQuery("FROM User", User.class).list();
    }

    public long countActiveUsers() {
        log.debug("Counting active users");
        return getSession().createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.isActive = true", Long.class)
                .uniqueResult();
    }
}