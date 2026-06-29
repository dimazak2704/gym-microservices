package com.dimazak.gym.service;

import com.dimazak.gym.model.Trainer;
import com.dimazak.gym.model.Training;

import java.time.LocalDate;
import java.util.List;

public interface TrainerService {

    Trainer createTrainer(String firstName, String lastName, Long specializationId);

    Trainer getByUsername(String username);

    Trainer getProfileByUsername(String username);

    void changePassword(String username, String newPassword);

    Trainer updateTrainer(String username, String firstName, String lastName,
                          Long specializationId, boolean isActive);

    Trainer updateTrainerProfile(String username, String firstName,
                                 String lastName, boolean isActive);

    void setActiveStatus(String username, boolean isActive);

    List<Training> getTrainerTrainings(String username, LocalDate fromDate,
                                       LocalDate toDate, String traineeName);
}