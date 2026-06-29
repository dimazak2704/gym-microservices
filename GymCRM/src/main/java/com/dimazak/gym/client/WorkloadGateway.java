package com.dimazak.gym.client;

import com.dimazak.gym.dto.WorkloadActionType;
import com.dimazak.gym.dto.WorkloadRequest;
import com.dimazak.gym.model.Training;
import org.springframework.stereotype.Component;

@Component
public class WorkloadGateway {

    private final WorkloadCircuitBreakerClient cbClient;

    public WorkloadGateway(WorkloadCircuitBreakerClient cbClient) {
        this.cbClient = cbClient;
    }

    public void notifyTrainingAdded(Training training) {
        cbClient.send(buildRequest(training, WorkloadActionType.ADD));
    }

    public void notifyTrainingDeleted(Training training) {
        cbClient.send(buildRequest(training, WorkloadActionType.DELETE));
    }

    private WorkloadRequest buildRequest(Training training, WorkloadActionType action) {
        var user = training.getTrainer().getUser();
        return new WorkloadRequest(
                user.getUsername(), user.getFirstName(), user.getLastName(),
                user.isActive(), training.getTrainingDate(),
                training.getTrainingDuration(), action);
    }
}