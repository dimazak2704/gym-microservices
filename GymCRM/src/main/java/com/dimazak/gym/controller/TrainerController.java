package com.dimazak.gym.controller;

import com.dimazak.gym.dto.*;
import com.dimazak.gym.mapper.EntityMapper;
import com.dimazak.gym.model.Trainer;
import com.dimazak.gym.model.Training;
import com.dimazak.gym.security.JwtService;
import com.dimazak.gym.service.TrainerService;
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
@RequestMapping("/api/trainers")
@Tag(name = "Trainer", description = "Trainer management endpoints")
public class TrainerController {

    private static final Logger log = LoggerFactory.getLogger(TrainerController.class);

    private final TrainerService trainerService;
    private final EntityMapper mapper;
    private final JwtService jwtService;

    public TrainerController(TrainerService trainerService,
                             EntityMapper mapper,
                             JwtService jwtService) {
        this.trainerService = trainerService;
        this.mapper = mapper;
        this.jwtService = jwtService;
    }

    @PostMapping
    @Operation(summary = "Register trainer",
            description = "Create a new trainer profile and receive a JWT token. No authentication required.")
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody TrainerRegistrationRequest request) {
        log.info("Registering new trainer: {} {}", request.firstName(), request.lastName());

        Trainer trainer = trainerService.createTrainer(
                request.firstName(), request.lastName(), request.specializationId());

        String token = jwtService.generateToken(
                trainer.getUser().getUsername(),
                trainer.getUser().getRole());

        RegistrationResponse response = new RegistrationResponse(
                trainer.getUser().getUsername(),
                trainer.getUser().getPassword(),
                token);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{username}")
    @PreAuthorize("hasRole('TRAINER')")
    @Operation(summary = "Get trainer profile")
    public ResponseEntity<TrainerProfileResponse> getProfile(
            @Parameter(description = "Trainer username") @PathVariable String username) {
        log.info("Getting profile for trainer: {}", username);

        Trainer trainer = trainerService.getProfileByUsername(username);
        return ResponseEntity.ok(mapper.toTrainerProfileResponse(trainer));
    }

    @PutMapping("/{username}")
    @PreAuthorize("hasRole('TRAINER')")
    @Operation(summary = "Update trainer profile")
    public ResponseEntity<UpdateTrainerResponse> updateProfile(
            @Parameter(description = "Trainer username") @PathVariable String username,
            @Valid @RequestBody UpdateTrainerRequest request) {
        log.info("Updating trainer profile: {}", username);

        Trainer trainer = trainerService.updateTrainerProfile(
                username, request.firstName(), request.lastName(), request.isActive());

        return ResponseEntity.ok(mapper.toUpdateTrainerResponse(trainer));
    }

    @GetMapping("/{username}/trainings")
    @PreAuthorize("hasRole('TRAINER')")
    @Operation(summary = "Get trainer trainings list")
    public ResponseEntity<List<TrainerTrainingResponse>> getTrainings(
            @Parameter(description = "Trainer username") @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String traineeName) {
        log.info("Getting trainings for trainer: {}", username);

        List<Training> trainings = trainerService.getTrainerTrainings(
                username, periodFrom, periodTo, traineeName);

        return ResponseEntity.ok(trainings.stream().map(mapper::toTrainerTrainingResponse).toList());
    }

    @PatchMapping("/{username}/activate")
    @PreAuthorize("hasRole('TRAINER')")
    @Operation(summary = "Activate/De-activate trainer")
    public ResponseEntity<Void> updateActiveStatus(
            @Parameter(description = "Trainer username") @PathVariable String username,
            @Valid @RequestBody ActivateDeactivateRequest request) {
        log.info("Updating active status for trainer: {} to: {}", username, request.isActive());

        trainerService.setActiveStatus(username, request.isActive());
        return ResponseEntity.ok().build();
    }
}