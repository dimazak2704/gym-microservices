package com.dimazak.gym.controller;

import com.dimazak.gym.dto.ChangePasswordRequest;
import com.dimazak.gym.dto.LoginRequest;
import com.dimazak.gym.dto.LoginResponse;
import com.dimazak.gym.exception.AuthenticationException;
import com.dimazak.gym.exception.GlobalExceptionHandler;
import com.dimazak.gym.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private static final String USERNAME = "John.Doe";
    private static final String PASSWORD = "pass123456";
    private static final String NEW_PASSWORD = "newPass1234";
    private static final String TOKEN = "header.payload.sig";
    private static final String LOGIN_URL = "/api/users/login";
    private static final String LOGOUT_URL = "/api/users/logout";
    private static final String PASSWORD_URL = "/api/users/password";

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private AuthService authService;

    @InjectMocks
    private AuthController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ==================== /login ====================

    @Test
    void login_shouldReturnTokenForValidCredentials() throws Exception {
        LoginRequest request = new LoginRequest(USERNAME, PASSWORD);
        when(authService.login(USERNAME, PASSWORD)).thenReturn(new LoginResponse(TOKEN));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(TOKEN));
    }

    @Test
    void login_shouldReturn401WhenInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest(USERNAME, "wrong");
        when(authService.login(USERNAME, "wrong"))
                .thenThrow(new AuthenticationException("Invalid username or password"));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void login_shouldReturn400WhenUsernameBlank() throws Exception {
        LoginRequest request = new LoginRequest("", PASSWORD);

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void login_shouldReturn400WhenPasswordBlank() throws Exception {
        LoginRequest request = new LoginRequest(USERNAME, "");

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== /logout ====================

    @Test
    void logout_shouldExtractTokenFromBearerHeader() throws Exception {
        mockMvc.perform(post(LOGOUT_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN))
                .andExpect(status().isOk());

        verify(authService).logout(TOKEN);
    }

    @Test
    void logout_shouldReturn400WhenAuthHeaderMissing() throws Exception {
        mockMvc.perform(post(LOGOUT_URL))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    // ==================== /password ====================

    @Test
    void changePassword_shouldDelegateToAuthService() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(USERNAME, PASSWORD, NEW_PASSWORD);

        mockMvc.perform(put(PASSWORD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService).changePassword(USERNAME, PASSWORD, NEW_PASSWORD);
    }

    @Test
    void changePassword_shouldReturn401WhenOldPasswordWrong() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(USERNAME, "wrong", NEW_PASSWORD);
        doThrow(new AuthenticationException("Invalid username or password"))
                .when(authService).changePassword(USERNAME, "wrong", NEW_PASSWORD);

        mockMvc.perform(put(PASSWORD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePassword_shouldReturn400WhenFieldsMissing() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("", "", "");

        mockMvc.perform(put(PASSWORD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }
}