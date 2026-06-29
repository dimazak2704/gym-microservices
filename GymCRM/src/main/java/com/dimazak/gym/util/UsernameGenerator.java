package com.dimazak.gym.util;

import com.dimazak.gym.dao.UserDao;
import com.dimazak.gym.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UsernameGenerator {

    private static final Logger log = LoggerFactory.getLogger(UsernameGenerator.class);

    private final UserDao userDao;

    public UsernameGenerator(UserDao userDao) {
        this.userDao = userDao;
    }

    public String generateUsername(String firstName, String lastName) {
        String baseUsername = firstName + "." + lastName;
        List<User> allUsers = userDao.findAll();

        long count = allUsers.stream()
                .filter(u -> u.getUsername() != null && (
                        u.getUsername().equals(baseUsername) ||
                                u.getUsername().matches(baseUsername + "\\d+")))
                .count();

        String username;
        if (count == 0) {
            username = baseUsername;
        } else {
            username = baseUsername + count;
        }

        log.info("Generated username: {}", username);
        return username;
    }
}