package com.dimazak.gym.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationEntryPointTest {

    @Mock private JsonErrorWriter errorWriter;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    @InjectMocks
    private JwtAuthenticationEntryPoint entryPoint;

    @Test
    void commence_shouldDelegateToWriterWith401() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/trainees/John.Doe");

        entryPoint.commence(request, response, new BadCredentialsException("bad"));

        verify(errorWriter).write(eq(response), eq(HttpStatus.UNAUTHORIZED),
                eq("Authentication required"));
    }
}