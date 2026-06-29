package com.dimazak.gym.service;

import com.dimazak.gym.model.Training;

import java.time.LocalDate;

public interface TrainingService {

    Training addTraining(String traineeUsername, String trainerUsername,
                         String trainingName, LocalDate trainingDate,
                         int trainingDuration);

    Training addTraining(String traineeUsername, String trainerUsername,
                         String trainingName, Long trainingTypeId,
                         LocalDate trainingDate, int trainingDuration);
}