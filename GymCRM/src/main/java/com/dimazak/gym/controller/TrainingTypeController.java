package com.dimazak.gym.controller;

import com.dimazak.gym.dto.TrainingTypeResponse;
import com.dimazak.gym.service.TrainingTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/training-types")
@Tag(name = "Training Type", description = "Training type reference data")
public class TrainingTypeController {

    private static final Logger log = LoggerFactory.getLogger(TrainingTypeController.class);

    private final TrainingTypeService trainingTypeService;

    public TrainingTypeController(TrainingTypeService trainingTypeService) {
        this.trainingTypeService = trainingTypeService;
    }

    @GetMapping
    @Operation(summary = "Get all training types", description = "Retrieve all available training types")
    public ResponseEntity<List<TrainingTypeResponse>> getAllTrainingTypes() {
        log.info("Fetching all training types");
        return ResponseEntity.ok(trainingTypeService.getAllTrainingTypes());
    }
}