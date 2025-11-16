package com.fittrack.mainapp.model.dto;

import com.fittrack.mainapp.badge.dto.BadgeDto;
import com.fittrack.mainapp.model.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProfileViewDto {

    private User userProfile;

    private long totalGoals;

    private long activeGoals;

    private long completedGoals;

    private int totalPlans;

    private int totalLogs;

    private WorkoutPlanDto activePlan;

    private List<BadgeDto> badges;
}