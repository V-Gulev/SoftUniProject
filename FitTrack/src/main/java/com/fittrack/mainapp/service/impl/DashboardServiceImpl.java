package com.fittrack.mainapp.service.impl;

import com.fittrack.mainapp.model.dto.DashboardSummaryDto;
import com.fittrack.mainapp.model.dto.GoalDto;
import com.fittrack.mainapp.model.dto.WorkoutLogDto;
import com.fittrack.mainapp.model.dto.WorkoutPlanDto;
import com.fittrack.mainapp.model.enums.GoalStatus;
import com.fittrack.mainapp.service.DashboardService;
import com.fittrack.mainapp.service.GoalService;
import com.fittrack.mainapp.service.WorkoutLogService;
import com.fittrack.mainapp.service.WorkoutPlanService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final GoalService goalService;
    private final WorkoutPlanService workoutPlanService;
    private final WorkoutLogService workoutLogService;

    public DashboardServiceImpl(GoalService goalService,
                                WorkoutPlanService workoutPlanService,
                                WorkoutLogService workoutLogService) {

        this.goalService = goalService;
        this.workoutPlanService = workoutPlanService;
        this.workoutLogService = workoutLogService;
    }

    @Override
    public DashboardSummaryDto buildDashboard(String username) {
        List<GoalDto> goals = goalService.getGoalsForUser(username);
        long completedGoals = goals.stream().filter(g -> g.getStatus() == GoalStatus.COMPLETED).count();
        long activeGoals = goals.stream().filter(g -> g.getStatus() == GoalStatus.ACTIVE).count();

        List<WorkoutPlanDto> plans = workoutPlanService.getPlansForUser(username);
        List<WorkoutLogDto> logs = workoutLogService.getLogsForUser(username);

        List<WorkoutLogDto> recentLogs = logs.stream()
                .sorted(Comparator.comparing(WorkoutLogDto::getDate).reversed())
                .limit(5)
                .collect(Collectors.toList());

        List<GoalDto> recentGoals = goals.stream()
                .filter(g -> g.getStatus() == GoalStatus.ACTIVE)
                .sorted(Comparator.comparing(GoalDto::getTargetDate))
                .limit(5)
                .collect(Collectors.toList());

        DashboardSummaryDto dto = new DashboardSummaryDto();
        dto.setGoals(goals);
        dto.setCompletedGoalsCount(completedGoals);
        dto.setActiveGoalsCount(activeGoals);
        dto.setPlans(plans);
        dto.setLogs(logs);
        dto.setRecentLogs(recentLogs);
        dto.setRecentGoals(recentGoals);
        return dto;

    }
}