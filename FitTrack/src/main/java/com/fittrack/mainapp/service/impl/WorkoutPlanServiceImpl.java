package com.fittrack.mainapp.service.impl;

import com.fittrack.mainapp.exceptions.ResourceNotFoundException;
import com.fittrack.mainapp.exceptions.UnauthorizedOperationException;
import com.fittrack.mainapp.exceptions.WorkoutPlanException;
import com.fittrack.mainapp.model.dto.WorkoutPlanDto;
import com.fittrack.mainapp.model.entity.User;
import com.fittrack.mainapp.model.entity.WorkoutPlan;
import com.fittrack.mainapp.repository.UserRepository;
import com.fittrack.mainapp.repository.WorkoutPlanRepository;
import com.fittrack.mainapp.badge.dto.BadgeDto;
import com.fittrack.mainapp.badge.service.BadgeAwardService;
import com.fittrack.mainapp.badge.service.BadgeNotificationService;
import com.fittrack.mainapp.service.WorkoutPlanService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.Cacheable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WorkoutPlanServiceImpl implements WorkoutPlanService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkoutPlanServiceImpl.class);

    private final WorkoutPlanRepository workoutPlanRepository;
    private final UserRepository userRepository;
    private final BadgeAwardService badgeAwardService;
    private final BadgeNotificationService badgeNotificationService;

    public WorkoutPlanServiceImpl(WorkoutPlanRepository workoutPlanRepository, UserRepository userRepository, BadgeAwardService badgeAwardService, BadgeNotificationService badgeNotificationService) {
        this.workoutPlanRepository = workoutPlanRepository;
        this.userRepository = userRepository;
        this.badgeAwardService = badgeAwardService;
        this.badgeNotificationService = badgeNotificationService;
    }

    @Override
    public void createPlan(WorkoutPlanDto planDto, String username) {
        try {
            User user = findUserByUsername(username);
            WorkoutPlan newPlan = new WorkoutPlan();
            newPlan.setName(planDto.getName());
            newPlan.setDescription(planDto.getDescription());
            newPlan.setDescriptionMonday(planDto.getDescriptionMonday());
            newPlan.setDescriptionTuesday(planDto.getDescriptionTuesday());
            newPlan.setDescriptionWednesday(planDto.getDescriptionWednesday());
            newPlan.setDescriptionThursday(planDto.getDescriptionThursday());
            newPlan.setDescriptionFriday(planDto.getDescriptionFriday());
            newPlan.setDescriptionSaturday(planDto.getDescriptionSaturday());
            newPlan.setDescriptionSunday(planDto.getDescriptionSunday());
            newPlan.setUser(user);
            WorkoutPlan savedPlan = workoutPlanRepository.save(newPlan);

            if (user.getActiveWorkoutPlan() == null) {
                user.setActiveWorkoutPlan(savedPlan);
                userRepository.save(user);
            }

            int totalPlans = workoutPlanRepository.findByUser(user).size();
            if (user.getId() != null) {
                BadgeDto awardedBadge = badgeAwardService.checkPlanBadges(user.getId(), totalPlans);
                if (awardedBadge != null) {
                    badgeNotificationService.setLastAwardedBadge(awardedBadge);
                }
            }
            LOGGER.info("Workout plan '{}' created for user '{}'", newPlan.getName(), username);
        } catch (Exception e) {
            throw new WorkoutPlanException("Error creating workout plan: " + e.getMessage(), planDto);
        }
    }

    @Override
    @CacheEvict(value = "workoutPlans", key = "#planId")
    public void updatePlan(UUID planId, WorkoutPlanDto planDto, String username) {
        try {
            LOGGER.info("Updating workout plan {}. Evicting from cache.", planId);
            WorkoutPlan plan = findPlanById(planId);
            if (!plan.getUser().getUsername().equals(username)) {
                throw new UnauthorizedOperationException("You are not authorized to edit this plan.");
            }
            plan.setName(planDto.getName());
            plan.setDescription(planDto.getDescription());
            plan.setDescriptionMonday(planDto.getDescriptionMonday());
            plan.setDescriptionTuesday(planDto.getDescriptionTuesday());
            plan.setDescriptionWednesday(planDto.getDescriptionWednesday());
            plan.setDescriptionThursday(planDto.getDescriptionThursday());
            plan.setDescriptionFriday(planDto.getDescriptionFriday());
            plan.setDescriptionSaturday(planDto.getDescriptionSaturday());
            plan.setDescriptionSunday(planDto.getDescriptionSunday());
            workoutPlanRepository.save(plan);
        } catch (Exception e) {
            throw new WorkoutPlanException("Error updating workout plan: " + e.getMessage(), planDto);
        }
    }

    @Override
    @CacheEvict(value = "workoutPlans", key = "#planId")
    public void deletePlan(UUID planId, String username) {
        LOGGER.info("Deleting workout plan {}. Evicting from cache.", planId);

        try {
            User user = userRepository.getUserByUsername(username);

            WorkoutPlan planToDelete = (WorkoutPlan) workoutPlanRepository.findByIdAndUser(planId, user)
                    .orElseThrow(() -> new EntityNotFoundException("Workout plan not found or user not authorized"));

            if (user.getActiveWorkoutPlan() != null && user.getActiveWorkoutPlan().getId().equals(planToDelete.getId())) {
                user.setActiveWorkoutPlan(null);
                userRepository.save(user);
                LOGGER.info("Cleared active workout plan reference for user '{}'", username);
            }

            workoutPlanRepository.delete(planToDelete);
            LOGGER.info("Successfully deleted workout plan '{}' for user '{}'", planToDelete.getName(), username);

        } catch (EntityNotFoundException e) {
            LOGGER.warn("Attempted to delete workout plan {} that does not exist for user '{}'", planId, username);
            throw e;
        }
    }

    @Override
    @Cacheable(value = "workoutPlans", key = "#planId")
    public WorkoutPlanDto getPlanById(UUID planId, String username) {
        LOGGER.info("Fetching workout plan {} from DATABASE (cache miss).", planId);
        WorkoutPlan plan = findPlanById(planId);
        validateUserOwnership(plan, username);
        return mapToDto(plan);
    }

    @Override
    public List<WorkoutPlanDto> getPlansForUser(String username) {
        User user = findUserByUsername(username);
        return workoutPlanRepository.findByUser(user)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkoutPlanDto> getInactivePlansForUser(String username) {
        User user = findUserByUsername(username);
        WorkoutPlan activePlan = user.getActiveWorkoutPlan();

        return workoutPlanRepository.findByUser(user)
                .stream()
                .filter(plan -> activePlan == null || !plan.getId().equals(activePlan.getId()))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void setActivePlan(UUID planId, String username) {
        User user = findUserByUsername(username);
        WorkoutPlan plan = findPlanById(planId);
        validateUserOwnership(plan, username);
        user.setActiveWorkoutPlan(plan);
        userRepository.save(user);
        LOGGER.info("Set active workout plan '{}' for user '{}'", plan.getName(), username);
    }

    @Override
    public WorkoutPlanDto getActivePlan(String username) {
        User user = findUserByUsername(username);
        if (user.getActiveWorkoutPlan() != null) {
            return mapToDto(user.getActiveWorkoutPlan());
        }
        return null;
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private WorkoutPlan findPlanById(UUID planId) {
        return workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout plan not found with ID: " + planId));
    }

    private WorkoutPlanDto mapToDto(WorkoutPlan plan) {
        WorkoutPlanDto dto = new WorkoutPlanDto();
        dto.setId(plan.getId());
        dto.setName(plan.getName());
        dto.setDescription(plan.getDescription());
        dto.setDescriptionMonday(plan.getDescriptionMonday());
        dto.setDescriptionTuesday(plan.getDescriptionTuesday());
        dto.setDescriptionWednesday(plan.getDescriptionWednesday());
        dto.setDescriptionThursday(plan.getDescriptionThursday());
        dto.setDescriptionFriday(plan.getDescriptionFriday());
        dto.setDescriptionSaturday(plan.getDescriptionSaturday());
        dto.setDescriptionSunday(plan.getDescriptionSunday());
        return dto;
    }

    private void validateUserOwnership(WorkoutPlan plan, String username) {
        if (!plan.getUser().getUsername().equals(username)) {
            throw new UnauthorizedOperationException("You are not authorized to view this plan.");
        }
    }
}