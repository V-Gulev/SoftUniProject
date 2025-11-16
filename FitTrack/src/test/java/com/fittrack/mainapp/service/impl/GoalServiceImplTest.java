package com.fittrack.mainapp.service.impl;

import com.fittrack.mainapp.badge.service.BadgeAwardService;
import com.fittrack.mainapp.badge.service.BadgeNotificationService;
import com.fittrack.mainapp.exceptions.GoalException;
import com.fittrack.mainapp.exceptions.ResourceNotFoundException;
import com.fittrack.mainapp.exceptions.UnauthorizedOperationException;
import com.fittrack.mainapp.model.dto.GoalDto;
import com.fittrack.mainapp.model.entity.Goal;
import com.fittrack.mainapp.model.entity.User;
import com.fittrack.mainapp.model.enums.GoalStatus;
import com.fittrack.mainapp.model.enums.GoalUnit;
import com.fittrack.mainapp.repository.GoalRepository;
import com.fittrack.mainapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceImplTest {

    private GoalServiceImpl goalService;

    @Mock
    private GoalRepository mockGoalRepository;
    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private BadgeAwardService mockBadgeAwardService;
    @Mock
    private BadgeNotificationService mockBadgeNotificationService;

    private User testUser;
    private Goal testGoal;
    private final String username = "testuser";
    private final UUID goalId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        goalService = new GoalServiceImpl(
                mockGoalRepository,
                mockUserRepository,
                mockBadgeAwardService,
                mockBadgeNotificationService
        );

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername(username);

        testGoal = new Goal();
        testGoal.setId(goalId);
        testGoal.setUser(testUser);
        testGoal.setName("Initial Goal");
        testGoal.setTargetValue(10.0);
        testGoal.setCurrentValue(5.0);
        testGoal.setStatus(GoalStatus.ACTIVE);
    }

    @Test
    void testCreateGoal_ShouldSaveCorrectGoal() {
        GoalDto goalDto = new GoalDto();
        goalDto.setName("Run 5k");
        goalDto.setDescription("Run a full 5k without stopping.");
        goalDto.setTargetValue(5.0);
        goalDto.setCurrentValue(0.0);
        goalDto.setUnit(GoalUnit.KILOMETERS);
        goalDto.setTargetDate(LocalDate.now().plusMonths(1));

        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(mockGoalRepository.findByUser(testUser)).thenReturn(Collections.emptyList());

        goalService.createGoal(goalDto, username);

        ArgumentCaptor<Goal> goalCaptor = ArgumentCaptor.forClass(Goal.class);
        verify(mockGoalRepository).save(goalCaptor.capture());

        Goal savedGoal = goalCaptor.getValue();
        assertEquals("Run 5k", savedGoal.getName());
        assertEquals(testUser, savedGoal.getUser());
        verify(mockBadgeAwardService).checkGoalBadges(any(UUID.class), any(int.class), any(int.class));
    }

    @Test
    void testCreateGoal_WhenRepositoryFails_ShouldThrowGoalException() {
        GoalDto goalDto = new GoalDto();
        goalDto.setName("Test Goal");

        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(mockGoalRepository.save(any(Goal.class))).thenThrow(new RuntimeException("Database error"));

        GoalException exception = assertThrows(GoalException.class, () -> goalService.createGoal(goalDto, username));

        assertEquals("Error creating goal: Database error", exception.getMessage());
        assertEquals(goalDto, exception.getGoalDto());
    }

    @Test
    void testUpdateGoal_ShouldUpdateFields() {
        GoalDto goalDto = new GoalDto();
        goalDto.setName("Updated Goal Name");
        goalDto.setCurrentValue(10.0);
        goalDto.setTargetValue(10.0);
        goalDto.setTargetDate(LocalDate.now().plusDays(10));

        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(mockGoalRepository.findByIdAndUser(goalId, testUser)).thenReturn(Optional.of(testGoal));

        goalService.updateGoal(goalId, goalDto, username);

        ArgumentCaptor<Goal> goalCaptor = ArgumentCaptor.forClass(Goal.class);
        verify(mockGoalRepository).save(goalCaptor.capture());
        Goal updatedGoal = goalCaptor.getValue();

        assertEquals("Updated Goal Name", updatedGoal.getName());
        assertTrue(updatedGoal.getStatus().equals(GoalStatus.COMPLETED));
    }

    @Test
    void testDeleteGoal_ShouldCallDeleteOnRepository() {
        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(mockGoalRepository.findByIdAndUser(goalId, testUser)).thenReturn(Optional.of(testGoal));

        goalService.deleteGoal(goalId, username);

        verify(mockGoalRepository, times(1)).delete(testGoal);
    }

    @Test
    void testCompleteGoal_ShouldSetCompletedAndSave() {
        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(mockGoalRepository.findByIdAndUser(goalId, testUser)).thenReturn(Optional.of(testGoal));

        goalService.completeGoal(goalId, username);

        ArgumentCaptor<Goal> goalCaptor = ArgumentCaptor.forClass(Goal.class);
        verify(mockGoalRepository).saveAndFlush(goalCaptor.capture());
        Goal completedGoal = goalCaptor.getValue();

        assertTrue(completedGoal.getStatus().equals(GoalStatus.COMPLETED));
        assertNotNull(completedGoal.getCompletedDate());
        assertEquals(completedGoal.getTargetValue(), completedGoal.getCurrentValue());
    }

    @Test
    void testGetGoalById_WhenGoalNotFound_ShouldThrowUnauthorizedOperationException() {
        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(mockGoalRepository.findByIdAndUser(goalId, testUser)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedOperationException.class, () -> {
            goalService.getGoalById(goalId, username);
        });
    }
}
