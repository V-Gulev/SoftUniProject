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

        BadgeDto awardedBadge = null;

        if (completedGoals == 1) {
            awardedBadge = awardBadgeIfNotExists(userId, "First Goal Completed", "/img/FirstGoalCompleted.png");
        } else if (completedGoals == 5) {
            awardedBadge = awardBadgeIfNotExists(userId, "Goal Master", "/img/GoalMaster.png");
        } else if (completedGoals == 10) {
            awardedBadge = awardBadgeIfNotExists(userId, "Goal Champion", "/img/GoalChampion.png");
        } else if (completedGoals == 25) {
            awardedBadge = awardBadgeIfNotExists(userId, "Goal Legend", "/img/GoalLegend.png");
        } else if (completedGoals == 50) {
            awardedBadge = awardBadgeIfNotExists(userId, "Goal Hero", "https://cdn-icons-png.flaticon.com/512/3135/3135799.png");
        } else if (completedGoals == 100) {
            awardedBadge = awardBadgeIfNotExists(userId, "Goal God", "https://cdn-icons-png.flaticon.com/512/3135/3135743.png");
        }

        return awardedBadge;
    }

    public BadgeDto checkWorkoutBadges(UUID userId, int totalWorkouts) {
        BadgeDto awardedBadge = null;

        if (totalWorkouts == 1) {
            awardedBadge = awardBadgeIfNotExists(userId, "First Workout", "/img/FirstWorkout.png");
        }
        if (totalWorkouts == 10) {
            awardedBadge = awardBadgeIfNotExists(userId, "Workout Beginner", "/img/WorkoutBeginner.png");
        }
        if (totalWorkouts == 50) {
            awardedBadge = awardBadgeIfNotExists(userId, "Workout Warrior", "/img/WorkoutWarrior.png");
        }
        if (totalWorkouts == 100) {
            awardedBadge = awardBadgeIfNotExists(userId, "Workout Champion", "/img/WorkoutChampion.png");
        }
        if (totalWorkouts == 500) {
            awardedBadge = awardBadgeIfNotExists(userId, "Workout Legend", "https://cdn-icons-png.flaticon.com/512/3135/3135815.png");
        }


        return awardedBadge;
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