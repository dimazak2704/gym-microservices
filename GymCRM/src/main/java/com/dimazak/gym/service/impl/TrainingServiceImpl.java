package com.dimazak.gym.service.impl;

import com.dimazak.gym.client.WorkloadGateway;
import com.dimazak.gym.dao.TraineeDao;
import com.dimazak.gym.dao.TrainerDao;
import com.dimazak.gym.dao.TrainingDao;
import com.dimazak.gym.dao.TrainingTypeDao;
import com.dimazak.gym.exception.EntityNotFoundException;
import com.dimazak.gym.exception.ValidationException;
import com.dimazak.gym.metrics.GymMetrics;
import com.dimazak.gym.model.Trainee;
import com.dimazak.gym.model.Trainer;
import com.dimazak.gym.model.Training;
import com.dimazak.gym.model.TrainingType;
import com.dimazak.gym.service.TrainingService;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class TrainingServiceImpl implements TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingServiceImpl.class);

    private final TrainingDao trainingDao;
    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final TrainingTypeDao trainingTypeDao;
    private final GymMetrics gymMetrics;
    private final WorkloadGateway workloadGateway;

    public TrainingServiceImpl(TrainingDao trainingDao,
                               TraineeDao traineeDao,
                               TrainerDao trainerDao,
                               TrainingTypeDao trainingTypeDao,
                               GymMetrics gymMetrics, WorkloadGateway workloadGateway) {
        this.trainingDao = trainingDao;
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.trainingTypeDao = trainingTypeDao;
        this.gymMetrics = gymMetrics;
        this.workloadGateway = workloadGateway;
    }

    @Override
    @Timed(value = "gym.training.creation.time", description = "Time to create training")
    @Transactional
    public Training addTraining(String traineeUsername, String trainerUsername,
                                String trainingName, LocalDate trainingDate,
                                int trainingDuration) {
        log.info("Adding training '{}' for trainee: '{}', trainer: '{}'",
                trainingName, traineeUsername, trainerUsername);

        validateTrainingFields(trainingName, trainingDate, trainingDuration);

        Trainee trainee = traineeDao.findByUsername(traineeUsername)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Trainee not found: " + traineeUsername));

        Trainer trainer = trainerDao.findByUsername(trainerUsername)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Trainer not found: " + trainerUsername));

        TrainingType trainingType = trainer.getSpecialization();

        Training training = new Training(null, trainee, trainer,
                trainingName, trainingType, trainingDate, trainingDuration);
        training = trainingDao.save(training);

        log.info("Training created with id: {}", training.getId());
        gymMetrics.incrementTrainingCreated();

        workloadGateway.notifyTrainingAdded(training);

        return training;
    }

    @Override
    @Transactional
    public Training addTraining(String traineeUsername, String trainerUsername,
                                String trainingName, Long trainingTypeId,
                                LocalDate trainingDate, int trainingDuration) {
        log.info("Adding training '{}' with explicit type for trainee: '{}', trainer: '{}'",
                trainingName, traineeUsername, trainerUsername);

        if (trainingName == null || trainingName.isBlank()) {
            throw new ValidationException("Training name is required");
        }
        if (trainingTypeId == null) {
            throw new ValidationException("Training type is required");
        }
        if (trainingDate == null) {
            throw new ValidationException("Training date is required");
        }
        if (trainingDuration <= 0) {
            throw new ValidationException("Training duration must be positive");
        }

        Trainee trainee = traineeDao.findByUsername(traineeUsername)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Trainee not found: " + traineeUsername));

        Trainer trainer = trainerDao.findByUsername(trainerUsername)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Trainer not found: " + trainerUsername));

        TrainingType trainingType = trainingTypeDao.findById(trainingTypeId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Training type not found with id: " + trainingTypeId));

        Training training = new Training(null, trainee, trainer,
                trainingName, trainingType, trainingDate, trainingDuration);
        training = trainingDao.save(training);

        log.info("Training created with id: {}", training.getId());

        gymMetrics.incrementTrainingCreated();
        workloadGateway.notifyTrainingAdded(training);

        return training;
    }

    private void validateTrainingFields(String trainingName, LocalDate trainingDate,
                                        int trainingDuration) {
        if (trainingName == null || trainingName.isBlank()) {
            throw new ValidationException("Training name is required");
        }
        if (trainingDate == null) {
            throw new ValidationException("Training date is required");
        }
        if (trainingDuration <= 0) {
            throw new ValidationException("Training duration must be positive");
        }
    }
}