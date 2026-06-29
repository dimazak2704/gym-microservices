package com.dimazak.gym.controller;

import com.dimazak.gym.dto.*;
import com.dimazak.gym.mapper.EntityMapper;
import com.dimazak.gym.model.Trainee;
import com.dimazak.gym.model.Trainer;
import com.dimazak.gym.model.Training;
import com.dimazak.gym.security.JwtService;
import com.dimazak.gym.service.TraineeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trainees")
@Tag(name = "Trainee", description = "Trainee management endpoints")
public class TraineeController {

    private static final Logger log = LoggerFactory.getLogger(TraineeController.class);

    private final TraineeService traineeService;
    private final EntityMapper mapper;
    private final JwtService jwtService;

    public TraineeController(TraineeService traineeService,
                             EntityMapper mapper,
                             JwtService jwtService) {
        this.traineeService = traineeService;
        this.mapper = mapper;
        this.jwtService = jwtService;
    }

    @PostMapping
    @Operation(summary = "Register trainee",
            description = "Create a new trainee profile and receive a JWT token. No authentication required.")
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody TraineeRegistrationRequest request) {
        log.info("Registering new trainee: {} {}", request.firstName(), request.lastName());

        Trainee trainee = traineeService.createTrainee(
                request.firstName(), request.lastName(),
                request.dateOfBirth(), request.address());

        String token = jwtService.generateToken(
                trainee.getUser().getUsername(),
                trainee.getUser().getRole());

        RegistrationResponse response = new RegistrationResponse(
                trainee.getUser().getUsername(),
                trainee.getUser().getPassword(),
                token);

        log.info("Trainee registered successfully with username: {}", response.username());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{username}")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Get trainee profile", description = "Get trainee profile by username")
    public ResponseEntity<TraineeProfileResponse> getProfile(
            @Parameter(description = "Trainee username") @PathVariable String username) {
        log.info("Getting profile for trainee: {}", username);

        Trainee trainee = traineeService.getProfileByUsername(username);
        return ResponseEntity.ok(mapper.toTraineeProfileResponse(trainee));
    }

    @PutMapping("/{username}")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Update trainee profile")
    public ResponseEntity<UpdateTraineeResponse> updateProfile(
            @Parameter(description = "Trainee username") @PathVariable String username,
            @Valid @RequestBody UpdateTraineeRequest request) {
        log.info("Updating trainee profile: {}", username);

        Trainee trainee = traineeService.updateTrainee(
                username, request.firstName(), request.lastName(),
                request.dateOfBirth(), request.address(), request.isActive());

        return ResponseEntity.ok(mapper.toUpdateTraineeResponse(trainee));
    }

    @DeleteMapping("/{username}")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Delete trainee profile")
    public ResponseEntity<Void> deleteProfile(
            @Parameter(description = "Trainee username") @PathVariable String username) {
        log.info("Deleting trainee profile: {}", username);

        traineeService.deleteByUsername(username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/unassigned-trainers")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Get unassigned trainers")
    public ResponseEntity<List<TrainerSummary>> getUnassignedTrainers(
            @Parameter(description = "Trainee username") @PathVariable String username) {
        log.info("Getting unassigned trainers for trainee: {}", username);

        List<Trainer> trainers = traineeService.getUnassignedTrainers(username);
        return ResponseEntity.ok(trainers.stream().map(mapper::toTrainerSummary).toList());
    }

    @PutMapping("/{username}/trainers")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Update trainee's trainer list")
    public ResponseEntity<List<TrainerSummary>> updateTrainersList(
            @Parameter(description = "Trainee username") @PathVariable String username,
            @Valid @RequestBody UpdateTraineeTrainersRequest request) {
        log.info("Updating trainers list for trainee: {}", username);

        Trainee trainee = traineeService.updateTrainersList(username, request.trainerUsernames());

        List<TrainerSummary> response = trainee.getTrainers().stream()
                .map(mapper::toTrainerSummary)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}/trainings")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Get trainee trainings list")
    public ResponseEntity<List<TraineeTrainingResponse>> getTrainings(
            @Parameter(description = "Trainee username") @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingType) {
        log.info("Getting trainings for trainee: {} [from={}, to={}, trainer={}, type={}]",
                username, periodFrom, periodTo, trainerName, trainingType);

        List<Training> trainings = traineeService.getTraineeTrainings(
                username, periodFrom, periodTo, trainerName, trainingType);

        return ResponseEntity.ok(trainings.stream().map(mapper::toTraineeTrainingResponse).toList());
    }

    @PatchMapping("/{username}/activate")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Activate/De-activate trainee")
    public ResponseEntity<Void> updateActiveStatus(
            @Parameter(description = "Trainee username") @PathVariable String username,
            @Valid @RequestBody ActivateDeactivateRequest request) {
        log.info("Updating active status for trainee: {} to: {}", username, request.isActive());

        traineeService.setActiveStatus(username, request.isActive());
        return ResponseEntity.ok().build();
    }
}