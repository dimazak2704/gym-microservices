package com.dimazak.gym.controller;

import com.dimazak.gym.dto.TrainingTypeResponse;
import com.dimazak.gym.service.TrainingTypeService;
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
class TrainingTypeControllerTest {

    private static final String BASE_URL = "/api/training-types";
    private static final Long CARDIO_ID = 1L;
    private static final Long STRENGTH_ID = 2L;
    private static final Long YOGA_ID = 3L;
    private static final String CARDIO = "Cardio";
    private static final String STRENGTH = "Strength";
    private static final String YOGA = "Yoga";

    private MockMvc mockMvc;

    @Mock private TrainingTypeService trainingTypeService;

    @InjectMocks
    private TrainingTypeController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getAllTypes_shouldReturn200() throws Exception {
        List<TrainingTypeResponse> types = List.of(
                new TrainingTypeResponse(CARDIO_ID, CARDIO),
                new TrainingTypeResponse(STRENGTH_ID, STRENGTH),
                new TrainingTypeResponse(YOGA_ID, YOGA));
        when(trainingTypeService.getAllTrainingTypes()).thenReturn(types);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(CARDIO_ID))
                .andExpect(jsonPath("$[0].trainingType").value(CARDIO))
                .andExpect(jsonPath("$[2].trainingType").value(YOGA));
    }

    @Test
    void getAllTypes_shouldReturnEmptyList() throws Exception {
        when(trainingTypeService.getAllTrainingTypes()).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}