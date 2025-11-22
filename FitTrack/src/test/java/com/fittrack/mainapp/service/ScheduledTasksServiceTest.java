package com.fittrack.mainapp.service;

import com.fittrack.mainapp.model.entity.Goal;
import com.fittrack.mainapp.model.enums.GoalStatus;
import com.fittrack.mainapp.repository.GoalRepository;
import com.fittrack.mainapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledTasksServiceTest {

    private ScheduledTasksService scheduledTasksService;

    @Mock
    private GoalRepository mockGoalRepository;
    @Mock
    private UserRepository mockUserRepository;

    @BeforeEach
    void setUp() {
        scheduledTasksService = new ScheduledTasksService(mockGoalRepository, mockUserRepository);
    }

    @Test
    void testArchiveOldGoals_ShouldArchiveCompletedGoals() {
        Goal oldGoal = new Goal();
        oldGoal.setStatus(GoalStatus.COMPLETED);
        oldGoal.setArchived(false);
        oldGoal.setCompletedDate(LocalDateTime.now().minusDays(40));

        when(mockGoalRepository.findByStatusAndArchivedFalseAndCompletedDateBefore(eq(GoalStatus.COMPLETED), any(LocalDateTime.class)))
                .thenReturn(List.of(oldGoal));

        scheduledTasksService.archiveOldGoals();

        verify(mockGoalRepository, times(1)).saveAll(any());
    }

    @Test
    void testReportRecentlyCompletedGoals_ShouldQueryForRecentGoals() {
        when(mockGoalRepository.countByCompletedDateAfter(any(LocalDateTime.class))).thenReturn(5L);

        scheduledTasksService.reportRecentlyCompletedGoals();

        verify(mockGoalRepository, times(1)).countByCompletedDateAfter(any(LocalDateTime.class));
    }

    @Test
    void testCheckForInactiveUsers_ShouldLogoutInactiveUsers() {
        when(mockUserRepository.findByLoggedInTrue()).thenReturn(Collections.emptyList());

        scheduledTasksService.checkForInactiveUsers();

        verify(mockUserRepository, times(1)).findByLoggedInTrue();
        verify(mockUserRepository, never()).save(any());
    }
}
