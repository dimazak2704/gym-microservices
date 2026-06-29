package com.dimazak.workload.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "trainer_workload",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"username", "training_year", "training_month"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainerWorkload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "training_year", nullable = false)
    private int year;

    @Column(name = "training_month", nullable = false)
    private int month;

    @Column(name = "total_duration", nullable = false)
    private int totalDuration;
}