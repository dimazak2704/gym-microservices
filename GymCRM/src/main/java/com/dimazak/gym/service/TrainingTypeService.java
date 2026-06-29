package com.dimazak.gym.service;

import com.dimazak.gym.dto.TrainingTypeResponse;

import java.util.List;

public interface TrainingTypeService {

    List<TrainingTypeResponse> getAllTrainingTypes();
}