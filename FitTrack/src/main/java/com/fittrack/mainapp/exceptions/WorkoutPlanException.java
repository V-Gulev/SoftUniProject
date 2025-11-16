package com.fittrack.mainapp.exceptions;

import com.fittrack.mainapp.model.dto.WorkoutPlanDto;
import lombok.Getter;

@Getter
public class WorkoutPlanException extends RuntimeException {

    private final WorkoutPlanDto workoutPlanDto;

    public WorkoutPlanException(String message, WorkoutPlanDto workoutPlanDto) {
        super(message);
        this.workoutPlanDto = workoutPlanDto;
    }
}
