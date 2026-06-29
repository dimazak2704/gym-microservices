package com.dimazak.gym.controller;

import com.dimazak.gym.dto.AddTrainingRequest;
import com.dimazak.gym.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trainings")
@Tag(name = "Training", description = "Training management endpoints")
public class TrainingController {

    private static final Logger log = LoggerFactory.getLogger(TrainingController.class);

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @PostMapping
    @Operation(summary = "Add training",
            description = "Create a new training. Requires authentication.")
    public ResponseEntity<Void> addTraining(@Valid @RequestBody AddTrainingRequest request) {
        log.info("Adding training: '{}' for trainee: {}, trainer: {}",
                request.trainingName(), request.traineeUsername(), request.trainerUsername());

        trainingService.addTraining(
                request.traineeUsername(), request.trainerUsername(),
                request.trainingName(), request.trainingDate(),
                request.trainingDuration());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}