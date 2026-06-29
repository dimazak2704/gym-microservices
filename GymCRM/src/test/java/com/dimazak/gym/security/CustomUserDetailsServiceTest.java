package com.dimazak.gym.security;

import com.dimazak.gym.dao.UserDao;
import com.dimazak.gym.model.Role;
import com.dimazak.gym.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    private static final String USERNAME = "John.Doe";
    private static final String ENCODED_PASSWORD = "$2a$10$encoded";

    @Mock private UserDao userDao;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    void loadUserByUsername_shouldReturnUserDetailsForTrainee() {
        User user = new User(1L, "John", "Doe", USERNAME, ENCODED_PASSWORD, true, Role.TRAINEE);
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername(USERNAME);

        assertEquals(USERNAME, details.getUsername());
        assertEquals(ENCODED_PASSWORD, details.getPassword());
        assertTrue(details.isEnabled());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TRAINEE")));
    }

    @Test
    void loadUserByUsername_shouldReflectInactiveUser() {
        User user = new User(1L, "John", "Doe", USERNAME, ENCODED_PASSWORD, false, Role.TRAINER);
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername(USERNAME);

        assertFalse(details.isEnabled());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TRAINER")));
    }

    @Test
    void loadUserByUsername_shouldThrowWhenNotFound() {
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername(USERNAME));
    }
}