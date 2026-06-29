package com.dimazak.gym.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAccessDeniedHandlerTest {

    @Mock private JsonErrorWriter errorWriter;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    @InjectMocks
    private JwtAccessDeniedHandler handler;

    @Test
    void handle_shouldDelegateToWriterWith403() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/trainees/John.Doe");

        handler.handle(request, response, new AccessDeniedException("denied"));

        verify(errorWriter).write(eq(response), eq(HttpStatus.FORBIDDEN), eq("Access denied"));
    }
}