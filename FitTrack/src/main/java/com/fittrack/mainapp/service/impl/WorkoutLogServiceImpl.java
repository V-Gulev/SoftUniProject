package com.fittrack.mainapp.service.impl;

import com.fittrack.mainapp.exceptions.ResourceNotFoundException;
import com.fittrack.mainapp.exceptions.UnauthorizedOperationException;
import com.fittrack.mainapp.exceptions.WorkoutLogException;
import com.fittrack.mainapp.model.dto.WorkoutLogDto;
import com.fittrack.mainapp.model.entity.User;
import com.fittrack.mainapp.model.entity.WorkoutLog;
import com.fittrack.mainapp.model.entity.WorkoutPlan;
import com.fittrack.mainapp.repository.UserRepository;
import com.fittrack.mainapp.repository.WorkoutLogRepository;
import com.fittrack.mainapp.repository.WorkoutPlanRepository;
import com.fittrack.mainapp.badge.dto.BadgeDto;
import com.fittrack.mainapp.badge.service.BadgeAwardService;
import com.fittrack.mainapp.badge.service.BadgeNotificationService;
import com.fittrack.mainapp.service.WorkoutLogService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WorkoutLogServiceImpl implements WorkoutLogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkoutLogServiceImpl.class);

    private final UserRepository userRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final BadgeAwardService badgeAwardService;
    private final BadgeNotificationService badgeNotificationService;

    public WorkoutLogServiceImpl(UserRepository userRepository, WorkoutLogRepository workoutLogRepository, WorkoutPlanRepository workoutPlanRepository, BadgeAwardService badgeAwardService, BadgeNotificationService badgeNotificationService) {
        this.userRepository = userRepository;
        this.workoutLogRepository = workoutLogRepository;
        this.workoutPlanRepository = workoutPlanRepository;
        this.badgeAwardService = badgeAwardService;
        this.badgeNotificationService = badgeNotificationService;
    }

    @Override
    @Transactional
    public void logWorkout(WorkoutLogDto logDto, String username) {
        try {
            User user = findUserByUsername(username);

            WorkoutLog newLog = new WorkoutLog();
            newLog.setUser(user);

            int totalMinutes = getTotalMinutes(logDto);

            newLog.setDurationMinutes(totalMinutes);
            newLog.setDate(logDto.getDate());
            newLog.setDescription(logDto.getDescription());

            if (logDto.getWorkoutPlanId() != null) {
                WorkoutPlan plan = workoutPlanRepository.findById(logDto.getWorkoutPlanId())
                        .orElseThrow(() -> new ResourceNotFoundException("Workout Plan not found with ID: " + logDto.getWorkoutPlanId()));

                if (!plan.getUser().equals(user)) {
                    throw new UnauthorizedOperationException("Cannot log workout with a plan that does not belong to you.");
                }
                newLog.setWorkoutPlan(plan);
            }

            workoutLogRepository.saveAndFlush(newLog);
            LOGGER.info("Workout logged for user '{}' on date {}", username, newLog.getDate());

            int totalWorkouts = workoutLogRepository.findByUser(user).size();
            if (user.getId() != null) {
                BadgeDto awardedBadge = badgeAwardService.checkWorkoutBadges(user.getId(), totalWorkouts);
                if (awardedBadge != null) {
                    badgeNotificationService.setLastAwardedBadge(awardedBadge);
                }
            }
        } catch (Exception e) {
            throw new WorkoutLogException("Error logging workout: " + e.getMessage(), logDto);
        }
    }

    private static int getTotalMinutes(WorkoutLogDto logDto) {
        int totalMinutes = 0;
        if (logDto.getHours() != null) {
            totalMinutes += logDto.getHours() * 60;
        }
        if (logDto.getMinutes() != null) {
            totalMinutes += logDto.getMinutes();
        }

        if (totalMinutes <= 0) {
            if (logDto.getDurationMinutes() != null && logDto.getDurationMinutes() > 0) {
                totalMinutes = logDto.getDurationMinutes();
            } else {
                throw new IllegalArgumentException("Workout duration must be greater than 0 minutes.");
            }
        }
        return totalMinutes;
    }

    @Override
    public List<WorkoutLogDto> getLogsForUser(String username) {
        User user = findUserByUsername(username);
        return workoutLogRepository.findByUserOrderByDateDesc(user)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public WorkoutLogDto getLogById(UUID logId, String username) {
        User user = findUserByUsername(username);
        WorkoutLog log = workoutLogRepository.findByIdAndUser(logId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Workout log not found or you are not authorized to view it."));
        return mapToDto(log);
    }

    @Override
    @Transactional
    public void updateLog(WorkoutLogDto logDto, String username) {
        try {
            User user = findUserByUsername(username);
            WorkoutLog logToUpdate = workoutLogRepository.findByIdAndUser(logDto.getId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Workout log not found or you are not authorized to update it."));

            int totalMinutes = 0;
            if (logDto.getHours() != null) {
                totalMinutes += logDto.getHours() * 60;
            }
            if (logDto.getMinutes() != null) {
                totalMinutes += logDto.getMinutes();
            }
            if (totalMinutes <= 0) {
                throw new IllegalArgumentException("Workout duration must be greater than 0 minutes.");
            }

            logToUpdate.setDate(logDto.getDate());
            logToUpdate.setDurationMinutes(totalMinutes);
            logToUpdate.setDescription(logDto.getDescription());

            if (logDto.getWorkoutPlanId() != null) {
                WorkoutPlan plan = workoutPlanRepository.findById(logDto.getWorkoutPlanId())
                        .orElseThrow(() -> new ResourceNotFoundException("Workout Plan not found with ID: " + logDto.getWorkoutPlanId()));
                if (!plan.getUser().equals(user)) {
                    throw new UnauthorizedOperationException("Cannot log workout with a plan that does not belong to you.");
                }
                logToUpdate.setWorkoutPlan(plan);
            } else {
                logToUpdate.setWorkoutPlan(null);
            }

            workoutLogRepository.save(logToUpdate);
            LOGGER.info("Workout log {} updated for user '{}'", logToUpdate.getId(), username);
        } catch (Exception e) {
            throw new WorkoutLogException("Error updating workout log: " + e.getMessage(), logDto);
        }
    }

    @Override
    @Transactional
    public void deleteLog(UUID logId, String username) {
        try {
            User user = findUserByUsername(username);
            WorkoutLog logToDelete = workoutLogRepository.findByIdAndUser(logId, user)
                    .orElseThrow(() -> new ResourceNotFoundException("Workout log not found or you are not authorized to delete it."));

            workoutLogRepository.delete(logToDelete);
            LOGGER.info("Workout log {} deleted for user '{}'", logId, username);
        } catch (Exception e) {
            throw new WorkoutLogException("Error deleting workout log: " + e.getMessage(), new WorkoutLogDto()); // Pass a dummy DTO for deletion error
        }
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private WorkoutLogDto mapToDto(WorkoutLog log) {
        WorkoutLogDto dto = new WorkoutLogDto();
        dto.setId(log.getId());
        dto.setDate(log.getDate());
        dto.setDurationMinutes(log.getDurationMinutes());
        if (log.getWorkoutPlan() != null) {
            dto.setWorkoutPlanId(log.getWorkoutPlan().getId());
            dto.setWorkoutPlanName(log.getWorkoutPlan().getName());
        }
        dto.setDescription(log.getDescription());

        int totalMinutes = log.getDurationMinutes();
        dto.setHours(totalMinutes / 60);
        dto.setMinutes(totalMinutes % 60);

        return dto;
    }

    @Override
    public BadgeDto getAndClearLastAwardedBadge() {
        return badgeNotificationService.getAndClearLastAwardedBadge();
    }
}