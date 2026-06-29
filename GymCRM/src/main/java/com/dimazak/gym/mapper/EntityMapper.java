package com.dimazak.gym.mapper;

import com.dimazak.gym.dto.*;
import com.dimazak.gym.model.Trainee;
import com.dimazak.gym.model.Trainer;
import com.dimazak.gym.model.Training;
import org.springframework.stereotype.Component;

@Component
public class EntityMapper {

    public TraineeProfileResponse toTraineeProfileResponse(Trainee trainee) {
        return new TraineeProfileResponse(
                trainee.getUser().getFirstName(),
                trainee.getUser().getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                trainee.getUser().isActive(),
                trainee.getTrainers().stream().map(this::toTrainerSummary).toList()
        );
    }

    public UpdateTraineeResponse toUpdateTraineeResponse(Trainee trainee) {
        return new UpdateTraineeResponse(
                trainee.getUser().getUsername(),
                trainee.getUser().getFirstName(),
                trainee.getUser().getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                trainee.getUser().isActive(),
                trainee.getTrainers().stream().map(this::toTrainerSummary).toList()
        );
    }

    public TrainerProfileResponse toTrainerProfileResponse(Trainer trainer) {
        return new TrainerProfileResponse(
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getSpecialization().getTrainingTypeName(),
                trainer.getUser().isActive(),
                trainer.getTrainees().stream().map(this::toTraineeSummary).toList()
        );
    }

    public UpdateTrainerResponse toUpdateTrainerResponse(Trainer trainer) {
        return new UpdateTrainerResponse(
                trainer.getUser().getUsername(),
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getSpecialization().getTrainingTypeName(),
                trainer.getUser().isActive(),
                trainer.getTrainees().stream().map(this::toTraineeSummary).toList()
        );
    }

    public TrainerSummary toTrainerSummary(Trainer trainer) {
        return new TrainerSummary(
                trainer.getUser().getUsername(),
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getSpecialization().getTrainingTypeName()
        );
    }

    public TraineeSummary toTraineeSummary(Trainee trainee) {
        return new TraineeSummary(
                trainee.getUser().getUsername(),
                trainee.getUser().getFirstName(),
                trainee.getUser().getLastName()
        );
    }

    public TraineeTrainingResponse toTraineeTrainingResponse(Training training) {
        return new TraineeTrainingResponse(
                training.getTrainingName(),
                training.getTrainingDate(),
                training.getTrainingType().getTrainingTypeName(),
                training.getTrainingDuration(),
                training.getTrainer().getUser().getFirstName() + " " +
                        training.getTrainer().getUser().getLastName()
        );
    }

    public TrainerTrainingResponse toTrainerTrainingResponse(Training training) {
        return new TrainerTrainingResponse(
                training.getTrainingName(),
                training.getTrainingDate(),
                training.getTrainingType().getTrainingTypeName(),
                training.getTrainingDuration(),
                training.getTrainee().getUser().getFirstName() + " " +
                        training.getTrainee().getUser().getLastName()
        );
    }
}