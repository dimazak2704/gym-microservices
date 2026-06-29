package com.dimazak.gym.service;

import com.dimazak.gym.dao.TrainerDao;
import com.dimazak.gym.dao.TrainingDao;
import com.dimazak.gym.dao.TrainingTypeDao;
import com.dimazak.gym.exception.EntityNotFoundException;
import com.dimazak.gym.exception.ValidationException;
import com.dimazak.gym.metrics.GymMetrics;
import com.dimazak.gym.model.*;
import com.dimazak.gym.security.SecurityUtils;
import com.dimazak.gym.service.impl.TrainerServiceImpl;
import com.dimazak.gym.util.PasswordGenerator;
import com.dimazak.gym.util.UsernameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    private static final String FIRST_NAME = "Jane";
    private static final String LAST_NAME = "Doe";
    private static final String USERNAME = "Jane.Doe";
    private static final String RAW_PASSWORD = "pass123456";
    private static final String ENCODED_PASSWORD = "$2a$10$encodedHash";
    private static final String NEW_PASSWORD = "newPass5678";
    private static final String SHORT_PASSWORD = "short";
    private static final Long SPECIALIZATION_ID = 1L;
    private static final Long NEW_SPECIALIZATION_ID = 2L;
    private static final String SPECIALIZATION = "Cardio";
    private static final Long TRAINER_ID = 1L;

    @Mock private TrainerDao trainerDao;
    @Mock private TrainingDao trainingDao;
    @Mock private TrainingTypeDao trainingTypeDao;
    @Mock private UsernameGenerator usernameGenerator;
    @Mock private PasswordGenerator passwordGenerator;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private GymMetrics gymMetrics;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks private TrainerServiceImpl trainerService;

    private Trainer buildTrainer() {
        User user = new User(1L, FIRST_NAME, LAST_NAME, USERNAME, ENCODED_PASSWORD, true, Role.TRAINER);
        return new Trainer(TRAINER_ID, new TrainingType(SPECIALIZATION_ID, SPECIALIZATION), user);
    }

    // ==================== createTrainer ====================

    @Test
    void createTrainer_shouldPersistEncodedPasswordAndReturnRawInView() {
        TrainingType type = new TrainingType(SPECIALIZATION_ID, SPECIALIZATION);
        when(trainingTypeDao.findById(SPECIALIZATION_ID)).thenReturn(Optional.of(type));
        when(usernameGenerator.generateUsername(FIRST_NAME, LAST_NAME)).thenReturn(USERNAME);
        when(passwordGenerator.generatePassword()).thenReturn(RAW_PASSWORD);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(trainerDao.save(any(Trainer.class))).thenAnswer(inv -> {
            Trainer t = inv.getArgument(0);
            t.setId(TRAINER_ID);
            assertEquals(ENCODED_PASSWORD, t.getUser().getPassword());
            return t;
        });

        Trainer result = trainerService.createTrainer(FIRST_NAME, LAST_NAME, SPECIALIZATION_ID);

        assertEquals(TRAINER_ID, result.getId());
        assertEquals(USERNAME, result.getUser().getUsername());
        assertEquals(RAW_PASSWORD, result.getUser().getPassword());
        assertEquals(Role.TRAINER, result.getUser().getRole());
        verify(gymMetrics).incrementTrainerRegistration();
    }

    @Test
    void createTrainer_shouldThrowWhenSpecializationNotFound() {
        when(trainingTypeDao.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> trainerService.createTrainer(FIRST_NAME, LAST_NAME, 99L));
    }

    @Test
    void createTrainer_shouldThrowWhenFirstNameBlank() {
        assertThrows(ValidationException.class,
                () -> trainerService.createTrainer("", LAST_NAME, SPECIALIZATION_ID));
    }

    @Test
    void createTrainer_shouldThrowWhenLastNameNull() {
        assertThrows(ValidationException.class,
                () -> trainerService.createTrainer(FIRST_NAME, null, SPECIALIZATION_ID));
    }

    @Test
    void createTrainer_shouldThrowWhenSpecializationNull() {
        assertThrows(ValidationException.class,
                () -> trainerService.createTrainer(FIRST_NAME, LAST_NAME, null));
    }

    // ==================== getByUsername ====================

    @Test
    void getByUsername_shouldReturnTrainer() {
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(buildTrainer()));
        assertEquals(USERNAME, trainerService.getByUsername(USERNAME).getUser().getUsername());
    }

    @Test
    void getByUsername_shouldThrowWhenNotFound() {
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> trainerService.getByUsername(USERNAME));
    }

    // ==================== getProfileByUsername (with ownership) ====================

    @Test
    void getProfileByUsername_shouldReturnProfileForOwner() {
        doNothing().when(securityUtils).verifyOwnership(USERNAME);
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(buildTrainer()));

        Trainer result = trainerService.getProfileByUsername(USERNAME);

        assertEquals(USERNAME, result.getUser().getUsername());
        verify(securityUtils).verifyOwnership(USERNAME);
    }

    @Test
    void getProfileByUsername_shouldThrowWhenNotOwner() {
        doThrow(new AccessDeniedException("Access denied"))
                .when(securityUtils).verifyOwnership(USERNAME);

        assertThrows(AccessDeniedException.class,
                () -> trainerService.getProfileByUsername(USERNAME));
    }

    // ==================== changePassword ====================

    @Test
    void changePassword_shouldEncodeAndPersist() {
        Trainer trainer = buildTrainer();
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn("$2a$10$newHash");
        when(trainerDao.save(any(Trainer.class))).thenReturn(trainer);

        trainerService.changePassword(USERNAME, NEW_PASSWORD);

        assertEquals("$2a$10$newHash", trainer.getUser().getPassword());
    }

    @Test
    void changePassword_shouldThrowWhenTooShort() {
        assertThrows(ValidationException.class,
                () -> trainerService.changePassword(USERNAME, SHORT_PASSWORD));
    }

    // ==================== updateTrainer (with ownership) ====================

    @Test
    void updateTrainer_shouldUpdateAllFields() {
        doNothing().when(securityUtils).verifyOwnership(USERNAME);
        Trainer trainer = buildTrainer();
        TrainingType newType = new TrainingType(NEW_SPECIALIZATION_ID, "Strength");
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
        when(trainingTypeDao.findById(NEW_SPECIALIZATION_ID)).thenReturn(Optional.of(newType));
        when(trainerDao.save(any(Trainer.class))).thenReturn(trainer);

        Trainer result = trainerService.updateTrainer(USERNAME, "Bob", "Smith",
                NEW_SPECIALIZATION_ID, false);

        assertEquals("Bob", result.getUser().getFirstName());
        assertEquals(newType, result.getSpecialization());
        assertFalse(result.getUser().isActive());
    }

    // ==================== updateTrainerProfile (with ownership) ====================

    @Test
    void updateTrainerProfile_shouldUpdateBasicFields() {
        doNothing().when(securityUtils).verifyOwnership(USERNAME);
        Trainer trainer = buildTrainer();
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
        when(trainerDao.save(any(Trainer.class))).thenReturn(trainer);

        Trainer result = trainerService.updateTrainerProfile(USERNAME, "Bob", "Smith", false);

        assertEquals("Bob", result.getUser().getFirstName());
        assertFalse(result.getUser().isActive());
    }

    @Test
    void updateTrainerProfile_shouldThrowWhenFirstNameBlank() {
        assertThrows(ValidationException.class,
                () -> trainerService.updateTrainerProfile(USERNAME, "", LAST_NAME, true));
    }

    @Test
    void updateTrainerProfile_shouldThrowWhenNotOwner() {
        doThrow(new AccessDeniedException("Access denied"))
                .when(securityUtils).verifyOwnership(USERNAME);

        assertThrows(AccessDeniedException.class,
                () -> trainerService.updateTrainerProfile(USERNAME, FIRST_NAME, LAST_NAME, true));
    }

    // ==================== setActiveStatus (with ownership) ====================

    @Test
    void setActiveStatus_shouldActivate() {
        doNothing().when(securityUtils).verifyOwnership(USERNAME);
        Trainer trainer = buildTrainer();
        trainer.getUser().setActive(false);
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
        when(trainerDao.save(any(Trainer.class))).thenReturn(trainer);

        trainerService.setActiveStatus(USERNAME, true);

        assertTrue(trainer.getUser().isActive());
    }

    @Test
    void setActiveStatus_shouldThrowWhenAlreadySame() {
        doNothing().when(securityUtils).verifyOwnership(USERNAME);
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(buildTrainer()));

        assertThrows(ValidationException.class,
                () -> trainerService.setActiveStatus(USERNAME, true));
    }

    @Test
    void setActiveStatus_shouldThrowWhenNotOwner() {
        doThrow(new AccessDeniedException("Access denied"))
                .when(securityUtils).verifyOwnership(USERNAME);

        assertThrows(AccessDeniedException.class,
                () -> trainerService.setActiveStatus(USERNAME, false));
    }

    // ==================== getTrainerTrainings (with ownership) ====================

    @Test
    void getTrainerTrainings_shouldDelegate() {
        doNothing().when(securityUtils).verifyOwnership(USERNAME);
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(buildTrainer()));
        when(trainingDao.findByTrainerWithFilters(USERNAME, null, null, null))
                .thenReturn(List.of());

        assertTrue(trainerService.getTrainerTrainings(USERNAME, null, null, null).isEmpty());
    }

    @Test
    void getTrainerTrainings_shouldThrowWhenNotOwner() {
        doThrow(new AccessDeniedException("Access denied"))
                .when(securityUtils).verifyOwnership(USERNAME);

        assertThrows(AccessDeniedException.class,
                () -> trainerService.getTrainerTrainings(USERNAME, null, null, null));
    }
}