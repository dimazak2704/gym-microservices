package com.dimazak.gym.service;

import com.dimazak.gym.client.WorkloadGateway;
import com.dimazak.gym.dao.TraineeDao;
import com.dimazak.gym.dao.TrainerDao;
import com.dimazak.gym.dao.TrainingDao;
import com.dimazak.gym.exception.EntityNotFoundException;
import com.dimazak.gym.exception.ValidationException;
import com.dimazak.gym.metrics.GymMetrics;
import com.dimazak.gym.model.*;
import com.dimazak.gym.security.SecurityUtils;
import com.dimazak.gym.service.impl.TraineeServiceImpl;
import com.dimazak.gym.util.PasswordGenerator;
import com.dimazak.gym.util.UsernameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String USERNAME = "John.Doe";
    private static final String RAW_PASSWORD = "abc1234567";
    private static final String ENCODED_PASSWORD = "$2a$10$encodedHash";
    private static final String NEW_PASSWORD = "newPass1234";
    private static final String SHORT_PASSWORD = "short";
    private static final LocalDate BIRTH_DATE = LocalDate.of(1990, 1, 1);
    private static final String ADDRESS = "123 Main St";
    private static final String NEW_ADDRESS = "456 New St";
    private static final String TRAINER_USERNAME = "Bob.Smith";
    private static final Long TRAINEE_ID = 1L;
    private static final Long TRAINER_ID = 1L;

    @Mock private TraineeDao traineeDao;
    @Mock private TrainerDao trainerDao;
    @Mock private TrainingDao trainingDao;
    @Mock private UsernameGenerator usernameGenerator;
    @Mock private PasswordGenerator passwordGenerator;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private GymMetrics gymMetrics;
    @Mock private SecurityUtils securityUtils;
    @Mock private WorkloadGateway workloadGateway;

    @InjectMocks private TraineeServiceImpl traineeService;

    private Trainee buildTrainee() {
        User user = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, ENCODED_PASSWORD, true, Role.TRAINEE);
        return new Trainee(TRAINEE_ID, BIRTH_DATE, ADDRESS, user);
    }

    // ==================== createTrainee ====================

    @Test
    void createTrainee_shouldPersistEncodedPasswordAndReturnRawInView() {
        when(usernameGenerator.generateUsername(FIRST_NAME, LAST_NAME)).thenReturn(USERNAME);
        when(passwordGenerator.generatePassword()).thenReturn(RAW_PASSWORD);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(traineeDao.save(any(Trainee.class))).thenAnswer(inv -> {
            Trainee t = inv.getArgument(0);
            t.setId(TRAINEE_ID);
            assertEquals(ENCODED_PASSWORD, t.getUser().getPassword());
            return t;
        });

        Trainee result = traineeService.createTrainee(FIRST_NAME, LAST_NAME, BIRTH_DATE, ADDRESS);

        assertEquals(TRAINEE_ID, result.getId());
        assertEquals(USERNAME, result.getUser().getUsername());
        assertEquals(RAW_PASSWORD, result.getUser().getPassword());
        assertEquals(Role.TRAINEE, result.getUser().getRole());
        verify(gymMetrics).incrementTraineeRegistration();
    }

    @Test
    void createTrainee_shouldThrowWhenFirstNameNull() {
        assertThrows(ValidationException.class,
                () -> traineeService.createTrainee(null, LAST_NAME, BIRTH_DATE, ADDRESS));
        verifyNoInteractions(traineeDao);
    }

    @Test
    void createTrainee_shouldThrowWhenFirstNameBlank() {
        assertThrows(ValidationException.class,
                () -> traineeService.createTrainee("  ", LAST_NAME, BIRTH_DATE, ADDRESS));
    }

    @Test
    void createTrainee_shouldThrowWhenLastNameBlank() {
        assertThrows(ValidationException.class,
                () -> traineeService.createTrainee(FIRST_NAME, "", BIRTH_DATE, ADDRESS));
    }

    // ==================== existsByUsername ====================

    @Test
    void existsByUsername_shouldReturnTrue() {
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(buildTrainee()));
        assertTrue(traineeService.existsByUsername(USERNAME));
    }

    @Test
    void existsByUsername_shouldReturnFalse() {
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.empty());
        assertFalse(traineeService.existsByUsername(USERNAME));
    }

    // ==================== getByUsername ====================

    @Test
    void getByUsername_shouldReturnTrainee() {
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(buildTrainee()));
        Trainee result = traineeService.getByUsername(USERNAME);
        assertEquals(USERNAME, result.getUser().getUsername());
    }

    @Test
    void getByUsername_shouldThrowWhenNotFound() {
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> traineeService.getByUsername(USERNAME));
    }

    // ==================== getProfileByUsername (with ownership) ====================

    @Test
    void getProfileByUsername_shouldReturnProfileForOwner() {
        doNothing().when(securityUtils).verifyOwnership(USERNAME);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(buildTrainee()));

        Trainee result = traineeService.getProfileByUsername(USERNAME);

        assertEquals(USERNAME, result.getUser().getUsername());
        verify(securityUtils).verifyOwnership(USERNAME);
    }

    @Test
    void getProfileByUsername_shouldThrowWhenNotOwner() {
        doThrow(new AccessDeniedException("Access denied"))
                .when(securityUtils).verifyOwnership(USERNAME);

        assertThrows(AccessDeniedException.class,
                () -> traineeService.getProfileByUsername(USERNAME));
        verifyNoInteractions(traineeDao);
    }

    // ==================== changePassword ====================

    @Test
    void changePassword_shouldEncodeAndPersist() {
        Trainee trainee = buildTrainee();
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn("$2a$10$newHash");
        when(traineeDao.save(any(Trainee.class))).thenReturn(trainee);

        traineeService.changePassword(USERNAME, NEW_PASSWORD);

        assertEquals("$2a$10$newHash", trainee.getUser().getPassword());
        verify(passwordEncoder).encode(NEW_PASSWORD);
    }

    @Test
    void changePassword_shouldThrowWhenNull() {
        assertThrows(ValidationException.class,
                () -> traineeService.changePassword(USERNAME, null));
    }

    @Test
    void changePassword_shouldThrowWhenBlank() {
        assertThrows(ValidationException.class,
                () -> traineeService.changePassword(USERNAME, "   "));
    }

    @Test
    void changePassword_shouldThrowWhenTooShort() {
        assertThrows(ValidationException.class,
                () -> traineeService.changePassword(USERNAME, SHORT_PASSWORD));
    }

    // ==================== updateTrainee (with ownership) ====================

    @Test
    void updateTrainee_shouldUpdateAllFields() {
        doNothing().when(securityUtils).verifyOwnership(USERNAME);
        Trainee trainee = buildTrainee();
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(traineeDao.save(any(Trainee.class))).thenReturn(trainee);

        Trainee result = traineeService.updateTrainee(USERNAME, "Jane", "Smith",
                BIRTH_DATE, NEW_ADDRESS, false);

        assertEquals("Jane", result.getUser().getFirstName());
        assertEquals("Smith", result.getUser().getLastName());
        assertEquals(NEW_ADDRESS, result.getAddress());
        assertFalse(result.getUser().isActive());
    }

    @Test
    void updateTrainee_shouldThrowWhenNotOwner() {
        doThrow(new AccessDeniedException("Access denied"))
                .when(securityUtils).verifyOwnership(USERNAME);

        assertThrows(AccessDeniedException.class,
                () -> traineeService.updateTrainee(USERNAME, FIRST_NAME, LAST_NAME,
                        BIRTH_DATE, ADDRESS, true));
    }

    @Test
    void updateTrainee_shouldThrowWhenFirstNameBlank() {
        assertThrows(ValidationException.class,
                () -> traineeService.updateTrainee(USERNAME, "", LAST_NAME,
                        BIRTH_DATE, ADDRESS, true));
    }

    // ==================== setActiveStatus (with ownership) ====================

    @Test
    void setActiveStatus_shouldDeactivate() {
        doNothing().when(securityUtils).verifyOwnership(USERNAME);
        Trainee trainee = buildTrainee();
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(traineeDao.save(any(Trainee.class))).thenReturn(trainee);

        traineeService.setActiveStatus(USERNAME, false);

        assertFalse(trainee.getUser().isActive());
    }

    @Test
    void setActiveStatus_shouldThrowWhenAlreadySameStatus() {
        doNothing().when(securityUtils).verifyOwnership(USERNAME);
        Trainee trainee = buildTrainee();
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        assertThrows(ValidationException.class,
                () -> traineeService.setActiveStatus(USERNAME, true));
    }

    @Test
    void setActiveStatus_shouldThrowWhenNotOwner() {
        doThrow(new AccessDeniedException("Access denied"))
                .when(securityUtils).verifyOwnership(USERNAME);

        assertThrows(AccessDeniedException.class,
                () -> traineeService.setActiveStatus(USERNAME, false));
    }

    // ==================== deleteByUsername (with ownership) ====================

    @Test
    void deleteByUsername_shouldDelegateToDao() {
        doNothing().when(securityUtils).verifyOwnership(USERNAME);
        Trainee trainee = buildTrainee();
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        traineeService.deleteByUsername(USERNAME);

        verify(traineeDao).delete(trainee);
    }

    @Test
    void deleteByUsername_shouldThrowWhenNotFound() {
        doNothing().when(securityUtils).verifyOwnership(USERNAME);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> traineeService.deleteByUsername(USERNAME));
    }

    @Test
    void deleteByUsername_shouldThrowWhenNotOwner() {
        doThrow(new AccessDeniedException("Access denied"))
                .when(securityUtils).verifyOwnership(USERNAME);

        assertThrows(AccessDeniedException.class,
                () -> traineeService.deleteByUsername(USERNAME));
    }

    // ==================== getTraineeTrainings (with ownership) ====================

    @Test
    void getTraineeTrainings_shouldDelegate() {
        doNothing().when(securityUtils).verifyOwnership(USERNAME);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(buildTrainee()));
        when(trainingDao.findByTraineeWithFilters(USERNAME, null, null, null, null))
                .thenReturn(List.of());

        List<Training> result = traineeService.getTraineeTrainings(USERNAME, null, null, null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void getTraineeTrainings_shouldThrowWhenNotOwner() {
        doThrow(new AccessDeniedException("Access denied"))
                .when(securityUtils).verifyOwnership(USERNAME);

        assertThrows(AccessDeniedException.class,
                () -> traineeService.getTraineeTrainings(USERNAME, null, null, null, null));
    }

    // ==================== getUnassignedTrainers (with ownership) ====================

    @Test
    void getUnassignedTrainers_shouldDelegate() {
        doNothing().when(securityUtils).verifyOwnership(USERNAME);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(buildTrainee()));
        when(trainerDao.findUnassignedByTraineeUsername(USERNAME)).thenReturn(List.of());

        assertTrue(traineeService.getUnassignedTrainers(USERNAME).isEmpty());
    }

    // ==================== updateTrainersList (with ownership) ====================

    @Test
    void updateTrainersList_shouldReplaceTrainers() {
        doNothing().when(securityUtils).verifyOwnership(USERNAME);
        Trainee trainee = buildTrainee();
        User trainerUser = new User(2L, "Bob", "Smith", TRAINER_USERNAME, "p", true, Role.TRAINER);
        Trainer trainer = new Trainer(TRAINER_ID, new TrainingType(1L, "Cardio"), trainerUser);

        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(trainer));
        when(traineeDao.save(any(Trainee.class))).thenReturn(trainee);

        Trainee result = traineeService.updateTrainersList(USERNAME, List.of(TRAINER_USERNAME));

        assertEquals(1, result.getTrainers().size());
    }

    @Test
    void updateTrainersList_shouldThrowWhenTrainerNotFound() {
        doNothing().when(securityUtils).verifyOwnership(USERNAME);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(buildTrainee()));
        when(trainerDao.findByUsername("Unknown")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> traineeService.updateTrainersList(USERNAME, List.of("Unknown")));
    }

    @Test
    void deleteByUsername_shouldNotifyWorkloadForEachTraining() {
        doNothing().when(securityUtils).verifyOwnership(USERNAME);
        Trainee trainee = buildTrainee();
        Training tr = new Training(1L, trainee, null, "Run",
                new TrainingType(1L, "Cardio"), LocalDate.of(2024, 4, 1), 60);
        trainee.setTrainings(new java.util.ArrayList<>(List.of(tr)));
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        traineeService.deleteByUsername(USERNAME);

        verify(traineeDao).delete(trainee);
        verify(workloadGateway).notifyTrainingDeleted(tr);
    }
}