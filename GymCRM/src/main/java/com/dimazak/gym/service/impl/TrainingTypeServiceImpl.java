package com.dimazak.gym.service.impl;

import com.dimazak.gym.dao.TrainingTypeDao;
import com.dimazak.gym.dto.TrainingTypeResponse;
import com.dimazak.gym.service.TrainingTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainingTypeServiceImpl implements TrainingTypeService {

    private static final Logger log = LoggerFactory.getLogger(TrainingTypeServiceImpl.class);

    private final TrainingTypeDao trainingTypeDao;

    public TrainingTypeServiceImpl(TrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingTypeResponse> getAllTrainingTypes() {
        log.info("Fetching all training types");
        return trainingTypeDao.findAll().stream()
                .map(type -> new TrainingTypeResponse(type.getId(), type.getTrainingTypeName()))
                .toList();
    }
}