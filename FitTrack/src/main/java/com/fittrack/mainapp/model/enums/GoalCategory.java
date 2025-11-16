package com.fittrack.mainapp.model.enums;

import lombok.Getter;

@Getter
public enum GoalCategory {
    WEIGHT_LOSS("Weight Loss", "#ef4444"),
    STRENGTH("Strength", "#f97316"),
    CARDIO("Cardio", "#22c55e"),
    ENDURANCE("Endurance", "#3b82f6"),
    FLEXIBILITY("Flexibility", "#a855f7"),
    OTHER("Other", "#64748b");

    private final String displayName;
    private final String color;

    GoalCategory(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

}