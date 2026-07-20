package com.dimazak.workload.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "trainer_workload")
@CompoundIndex(name = "idx_first_last_name", def = "{'firstName': 1, 'lastName': 1}")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainerWorkloadDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String firstName;

    private String lastName;

    private boolean active;

    @Builder.Default
    private List<YearSummary> years = new ArrayList<>();
}