package com.fittrack.mainapp.badge.service;

import com.fittrack.mainapp.badge.client.BadgeServiceClient;
import com.fittrack.mainapp.badge.dto.BadgeAwardDto;
import com.fittrack.mainapp.badge.dto.BadgeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BadgeAwardService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BadgeAwardService.class);

    private final BadgeServiceClient badgeServiceClient;

    public BadgeAwardService(BadgeServiceClient badgeServiceClient) {
        this.badgeServiceClient = badgeServiceClient;
    }

    private boolean userHasBadge(UUID userId, String badgeName) {
        try {
            List<BadgeDto> userBadges = badgeServiceClient.getBadgesForUser(userId);
            return userBadges.stream().anyMatch(badge -> badgeName.equals(badge.getName()));
        } catch (Exception e) {
            LOGGER.warn("Could not check badges for user {}: {}", userId, e.getMessage());
            return false;
        }
    }

    public BadgeDto awardBadgeIfNotExists(UUID userId, String badgeName, String iconUrl) {
        try {
            if (!userHasBadge(userId, badgeName)) {
                BadgeAwardDto badgeAwardDto = new BadgeAwardDto(badgeName, iconUrl, userId);
                BadgeDto awardedBadge = badgeServiceClient.awardBadge(badgeAwardDto);
                LOGGER.info("Awarded badge '{}' to user {}", badgeName, userId);
                return awardedBadge;
            }
        } catch (Exception e) {
            LOGGER.warn("Could not award badge '{}' to user {}: {}", badgeName, userId, e.getMessage());
        }
        return null;
    }

    public BadgeDto checkGoalBadges(UUID userId, int totalGoals, int completedGoals) {
        if (totalGoals == 1) {
            BadgeDto badge = awardBadgeIfNotExists(userId, "Goal Setter", "/images/GoalSetter.png");
            if (badge != null)
                return badge;
        }

        BadgeDto awardedBadge = null;

        if (completedGoals == 1) {
            awardedBadge = awardBadgeIfNotExists(userId, "First Goal Completed", "/images/FirstGoalCompleted.png");
        } else if (completedGoals == 5) {
            awardedBadge = awardBadgeIfNotExists(userId, "Goal Master", "/images/GoalMaster.png");
        } else if (completedGoals == 10) {
            awardedBadge = awardBadgeIfNotExists(userId, "Goal Champion", "/images/GoalChampion.png");
        } else if (completedGoals == 25) {
            awardedBadge = awardBadgeIfNotExists(userId, "Goal Legend", "/images/GoalLegend.png");
        }

        return awardedBadge;
    }

    public BadgeDto checkWorkoutBadges(UUID userId, int totalWorkouts) {
        BadgeDto awardedBadge = null;

        if (totalWorkouts == 1) {
            awardedBadge = awardBadgeIfNotExists(userId, "First Workout", "/images/FirstWorkout.png");
        }else if (totalWorkouts == 10) {
            awardedBadge = awardBadgeIfNotExists(userId, "Workout Beginner", "/images/WorkoutBeginner.png");
        }else if (totalWorkouts == 50) {
            awardedBadge = awardBadgeIfNotExists(userId, "Workout Warrior", "/images/WorkoutWarrior.png");
        }else if (totalWorkouts == 100) {
            awardedBadge = awardBadgeIfNotExists(userId, "Workout Champion", "/images/WorkoutChampion.png");
        }

        return awardedBadge;
    }

    public BadgeDto checkPlanBadges(UUID userId, int totalPlans) {
        if (totalPlans == 1) {
            return awardBadgeIfNotExists(userId, "Plan Creator", "/images/PlanCreator.png");
        }
        return null;
    }

    public void revokeBadge(UUID badgeId) {
        try {
            badgeServiceClient.deleteBadge(badgeId);
            LOGGER.info("Revoked badge with ID {}", badgeId);
        } catch (Exception e) {
            LOGGER.warn("Could not revoke badge with ID {}: {}", badgeId, e.getMessage());
        }
    }
}