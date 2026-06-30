package com.dimazak.workload.controller;

import com.dimazak.workload.dto.ActionType;
import com.dimazak.workload.dto.TrainerSummaryResponse;
import com.dimazak.workload.dto.WorkloadRequest;
import com.dimazak.workload.exception.GlobalExceptionHandler;
import com.dimazak.workload.service.WorkloadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WorkloadControllerTest {

    private static final String USERNAME = "Jane.Smith";
    private static final String BASE_URL = "/api/workload";

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock private WorkloadService service;

    @InjectMocks private WorkloadController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    private WorkloadRequest validRequest() {
        return new WorkloadRequest(USERNAME, "Jane", "Smith", true,
                LocalDate.of(2024, 4, 1), 60, ActionType.ADD);
    }

    @Test
    void updateWorkload_shouldReturn200() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk());

        verify(service).processWorkload(any(WorkloadRequest.class));
    }

    @Test
    void updateWorkload_shouldReturn400WhenUsernameBlank() throws Exception {
        WorkloadRequest bad = new WorkloadRequest("", "Jane", "Smith", true,
                LocalDate.of(2024, 4, 1), 60, ActionType.ADD);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void updateWorkload_shouldReturn400WhenDurationNegative() throws Exception {
        WorkloadRequest bad = new WorkloadRequest(USERNAME, "Jane", "Smith", true,
                LocalDate.of(2024, 4, 1), -5, ActionType.ADD);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateWorkload_shouldReturn400WhenActionTypeNull() throws Exception {
        String json = """
                {"trainerUsername":"Jane.Smith","trainerFirstName":"Jane",
                 "trainerLastName":"Smith","isActive":true,
                 "trainingDate":"2024-04-01","trainingDuration":60}
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSummary_shouldReturn200() throws Exception {
        when(service.getSummary(USERNAME, null, null)).thenReturn(
                new TrainerSummaryResponse(USERNAME, "Jane", "Smith", true, List.of()));

        mockMvc.perform(get(BASE_URL + "/" + USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(USERNAME));
    }
}