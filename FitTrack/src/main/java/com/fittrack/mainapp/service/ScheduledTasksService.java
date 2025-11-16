package com.fittrack.mainapp.service;

import com.fittrack.mainapp.model.entity.Goal;
import com.fittrack.mainapp.model.entity.User;
import com.fittrack.mainapp.model.enums.GoalStatus;
import com.fittrack.mainapp.repository.GoalRepository;
import com.fittrack.mainapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScheduledTasksService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTasksService.class);
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    public ScheduledTasksService(GoalRepository goalRepository, UserRepository userRepository) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "0 0 2 * * SUN")
    public void runWeeklySummaryTask() {
        LOGGER.info("Executing weekly summary task. It is now Sunday at 2 AM.");
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        long weeklyCompletedGoals = goalRepository.countByCompletedDateBetween(oneWeekAgo, LocalDateTime.now());
        LOGGER.info("Weekly summary: {} goals completed in the last 7 days.", weeklyCompletedGoals);
    }

    @Scheduled(fixedRate = 1800000)
    public void checkForInactiveUsers() {
        LOGGER.info("Executing check for inactive users.");
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
        List<User> loggedInUsers = userRepository.findByLoggedInTrue();

        for (User user : loggedInUsers) {
            if (user.getLastActivity() != null && user.getLastActivity().isBefore(thirtyMinutesAgo)) {
                user.setLoggedIn(false);
                userRepository.save(user);
                LOGGER.info("User {} has been logged out due to inactivity.", user.getUsername());
            }
        }
        LOGGER.info("Finished checking for inactive users.");
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void archiveOldGoals() {
        LOGGER.info("Executing daily task: Archiving old goals...");
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Goal> goalsToArchive = goalRepository.findByStatusAndArchivedFalseAndCompletedDateBefore(GoalStatus.COMPLETED, thirtyDaysAgo);

        if (goalsToArchive.isEmpty()) {
            LOGGER.info("No goals to archive.");
            return;
        }

        for (Goal goal : goalsToArchive) {
            goal.setArchived(true);
        }

        goalRepository.saveAll(goalsToArchive);
        LOGGER.info("Task finished. Archived {} goals.", goalsToArchive.size());
    }

    @Scheduled(fixedDelay = 600000)
    public void reportRecentlyCompletedGoals() {
        LOGGER.info("Executing scheduled report: Checking recently completed goals...");
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        long completedCount = goalRepository.countByCompletedDateAfter(tenMinutesAgo);
        LOGGER.info("Report finished. {} goals have been completed in the last 10 minutes.", completedCount);
    }
}