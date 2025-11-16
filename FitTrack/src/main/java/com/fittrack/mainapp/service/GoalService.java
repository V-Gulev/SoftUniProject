package com.fittrack.mainapp.service;

import com.fittrack.mainapp.badge.dto.BadgeDto;
import com.fittrack.mainapp.model.dto.GoalDto;

import java.util.List;
import java.util.UUID;

public interface GoalService {

    void createGoal(GoalDto goalDto, String username);

    void updateGoal(UUID goalId, GoalDto goalDto, String username);

    void deleteGoal(UUID goalId, String username);

    GoalDto getGoalById(UUID goalId, String username);

    List<GoalDto> getGoalsForUser(String username);

    void completeGoal(UUID goalId, String username);

    BadgeDto getAndClearLastAwardedBadge();
}