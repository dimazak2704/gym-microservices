package com.dimazak.gym.service;

import com.dimazak.gym.dto.LoginResponse;
import com.dimazak.gym.exception.AuthenticationException;
import com.dimazak.gym.model.Role;
import com.dimazak.gym.security.JwtService;
import com.dimazak.gym.security.LoginAttemptService;
import com.dimazak.gym.security.SecurityUtils;
import com.dimazak.gym.security.TokenBlacklistService;
import com.dimazak.gym.service.TraineeService;
import com.dimazak.gym.service.TrainerService;
import com.dimazak.gym.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String USERNAME = "John.Doe";
    private static final String RAW_PASSWORD = "pass123456";
    private static final String NEW_PASSWORD = "newPass1234";
    private static final String TOKEN = "header.payload.sig";
    private static final String JTI = "jti-uuid";

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private LoginAttemptService loginAttemptService;
    @Mock private TokenBlacklistService tokenBlacklistService;
    @Mock private SecurityUtils securityUtils;
    @Mock private TraineeService traineeService;
    @Mock private TrainerService trainerService;

    @InjectMocks private AuthServiceImpl authService;

    private Authentication buildAuthentication(Role role) {
        return new UsernamePasswordAuthenticationToken(
                USERNAME, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
    }

    // ==================== login ====================

    @Test
    void login_shouldReturnTokenWhenCredentialsValid() {
        when(loginAttemptService.isBlocked(USERNAME)).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenReturn(buildAuthentication(Role.TRAINEE));
        when(jwtService.generateToken(USERNAME, Role.TRAINEE)).thenReturn(TOKEN);

        LoginResponse response = authService.login(USERNAME, RAW_PASSWORD);

        assertEquals(TOKEN, response.token());
        verify(loginAttemptService).recordSuccess(USERNAME);
        verify(loginAttemptService, never()).recordFailure(any());
    }

    @Test
    void login_shouldThrowWhenBlocked() {
        when(loginAttemptService.isBlocked(USERNAME)).thenReturn(true);

        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authService.login(USERNAME, RAW_PASSWORD));
        assertTrue(ex.getMessage().toLowerCase().contains("locked"));
        verifyNoInteractions(authenticationManager, jwtService);
    }

    @Test
    void login_shouldRecordFailureAndThrowWhenCredentialsInvalid() {
        when(loginAttemptService.isBlocked(USERNAME)).thenReturn(false);
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(AuthenticationException.class,
                () -> authService.login(USERNAME, RAW_PASSWORD));
        verify(loginAttemptService).recordFailure(USERNAME);
        verify(loginAttemptService, never()).recordSuccess(any());
        verifyNoInteractions(jwtService);
    }

    @Test
    void login_shouldExtractTrainerRoleCorrectly() {
        when(loginAttemptService.isBlocked(USERNAME)).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenReturn(buildAuthentication(Role.TRAINER));
        when(jwtService.generateToken(USERNAME, Role.TRAINER)).thenReturn(TOKEN);

        LoginResponse response = authService.login(USERNAME, RAW_PASSWORD);

        assertEquals(TOKEN, response.token());
        verify(jwtService).generateToken(USERNAME, Role.TRAINER);
    }

    // ==================== logout ====================

    @Test
    void logout_shouldInvalidateToken() {
        Instant exp = Instant.now().plusSeconds(60);
        when(jwtService.extractJti(TOKEN)).thenReturn(JTI);
        when(jwtService.extractExpiration(TOKEN)).thenReturn(exp);

        authService.logout(TOKEN);

        verify(tokenBlacklistService).invalidate(JTI, exp);
    }

    // ==================== changePassword ====================

    @Test
    void changePassword_shouldVerifyOwnershipAndDelegateToTraineeService() {
        doNothing().when(securityUtils).verifyOwnership(USERNAME);
        when(authenticationManager.authenticate(any())).thenReturn(buildAuthentication(Role.TRAINEE));
        when(traineeService.existsByUsername(USERNAME)).thenReturn(true);

        authService.changePassword(USERNAME, RAW_PASSWORD, NEW_PASSWORD);

        verify(securityUtils).verifyOwnership(USERNAME);
        verify(traineeService).changePassword(USERNAME, NEW_PASSWORD);
        verify(trainerService, never()).changePassword(any(), any());
    }

    @Test
    void changePassword_shouldDelegateToTrainerService() {
        doNothing().when(securityUtils).verifyOwnership(USERNAME);
        when(authenticationManager.authenticate(any())).thenReturn(buildAuthentication(Role.TRAINER));
        when(traineeService.existsByUsername(USERNAME)).thenReturn(false);

        authService.changePassword(USERNAME, RAW_PASSWORD, NEW_PASSWORD);

        verify(trainerService).changePassword(USERNAME, NEW_PASSWORD);
        verify(traineeService, never()).changePassword(any(), any());
    }

    @Test
    void changePassword_shouldThrowWhenOldPasswordWrong() {
        doNothing().when(securityUtils).verifyOwnership(USERNAME);
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(AuthenticationException.class,
                () -> authService.changePassword(USERNAME, RAW_PASSWORD, NEW_PASSWORD));
        verify(traineeService, never()).changePassword(any(), any());
        verify(trainerService, never()).changePassword(any(), any());
    }

    @Test
    void changePassword_shouldThrowWhenNotOwner() {
        doThrow(new org.springframework.security.access.AccessDeniedException("Access denied"))
                .when(securityUtils).verifyOwnership(USERNAME);

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> authService.changePassword(USERNAME, RAW_PASSWORD, NEW_PASSWORD));
        verifyNoInteractions(authenticationManager);
    }
}