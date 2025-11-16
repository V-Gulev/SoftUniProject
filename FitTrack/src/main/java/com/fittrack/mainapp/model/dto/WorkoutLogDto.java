package com.fittrack.mainapp.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutLogDto {

    private UUID id;

    @NotNull(message = "Date cannot be null.")
    @PastOrPresent(message = "Workout date cannot be in the future.")
    private LocalDate date;

    private Integer durationMinutes;

    private UUID workoutPlanId;

    private String workoutPlanName;

    private String description;

    @NotNull(message = "Hours must be provided.")
    @Min(value = 0, message = "Hours cannot be negative.")
    private Integer hours;

    @NotNull(message = "Minutes must be provided.")
    @Min(value = 0, message = "Minutes cannot be negative.")
    private Integer minutes;
}