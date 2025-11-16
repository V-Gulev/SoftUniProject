package com.fittrack.mainapp.model.dto;

import com.fittrack.mainapp.model.enums.GoalCategory;
import com.fittrack.mainapp.model.enums.GoalStatus;
import com.fittrack.mainapp.model.enums.GoalUnit;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class GoalDto {

    private UUID id;

    @NotEmpty(message = "Goal name cannot be empty.")
    private String name;

    private String description;

    @NotNull(message = "Category must be selected.")
    private GoalCategory category;

    @NotNull(message = "Status must be set.")
    private GoalStatus status = GoalStatus.ACTIVE;

    @NotNull(message = "Target value must be set.")
    @Positive(message = "Target value must be positive.")
    private Double targetValue;

    @NotNull(message = "Current value must be set.")
    private Double currentValue;

    @NotNull(message = "Unit (e.g., kg, km) must be set.")
    private GoalUnit unit;

    @NotNull(message = "Target date must be set.")
    @FutureOrPresent(message = "Target date must be in the present or future.")
    private LocalDate targetDate;
}