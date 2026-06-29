package com.dimazak.gym.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginAttemptServiceTest {

    private static final String USERNAME = "John.Doe";

    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        service = new LoginAttemptService();
    }

    @Test
    void isBlocked_shouldReturnFalseForUnknownUser() {
        assertFalse(service.isBlocked(USERNAME));
    }

    @Test
    void isBlocked_shouldReturnFalseAfterFewFailures() {
        service.recordFailure(USERNAME);
        service.recordFailure(USERNAME);

        assertFalse(service.isBlocked(USERNAME));
    }

    @Test
    void isBlocked_shouldReturnTrueAfterThreeFailures() {
        service.recordFailure(USERNAME);
        service.recordFailure(USERNAME);
        service.recordFailure(USERNAME);

        assertTrue(service.isBlocked(USERNAME));
    }

    @Test
    void recordSuccess_shouldResetCounter() {
        service.recordFailure(USERNAME);
        service.recordFailure(USERNAME);
        service.recordFailure(USERNAME);
        assertTrue(service.isBlocked(USERNAME));

        service.recordSuccess(USERNAME);

        assertFalse(service.isBlocked(USERNAME));
    }

    @Test
    void counter_shouldBeIndependentPerUser() {
        service.recordFailure("user1");
        service.recordFailure("user1");
        service.recordFailure("user1");

        assertTrue(service.isBlocked("user1"));
        assertFalse(service.isBlocked("user2"));
    }
}