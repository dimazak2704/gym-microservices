package com.dimazak.gym.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordGeneratorTest {

    private static final int EXPECTED_LENGTH = 10;
    private static final String ALPHANUMERIC_PATTERN = "[A-Za-z0-9]+";

    private final PasswordGenerator passwordGenerator = new PasswordGenerator();

    @Test
    void generatePassword_shouldReturnCorrectLength() {
        assertEquals(EXPECTED_LENGTH, passwordGenerator.generatePassword().length());
    }

    @Test
    void generatePassword_shouldGenerateDifferentPasswords() {
        String first = passwordGenerator.generatePassword();
        String second = passwordGenerator.generatePassword();

        assertNotEquals(first, second);
    }

    @Test
    void generatePassword_shouldContainOnlyAlphanumeric() {
        String password = passwordGenerator.generatePassword();

        assertTrue(password.matches(ALPHANUMERIC_PATTERN));
    }

    @Test
    void generatePassword_shouldAlwaysReturnNonEmpty() {
        for (int i = 0; i < 100; i++) {
            assertFalse(passwordGenerator.generatePassword().isEmpty());
        }
    }
}