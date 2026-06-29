package com.dimazak.gym.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilsTest {

    private final SecurityUtils securityUtils = new SecurityUtils();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUsername_shouldReturnUsernameWhenAuthenticated() {
        setAuthentication("John.Doe");
        assertEquals("John.Doe", securityUtils.getCurrentUsername());
    }

    @Test
    void getCurrentUsername_shouldThrowWhenNotAuthenticated() {
        assertThrows(AccessDeniedException.class, () -> securityUtils.getCurrentUsername());
    }

    @Test
    void verifyOwnership_shouldPassWhenSameUser() {
        setAuthentication("John.Doe");
        assertDoesNotThrow(() -> securityUtils.verifyOwnership("John.Doe"));
    }

    @Test
    void verifyOwnership_shouldThrowWhenDifferentUser() {
        setAuthentication("John.Doe");
        assertThrows(AccessDeniedException.class,
                () -> securityUtils.verifyOwnership("Jane.Smith"));
    }

    private void setAuthentication(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, List.of()));
    }
}