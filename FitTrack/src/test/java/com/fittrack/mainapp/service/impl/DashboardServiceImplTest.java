package com.fittrack.mainapp.service.impl;

import com.fittrack.mainapp.model.dto.DashboardSummaryDto;
import com.fittrack.mainapp.model.dto.GoalDto;
import com.fittrack.mainapp.model.enums.GoalStatus;
import com.fittrack.mainapp.service.GoalService;
import com.fittrack.mainapp.service.WorkoutLogService;
import com.fittrack.mainapp.service.WorkoutPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    private DashboardServiceImpl dashboardService;

    @Mock
    private GoalService mockGoalService;
    @Mock
    private WorkoutPlanService mockWorkoutPlanService;
    @Mock
    private WorkoutLogService mockWorkoutLogService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardServiceImpl(
                mockGoalService,
                mockWorkoutPlanService,
                mockWorkoutLogService
        );
    }

    @Test
    void testBuildDashboard_ShouldAggregateDataCorrectly() {
        String username = "testuser";
        GoalDto completedGoal = new GoalDto();
        completedGoal.setStatus(GoalStatus.COMPLETED);
        GoalDto activeGoal = new GoalDto();
        activeGoal.setStatus(GoalStatus.ACTIVE);

        when(mockGoalService.getGoalsForUser(username)).thenReturn(List.of(completedGoal, activeGoal));
        when(mockWorkoutPlanService.getPlansForUser(username)).thenReturn(Collections.emptyList());
        when(mockWorkoutLogService.getLogsForUser(username)).thenReturn(Collections.emptyList());

        DashboardSummaryDto result = dashboardService.buildDashboard(username);

        assertNotNull(result);
        assertEquals(2, result.getGoals().size());
        assertEquals(1, result.getCompletedGoalsCount());
        assertEquals(1, result.getActiveGoalsCount());
        assertEquals(0, result.getPlans().size());
    }
}
