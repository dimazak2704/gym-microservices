package com.dimazak.gym.client;

import com.dimazak.gym.dto.WorkloadActionType;
import com.dimazak.gym.dto.WorkloadRequest;
import com.dimazak.gym.model.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class WorkloadGateway {

    private static final Logger log = LoggerFactory.getLogger(WorkloadGateway.class);
    private static final String TX_HEADER = "transactionId";

    private final JmsTemplate jmsTemplate;

    public WorkloadGateway(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void notifyTrainingAdded(Training training) {
        send(buildRequest(training, WorkloadActionType.ADD));
    }

    public void notifyTrainingDeleted(Training training) {
        send(buildRequest(training, WorkloadActionType.DELETE));
    }

    private void send(WorkloadRequest request) {
        String txId = MDC.get(TX_HEADER);
        log.info("Sending workload message to queue '{}': trainer='{}', action={}, txId={}",
                JmsQueues.TRAINING_QUEUE, request.trainerUsername(),
                request.actionType(), txId);

        jmsTemplate.convertAndSend(JmsQueues.TRAINING_QUEUE, request, message -> {
            if (txId != null) {
                message.setStringProperty(TX_HEADER, txId);
            }
            return message;
        });

        log.info("Workload message sent for trainer '{}'", request.trainerUsername());
    }

    private WorkloadRequest buildRequest(Training training, WorkloadActionType action) {
        var user = training.getTrainer().getUser();
        return new WorkloadRequest(
                user.getUsername(), user.getFirstName(), user.getLastName(),
                user.isActive(), training.getTrainingDate(),
                training.getTrainingDuration(), action);
    }
}