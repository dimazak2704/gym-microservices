package com.dimazak.gym.controller;

import com.dimazak.gym.dto.ChangePasswordRequest;
import com.dimazak.gym.dto.LoginRequest;
import com.dimazak.gym.dto.LoginResponse;
import com.dimazak.gym.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Authentication", description = "Login, logout and password management")
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate and receive a JWT token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request.username(), request.password()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalidate the current JWT token")
    public ResponseEntity<Void> logout(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        authService.logout(extractToken(authHeader));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/password")
    @Operation(summary = "Change password",
            description = "Change user password (requires current password)")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(
                request.username(), request.oldPassword(), request.newPassword());
        return ResponseEntity.ok().build();
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return authHeader;
    }
}