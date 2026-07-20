package com.dimazak.workload.repository;

import com.dimazak.workload.document.TrainerWorkloadDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TrainerWorkloadRepository
        extends MongoRepository<TrainerWorkloadDocument, String> {

    Optional<TrainerWorkloadDocument> findByUsername(String username);

    List<TrainerWorkloadDocument> findByFirstNameAndLastName(
            String firstName, String lastName);
}