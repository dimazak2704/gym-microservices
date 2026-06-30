package com.dimazak.workload.repository;

import com.dimazak.workload.entity.TrainerWorkload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrainerWorkloadRepository
        extends JpaRepository<TrainerWorkload, Long> {

    Optional<TrainerWorkload> findByUsernameAndYearAndMonth(
            String username, int year, int month);

    List<TrainerWorkload> findByUsername(String username);

    List<TrainerWorkload> findByUsernameAndYear(String username, int year);
}