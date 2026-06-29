package com.dimazak.gym.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "trainers")
public class Trainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "specialization_id", nullable = false)
    private TrainingType specialization;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToMany(mappedBy = "trainers")
    private List<Trainee> trainees = new ArrayList<>();

    @OneToMany(mappedBy = "trainer")
    private List<Training> trainings = new ArrayList<>();

    public Trainer() {}

    public Trainer(Long id, TrainingType specialization, User user) {
        this.id = id;
        this.specialization = specialization;
        this.user = user;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TrainingType getSpecialization() { return specialization; }
    public void setSpecialization(TrainingType specialization) {
        this.specialization = specialization;
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<Trainee> getTrainees() { return trainees; }
    public void setTrainees(List<Trainee> trainees) { this.trainees = trainees; }

    public List<Training> getTrainings() { return trainings; }
    public void setTrainings(List<Training> trainings) { this.trainings = trainings; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trainer trainer = (Trainer) o;
        return Objects.equals(id, trainer.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Trainer{id=" + id + ", specialization=" + specialization +
                ", user=" + user + '}';
    }
}