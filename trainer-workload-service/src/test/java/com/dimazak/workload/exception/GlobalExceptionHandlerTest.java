package com.dimazak.workload.exception;

import com.dimazak.workload.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleGeneral_shouldReturn500WithGenericMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleGeneral(new RuntimeException("internal detail"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode().value());
        assertEquals("An unexpected error occurred", response.getBody().message());
        assertNotEquals("internal detail", response.getBody().message());
    }
}