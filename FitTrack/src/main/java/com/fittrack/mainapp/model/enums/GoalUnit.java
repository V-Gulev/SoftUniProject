package com.fittrack.mainapp.model.enums;

import lombok.Getter;

@Getter
public enum GoalUnit {
    KILOGRAMS("kg"),
    POUNDS("lbs"),
    REPS("reps"),
    SECONDS("seconds"),
    MINUTES("minutes"),
    HOURS("hours"),
    KILOMETERS("km"),
    MILES("miles"),
    STEPS("steps"),
    CALORIES("calories");

    private final String displayName;

    GoalUnit(String displayName) {
        this.displayName = displayName;
    }

}