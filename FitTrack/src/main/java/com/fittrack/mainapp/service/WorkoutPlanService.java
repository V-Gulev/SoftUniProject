package com.fittrack.mainapp.service;

import com.fittrack.mainapp.model.dto.WorkoutPlanDto;

import java.util.List;
import java.util.UUID;

public interface WorkoutPlanService {
    void createPlan(WorkoutPlanDto planDto, String username);

    void updatePlan(UUID planId, WorkoutPlanDto planDto, String username);

    void deletePlan(UUID planId, String username);

    WorkoutPlanDto getPlanById(UUID planId, String username);

    List<WorkoutPlanDto> getPlansForUser(String username);

    List<WorkoutPlanDto> getInactivePlansForUser(String username);

    void setActivePlan(UUID planId, String username);

    WorkoutPlanDto getActivePlan(String username);
}