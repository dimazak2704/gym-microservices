package com.dimazak.gym.client;

import com.dimazak.gym.dto.WorkloadActionType;
import com.dimazak.gym.dto.WorkloadRequest;
import com.dimazak.gym.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadGatewayTest {

    private static final String TRAINER_USERNAME = "Jane.Smith";
    private static final String FIRST = "Jane";
    private static final String LAST = "Smith";
    private static final LocalDate DATE = LocalDate.of(2024, 4, 1);
    private static final int DURATION = 60;

    @Mock
    private JmsTemplate jmsTemplate;

    @InjectMocks
    private WorkloadGateway gateway;

    @Captor
    private ArgumentCaptor<Object> payloadCaptor;

    private Training buildTraining() {
        User trainerUser = new User(2L, FIRST, LAST, TRAINER_USERNAME, "p", true, Role.TRAINER);
        Trainer trainer = new Trainer(1L, new TrainingType(1L, "Cardio"), trainerUser);
        Trainee trainee = new Trainee(1L, null, null,
                new User(1L, "John", "Doe", "John.Doe", "p", true, Role.TRAINEE));
        return new Training(1L, trainee, trainer, "Run",
                new TrainingType(1L, "Cardio"), DATE, DURATION);
    }

    @Test
    void notifyTrainingAdded_shouldSendToQueueWithAddAction() {
        gateway.notifyTrainingAdded(buildTraining());

        verify(jmsTemplate).convertAndSend(
                eq(JmsQueues.TRAINING_QUEUE),
                payloadCaptor.capture(),
                any(MessagePostProcessor.class));

        WorkloadRequest req = (WorkloadRequest) payloadCaptor.getValue();
        assertEquals(WorkloadActionType.ADD, req.actionType());
        assertEquals(TRAINER_USERNAME, req.trainerUsername());
        assertEquals(FIRST, req.trainerFirstName());
        assertEquals(LAST, req.trainerLastName());
        assertEquals(DATE, req.trainingDate());
        assertEquals(DURATION, req.trainingDuration());
        assertTrue(req.isActive());
    }

    @Test
    void notifyTrainingDeleted_shouldSendToQueueWithDeleteAction() {
        gateway.notifyTrainingDeleted(buildTraining());

        verify(jmsTemplate).convertAndSend(
                eq(JmsQueues.TRAINING_QUEUE),
                payloadCaptor.capture(),
                any(MessagePostProcessor.class));

        WorkloadRequest req = (WorkloadRequest) payloadCaptor.getValue();
        assertEquals(WorkloadActionType.DELETE, req.actionType());
        assertEquals(TRAINER_USERNAME, req.trainerUsername());
    }

    @Test
    void notifyTrainingAdded_shouldMapInactiveTrainer() {
        Training t = buildTraining();
        t.getTrainer().getUser().setActive(false);

        gateway.notifyTrainingAdded(t);

        verify(jmsTemplate).convertAndSend(
                eq(JmsQueues.TRAINING_QUEUE),
                payloadCaptor.capture(),
                any(MessagePostProcessor.class));

        WorkloadRequest req = (WorkloadRequest) payloadCaptor.getValue();
        assertFalse(req.isActive());
    }

    @Test
    void notifyTrainingAdded_shouldSendToCorrectQueue() {
        gateway.notifyTrainingAdded(buildTraining());

        verify(jmsTemplate).convertAndSend(
                eq("training.queue"),
                any(),
                any(MessagePostProcessor.class));
    }
}