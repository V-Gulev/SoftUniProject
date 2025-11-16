package com.fittrack.mainapp.model.entity;

import com.fittrack.mainapp.model.enums.GoalCategory;
import com.fittrack.mainapp.model.enums.GoalStatus;
import com.fittrack.mainapp.model.enums.GoalUnit;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "goals")
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotEmpty
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalStatus status = GoalStatus.ACTIVE;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalCategory category;

    @Column
    private boolean archived = false;

    @NotNull
    @Positive
    private Double targetValue;

    @NotNull
    private Double currentValue;

    @NotNull
    @Enumerated(EnumType.STRING)
    private GoalUnit unit;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate targetDate;

    @Column
    private LocalDateTime completedDate;

    @ManyToOne(optional = false)
    private User user;

}