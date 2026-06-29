package com.dimazak.gym.controller;

import com.dimazak.gym.dto.*;
import com.dimazak.gym.exception.GlobalExceptionHandler;
import com.dimazak.gym.exception.ValidationException;
import com.dimazak.gym.mapper.EntityMapper;
import com.dimazak.gym.model.*;
import com.dimazak.gym.security.JwtService;
import com.dimazak.gym.service.TrainerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TrainerControllerTest {

    private static final String USERNAME = "Jane.Smith";
    private static final String PASSWORD = "trainerPass";
    private static final String FIRST_NAME = "Jane";
    private static final String LAST_NAME = "Smith";
    private static final String SPECIALIZATION = "Cardio";
    private static final Long SPECIALIZATION_ID = 1L;
    private static final String TRAINEE_USERNAME = "Alice.Brown";
    private static final String TRAINEE_FIRST_NAME = "Alice";
    private static final String TRAINEE_LAST_NAME = "Brown";
    private static final String TRAINING_NAME = "Cardio Session";
    private static final LocalDate TRAINING_DATE = LocalDate.of(2024, 5, 1);
    private static final int TRAINING_DURATION = 45;
    private static final String BASE_URL = "/api/trainers";

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock private TrainerService trainerService;
    @Mock private JwtService jwtService;
    @Spy private EntityMapper mapper;

    @InjectMocks
    private TrainerController controller;

    private Trainer testTrainer;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        TrainingType type = new TrainingType(SPECIALIZATION_ID, SPECIALIZATION);
        User user = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true, Role.TRAINER);
        testTrainer = new Trainer(1L, type, user);
        testTrainer.setTrainees(List.of());
    }

    // ==================== Register ====================

    @Test
    void register_shouldReturn201() throws Exception {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest(
                FIRST_NAME, LAST_NAME, SPECIALIZATION_ID);
        when(trainerService.createTrainer(anyString(), anyString(), anyLong()))
                .thenReturn(testTrainer);
        when(jwtService.generateToken(USERNAME, Role.TRAINER)).thenReturn("test.jwt.token");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andExpect(jsonPath("$.password").value(PASSWORD))
                .andExpect(jsonPath("$.token").value("test.jwt.token"));
    }

    @Test
    void register_shouldReturn400WhenSpecializationNull() throws Exception {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest(
                FIRST_NAME, LAST_NAME, null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn400WhenFirstNameBlank() throws Exception {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest(
                "", LAST_NAME, SPECIALIZATION_ID);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== Get profile ====================

    @Test
    void getProfile_shouldReturn200() throws Exception {
        User traineeUser = new User(3L, TRAINEE_FIRST_NAME, TRAINEE_LAST_NAME,
                TRAINEE_USERNAME, "p", true, Role.TRAINEE);
        Trainee trainee = new Trainee(1L, null, null, traineeUser);
        testTrainer.setTrainees(List.of(trainee));
        when(trainerService.getProfileByUsername(USERNAME)).thenReturn(testTrainer);

        mockMvc.perform(get(BASE_URL + "/" + USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value(FIRST_NAME))
                .andExpect(jsonPath("$.specialization").value(SPECIALIZATION))
                .andExpect(jsonPath("$.trainees[0].username").value(TRAINEE_USERNAME));
    }

    // ==================== Update profile ====================

    @Test
    void updateProfile_shouldReturn200() throws Exception {
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                FIRST_NAME, "Updated", true);
        when(trainerService.updateTrainerProfile(anyString(), anyString(), anyString(), anyBoolean()))
                .thenReturn(testTrainer);

        mockMvc.perform(put(BASE_URL + "/" + USERNAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andExpect(jsonPath("$.specialization").value(SPECIALIZATION));
    }

    @Test
    void updateProfile_shouldReturn400WhenIsActiveNull() throws Exception {
        String json = """
                {"firstName":"Jane","lastName":"Smith"}
                """;

        mockMvc.perform(put(BASE_URL + "/" + USERNAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // ==================== Trainings ====================

    @Test
    void getTrainings_shouldReturn200() throws Exception {
        User traineeUser = new User(2L, "John", "Doe", "John.Doe", "p", true, Role.TRAINEE);
        Trainee trainee = new Trainee(1L, null, null, traineeUser);
        TrainingType type = new TrainingType(SPECIALIZATION_ID, SPECIALIZATION);
        Training training = new Training(1L, trainee, testTrainer,
                TRAINING_NAME, type, TRAINING_DATE, TRAINING_DURATION);
        when(trainerService.getTrainerTrainings(anyString(), any(), any(), any()))
                .thenReturn(List.of(training));

        mockMvc.perform(get(BASE_URL + "/" + USERNAME + "/trainings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingName").value(TRAINING_NAME))
                .andExpect(jsonPath("$[0].traineeName").value("John Doe"));
    }

    @Test
    void getTrainings_shouldReturn200WithFilters() throws Exception {
        when(trainerService.getTrainerTrainings(anyString(), any(), any(), any()))
                .thenReturn(List.of());

        mockMvc.perform(get(BASE_URL + "/" + USERNAME + "/trainings")
                        .param("periodFrom", "2024-01-01")
                        .param("periodTo", "2024-12-31")
                        .param("traineeName", "John"))
                .andExpect(status().isOk());
    }

    // ==================== Activate / deactivate ====================

    @Test
    void activateStatus_shouldReturn200() throws Exception {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest(false);

        mockMvc.perform(patch(BASE_URL + "/" + USERNAME + "/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(trainerService).setActiveStatus(USERNAME, false);
    }

    @Test
    void activateStatus_shouldReturn400WhenAlreadySameStatus() throws Exception {
        ActivateDeactivateRequest request = new ActivateDeactivateRequest(true);
        doThrow(new ValidationException("Trainer is already active"))
                .when(trainerService).setActiveStatus(USERNAME, true);

        mockMvc.perform(patch(BASE_URL + "/" + USERNAME + "/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}