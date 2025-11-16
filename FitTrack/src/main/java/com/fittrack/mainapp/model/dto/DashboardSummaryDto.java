package com.fittrack.mainapp.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DashboardSummaryDto {
    private List<GoalDto> goals;

    private long completedGoalsCount;

    private long activeGoalsCount;

    private List<WorkoutPlanDto> plans;

    private List<WorkoutLogDto> logs;

    private List<WorkoutLogDto> recentLogs;

    private List<GoalDto> recentGoals;
}