package com.fittrack.mainapp.exceptions;

import com.fittrack.mainapp.model.dto.WorkoutLogDto;
import lombok.Getter;

@Getter
public class WorkoutLogException extends RuntimeException {

    private final WorkoutLogDto workoutLogDto;

    public WorkoutLogException(String message, WorkoutLogDto workoutLogDto) {
        super(message);
        this.workoutLogDto = workoutLogDto;
    }
}