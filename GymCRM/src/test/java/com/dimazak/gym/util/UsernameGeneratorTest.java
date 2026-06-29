package com.dimazak.gym.util;

import com.dimazak.gym.dao.UserDao;
import com.dimazak.gym.model.Role;
import com.dimazak.gym.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsernameGeneratorTest {

    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Smith";
    private static final String BASE_USERNAME = "John.Smith";
    private static final String USERNAME_WITH_1 = "John.Smith1";
    private static final String USERNAME_WITH_2 = "John.Smith2";
    private static final String PASSWORD = "p";

    @Mock private UserDao userDao;

    @InjectMocks
    private UsernameGenerator usernameGenerator;

    @Test
    void generateUsername_shouldReturnBaseWhenNoConflict() {
        when(userDao.findAll()).thenReturn(Collections.emptyList());

        assertEquals(BASE_USERNAME, usernameGenerator.generateUsername(FIRST_NAME, LAST_NAME));
    }

    @Test
    void generateUsername_shouldAppendNumberOnConflict() {
        User existing = new User(1L, FIRST_NAME, LAST_NAME, BASE_USERNAME, PASSWORD, true, Role.TRAINEE);
        when(userDao.findAll()).thenReturn(List.of(existing));

        assertEquals(USERNAME_WITH_1, usernameGenerator.generateUsername(FIRST_NAME, LAST_NAME));
    }

    @Test
    void generateUsername_shouldIncrementOnMultipleConflicts() {
        User u1 = new User(1L, FIRST_NAME, LAST_NAME, BASE_USERNAME, PASSWORD, true, Role.TRAINEE);
        User u2 = new User(2L, FIRST_NAME, LAST_NAME, USERNAME_WITH_1, PASSWORD, true, Role.TRAINEE);
        when(userDao.findAll()).thenReturn(List.of(u1, u2));

        assertEquals(USERNAME_WITH_2, usernameGenerator.generateUsername(FIRST_NAME, LAST_NAME));
    }

    @Test
    void generateUsername_shouldNotConflictWithDifferentBase() {
        User other = new User(1L, "Jane", "Doe", "Jane.Doe", PASSWORD, true, Role.TRAINER);
        when(userDao.findAll()).thenReturn(List.of(other));

        assertEquals(BASE_USERNAME, usernameGenerator.generateUsername(FIRST_NAME, LAST_NAME));
    }
}