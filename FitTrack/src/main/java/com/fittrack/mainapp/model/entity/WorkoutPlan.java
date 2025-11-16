package com.fittrack.mainapp.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "workout_plans")
public class WorkoutPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotEmpty
    @Size(min = 3, max = 50)
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String descriptionMonday;

    @Column(columnDefinition = "TEXT")
    private String descriptionTuesday;

    @Column(columnDefinition = "TEXT")
    private String descriptionWednesday;

    @Column(columnDefinition = "TEXT")
    private String descriptionThursday;

    @Column(columnDefinition = "TEXT")
    private String descriptionFriday;

    @Column(columnDefinition = "TEXT")
    private String descriptionSaturday;

    @Column(columnDefinition = "TEXT")
    private String descriptionSunday;

    @ManyToOne(optional = false)
    private User user;

}