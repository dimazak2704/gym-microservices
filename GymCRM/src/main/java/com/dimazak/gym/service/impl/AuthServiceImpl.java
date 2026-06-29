package com.dimazak.gym.service.impl;

import com.dimazak.gym.dto.LoginResponse;
import com.dimazak.gym.exception.AuthenticationException;
import com.dimazak.gym.model.Role;
import com.dimazak.gym.security.JwtService;
import com.dimazak.gym.security.LoginAttemptService;
import com.dimazak.gym.security.SecurityUtils;
import com.dimazak.gym.security.TokenBlacklistService;
import com.dimazak.gym.service.AuthService;
import com.dimazak.gym.service.TraineeService;
import com.dimazak.gym.service.TrainerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final String ROLE_PREFIX = "ROLE_";

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;
    private final TokenBlacklistService tokenBlacklistService;
    private final SecurityUtils securityUtils;
    private final TraineeService traineeService;
    private final TrainerService trainerService;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtService jwtService,
                           LoginAttemptService loginAttemptService,
                           TokenBlacklistService tokenBlacklistService,
                           SecurityUtils securityUtils,
                           TraineeService traineeService,
                           TrainerService trainerService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.securityUtils = securityUtils;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(String username, String password) {
        log.info("Login attempt for user: {}", username);

        if (loginAttemptService.isBlocked(username)) {
            log.warn("Login blocked due to too many failed attempts for user: {}", username);
            throw new AuthenticationException(
                    "Account temporarily locked due to multiple failed login attempts. " +
                            "Try again later.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            loginAttemptService.recordSuccess(username);

            Role role = extractRole(authentication);
            String token = jwtService.generateToken(authentication.getName(), role);
            log.info("User '{}' logged in successfully", username);

            return new LoginResponse(token);
        } catch (org.springframework.security.core.AuthenticationException ex) {
            loginAttemptService.recordFailure(username);
            log.warn("Authentication failed for user: {}", username);
            throw new AuthenticationException("Invalid username or password");
        }
    }

    @Override
    @Transactional
    public void logout(String token) {
        String jti = jwtService.extractJti(token);
        tokenBlacklistService.invalidate(jti, jwtService.extractExpiration(token));
        log.info("User logged out, token invalidated");
    }

    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        log.info("Password change request for user: {}", username);
        securityUtils.verifyOwnership(username);
        verifyCurrentPassword(username, oldPassword);

        if (traineeService.existsByUsername(username)) {
            traineeService.changePassword(username, newPassword);
        } else {
            trainerService.changePassword(username, newPassword);
        }
        log.info("Password changed successfully for user: {}", username);
    }

    private void verifyCurrentPassword(String username, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
        } catch (org.springframework.security.core.AuthenticationException ex) {
            log.warn("Password verification failed for user '{}'", username);
            throw new AuthenticationException("Invalid username or password");
        }
    }

    private Role extractRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith(ROLE_PREFIX))
                .map(authority -> Role.valueOf(authority.substring(ROLE_PREFIX.length())))
                .findFirst()
                .orElseThrow(() -> new AuthenticationException("User has no valid role"));
    }
}