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
            BadgeDto badge = awardBadgeIfNotExists(userId, "Goal Setter", "/img/GoalSetter.png");
            if (badge != null) return badge;
        }

        return switch (completedGoals) {
            case 1 -> awardBadgeIfNotExists(userId, "First Goal Completed", "/img/FirstGoalCompleted.png");
            case 5 -> awardBadgeIfNotExists(userId, "Goal Master", "/img/GoalMaster.png");
            case 10 -> awardBadgeIfNotExists(userId, "Goal Champion", "/img/GoalChampion.png");
            case 25 -> awardBadgeIfNotExists(userId, "Goal Legend", "/img/GoalLegend.png");
            case 50 -> awardBadgeIfNotExists(userId, "Goal Hero", "https://cdn-icons-png.flaticon.com/512/3135/3135799.png");
            case 100 -> awardBadgeIfNotExists(userId, "Goal God", "https://cdn-icons-png.flaticon.com/512/3135/3135743.png");
            default -> null;
        };
    }

    public BadgeDto checkWorkoutBadges(UUID userId, int totalWorkouts) {
        return switch (totalWorkouts) {
            case 1 -> awardBadgeIfNotExists(userId, "First Workout", "/img/FirstWorkout.png");
            case 10 -> awardBadgeIfNotExists(userId, "Workout Beginner", "/img/WorkoutBeginner.png");
            case 50 -> awardBadgeIfNotExists(userId, "Workout Warrior", "/img/WorkoutWarrior.png");
            case 100 -> awardBadgeIfNotExists(userId, "Workout Champion", "/img/WorkoutChampion.png");
            case 500 -> awardBadgeIfNotExists(userId, "Workout Legend", "https://cdn-icons-png.flaticon.com/512/3135/3135815.png");
            default -> null;
        };
    }

    public BadgeDto checkPlanBadges(UUID userId, int totalPlans) {
        if (totalPlans == 1) {
            return awardBadgeIfNotExists(userId, "Plan Creator", "/img/PlanCreator.png");
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