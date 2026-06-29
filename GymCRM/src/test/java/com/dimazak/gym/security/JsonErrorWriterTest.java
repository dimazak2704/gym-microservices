package com.dimazak.gym.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class JsonErrorWriterTest {

    private JsonErrorWriter writer;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        writer = new JsonErrorWriter(objectMapper);
    }

    @Test
    void write_shouldProduceJsonWithStatusAndMessage() throws Exception {
        HttpServletResponse response = new MockHttpServletResponse();

        writer.write(response, HttpStatus.UNAUTHORIZED, "Authentication required");

        MockHttpServletResponse mock = (MockHttpServletResponse) response;
        assertEquals(401, mock.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, mock.getContentType());
        String body = mock.getContentAsString();
        assertTrue(body.contains("\"status\":401"));
        assertTrue(body.contains("\"message\":\"Authentication required\""));
        assertTrue(body.contains("\"timestamp\""));
    }

    @Test
    void write_shouldProduce403ForForbidden() throws Exception {
        HttpServletResponse response = new MockHttpServletResponse();

        writer.write(response, HttpStatus.FORBIDDEN, "Access denied");

        MockHttpServletResponse mock = (MockHttpServletResponse) response;
        assertEquals(403, mock.getStatus());
        assertTrue(mock.getContentAsString().contains("\"message\":\"Access denied\""));
    }
}