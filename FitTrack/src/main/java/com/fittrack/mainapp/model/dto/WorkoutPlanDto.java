package com.fittrack.mainapp.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutPlanDto {

    private UUID id;

    @NotEmpty(message = "Plan name cannot be empty.")
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters.")
    private String name;

    private String description;

    private String descriptionMonday;
    private String descriptionTuesday;
    private String descriptionWednesday;
    private String descriptionThursday;
    private String descriptionFriday;
    private String descriptionSaturday;
    private String descriptionSunday;
}