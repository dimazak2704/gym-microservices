package com.dimazak.gym.service;

import com.dimazak.gym.model.Trainee;
import com.dimazak.gym.model.Trainer;
import com.dimazak.gym.model.Training;

import java.time.LocalDate;
import java.util.List;

public interface TraineeService {

    Trainee createTrainee(String firstName, String lastName,
                          LocalDate dateOfBirth, String address);

    boolean existsByUsername(String username);

    Trainee getByUsername(String username);

    Trainee getProfileByUsername(String username);

    void changePassword(String username, String newPassword);

    Trainee updateTrainee(String username, String firstName, String lastName,
                          LocalDate dateOfBirth, String address, boolean isActive);

    void setActiveStatus(String username, boolean isActive);

    void deleteByUsername(String username);

    List<Training> getTraineeTrainings(String username, LocalDate fromDate,
                                       LocalDate toDate, String trainerName,
                                       String trainingTypeName);

    List<Trainer> getUnassignedTrainers(String traineeUsername);

    Trainee updateTrainersList(String username, List<String> trainerUsernames);
}