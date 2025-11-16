package com.fittrack.mainapp.service.impl;

import com.fittrack.mainapp.exceptions.GoalException;
import com.fittrack.mainapp.exceptions.ResourceNotFoundException;
import com.fittrack.mainapp.exceptions.UnauthorizedOperationException;
import com.fittrack.mainapp.model.dto.GoalDto;
import com.fittrack.mainapp.model.entity.Goal;
import com.fittrack.mainapp.model.entity.User;
import com.fittrack.mainapp.model.enums.GoalStatus;
import com.fittrack.mainapp.repository.GoalRepository;
import com.fittrack.mainapp.repository.UserRepository;
import com.fittrack.mainapp.badge.dto.BadgeDto;
import com.fittrack.mainapp.badge.service.BadgeAwardService;
import com.fittrack.mainapp.badge.service.BadgeNotificationService;
import com.fittrack.mainapp.service.GoalService;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GoalServiceImpl implements GoalService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoalServiceImpl.class);

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final BadgeAwardService badgeAwardService;
    private final BadgeNotificationService badgeNotificationService;

    public GoalServiceImpl(GoalRepository goalRepository, UserRepository userRepository, BadgeAwardService badgeAwardService, BadgeNotificationService badgeNotificationService) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
        this.badgeAwardService = badgeAwardService;
        this.badgeNotificationService = badgeNotificationService;
    }

    @Override
    public void createGoal(GoalDto goalDto, String username) {
        try {
            User user = findUserByUsername(username);

            Goal goal = new Goal();
            goal.setUser(user);
            goal.setName(goalDto.getName());
            goal.setDescription(goalDto.getDescription());
            goal.setCategory(goalDto.getCategory());
            goal.setStatus(goalDto.getStatus());
            goal.setTargetValue(goalDto.getTargetValue());
            goal.setCurrentValue(goalDto.getCurrentValue());
            goal.setUnit(goalDto.getUnit());
            goal.setStartDate(LocalDate.now());
            goal.setTargetDate(goalDto.getTargetDate());
            goal.setArchived(false);

            goalRepository.save(goal);

            setBadgeForNotification(username, user);
            LOGGER.info("Goal created for user '{}': {}", username, goalDto.getName());
        } catch (Exception e) {
            throw new GoalException("Error creating goal: " + e.getMessage(), goalDto);
        }
    }

    @Override
    public void updateGoal(UUID goalId, GoalDto goalDto, String username) {
        try {
            User user = findUserByUsername(username);
            Goal goal = findGoalByIdAndUser(goalId, user);
            goal.setName(goalDto.getName());
            goal.setDescription(goalDto.getDescription());
            goal.setCategory(goalDto.getCategory());
            goal.setStatus(goalDto.getStatus());
            goal.setTargetValue(goalDto.getTargetValue());
            goal.setCurrentValue(goalDto.getCurrentValue());
            goal.setUnit(goalDto.getUnit());
            goal.setTargetDate(goalDto.getTargetDate());

            boolean wasJustCompleted = goal.getStatus() != GoalStatus.COMPLETED && goalDto.getStatus() == GoalStatus.COMPLETED;

            if (goalDto.getStatus() == GoalStatus.COMPLETED) {
                if (goal.getCompletedDate() == null) {
                    goal.setCompletedDate(LocalDateTime.now());
                }
            } else {
                goal.setCompletedDate(null);
            }

            goalRepository.save(goal);

            if (wasJustCompleted) {
                setBadgeForNotification(username, user);
            }
            LOGGER.info("Goal updated for user '{}': {}", username, goalDto.getName());
        } catch (Exception e) {
            throw new GoalException("Error updating goal: " + e.getMessage(), goalDto);
        }
    }

    @Override
    public void deleteGoal(UUID goalId, String username) {
        User user = findUserByUsername(username);
        Goal goal = findGoalByIdAndUser(goalId, user);
        goalRepository.delete(goal);
        LOGGER.info("Goal with ID {} deleted for user '{}'", goalId, username);
    }

    @Override
    public GoalDto getGoalById(UUID goalId, String username) {
        User user = findUserByUsername(username);
        Goal goal = findGoalByIdAndUser(goalId, user);
        return mapToDto(goal);
    }

    @Override
    public List<GoalDto> getGoalsForUser(String username) {
        User user = findUserByUsername(username);
        return goalRepository.findByUser(user)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void completeGoal(UUID goalId, String username) {
        User user = findUserByUsername(username);
        Goal goal = findGoalByIdAndUser(goalId, user);

        goal.setStatus(GoalStatus.COMPLETED);
        goal.setCurrentValue(goal.getTargetValue());
        goal.setCompletedDate(LocalDateTime.now());

        goalRepository.saveAndFlush(goal);

        LOGGER.info("Goal completed for user '{}': {}", username, goal.getName());

        setBadgeForNotification(username, user);
    }

    private void setBadgeForNotification(String username, User user) {
        List<GoalDto> userGoals = getGoalsForUser(username);
        int totalGoals = userGoals.size();
        int completedGoals = (int) userGoals.stream().filter(g -> g.getStatus() == GoalStatus.COMPLETED).count();
        BadgeDto awardedBadge = badgeAwardService.checkGoalBadges(user.getId(), totalGoals, completedGoals);
        badgeNotificationService.setLastAwardedBadge(awardedBadge);
    }

    @Override
    public BadgeDto getAndClearLastAwardedBadge() {
        return badgeNotificationService.getAndClearLastAwardedBadge();
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    private Goal findGoalByIdAndUser(UUID goalId, User user) {
        return goalRepository.findByIdAndUser(goalId, user)
                .orElseThrow(() -> new UnauthorizedOperationException("Goal not found or you do not have permission to access it."));
    }

    private GoalDto mapToDto(Goal goal) {
        GoalDto dto = new GoalDto();
        dto.setId(goal.getId());
        dto.setName(goal.getName());
        dto.setDescription(goal.getDescription());
        dto.setCategory(goal.getCategory());
        dto.setStatus(goal.getStatus());
        dto.setTargetValue(goal.getTargetValue());
        dto.setCurrentValue(goal.getCurrentValue());
        dto.setUnit(goal.getUnit());
        dto.setTargetDate(goal.getTargetDate());
        return dto;
    }
}