package com.fittrack.mainapp.exceptions;

import com.fittrack.mainapp.model.dto.GoalDto;
import lombok.Getter;

@Getter
public class GoalException extends RuntimeException {

    private final GoalDto goalDto;

    public GoalException(String message, GoalDto goalDto) {
        super(message);
        this.goalDto = goalDto;
    }
}