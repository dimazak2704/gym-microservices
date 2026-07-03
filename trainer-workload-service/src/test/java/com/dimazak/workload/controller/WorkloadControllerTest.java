package com.dimazak.workload.controller;

import com.dimazak.workload.dto.*;
import com.dimazak.workload.exception.GlobalExceptionHandler;
import com.dimazak.workload.service.WorkloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WorkloadControllerTest {

    private static final String USERNAME = "Jane.Smith";
    private static final String BASE_URL = "/api/workload";

    private MockMvc mockMvc;

    @Mock
    private WorkloadService service;

    @InjectMocks
    private WorkloadController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getSummary_shouldReturn200WithData() throws Exception {
        var months = List.of(new MonthSummary(4, 120));
        var years = List.of(new YearSummary(2024, months));
        var response = new TrainerSummaryResponse(USERNAME, "Jane", "Smith", true, years);
        when(service.getSummary(USERNAME, null, null)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/" + USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.years[0].year").value(2024))
                .andExpect(jsonPath("$.years[0].months[0].month").value(4))
                .andExpect(jsonPath("$.years[0].months[0].totalDuration").value(120));
    }

    @Test
    void getSummary_shouldReturn200WhenEmpty() throws Exception {
        var response = new TrainerSummaryResponse(USERNAME, null, null, false, List.of());
        when(service.getSummary(USERNAME, null, null)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/" + USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andExpect(jsonPath("$.years").isEmpty());
    }

    @Test
    void getSummary_shouldPassYearAndMonthParams() throws Exception {
        var response = new TrainerSummaryResponse(USERNAME, "Jane", "Smith", true, List.of());
        when(service.getSummary(USERNAME, 2024, 4)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/" + USERNAME)
                        .param("year", "2024")
                        .param("month", "4"))
                .andExpect(status().isOk());

        verify(service).getSummary(USERNAME, 2024, 4);
    }

    @Test
    void getSummary_shouldPassOnlyYearParam() throws Exception {
        var response = new TrainerSummaryResponse(USERNAME, "Jane", "Smith", true, List.of());
        when(service.getSummary(USERNAME, 2024, null)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/" + USERNAME)
                        .param("year", "2024"))
                .andExpect(status().isOk());

        verify(service).getSummary(USERNAME, 2024, null);
    }
}