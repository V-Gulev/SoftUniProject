package com.fittrack.mainapp.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "workout_logs")
public class WorkoutLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(nullable = false)
    private LocalDate date;

    @Positive
    @Column(nullable = false)
    private int durationMinutes;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne
    private WorkoutPlan workoutPlan;

    @Column(columnDefinition = "TEXT")
    private String description;

}