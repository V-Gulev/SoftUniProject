package com.fittrack.mainapp.service.impl;

import com.fittrack.mainapp.badge.service.BadgeAwardService;
import com.fittrack.mainapp.badge.service.BadgeNotificationService;
import com.fittrack.mainapp.exceptions.WorkoutLogException;
import com.fittrack.mainapp.model.dto.WorkoutLogDto;
import com.fittrack.mainapp.model.entity.User;
import com.fittrack.mainapp.model.entity.WorkoutLog;
import com.fittrack.mainapp.repository.UserRepository;
import com.fittrack.mainapp.repository.WorkoutLogRepository;
import com.fittrack.mainapp.repository.WorkoutPlanRepository;
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
class WorkoutLogServiceImplTest {

    private WorkoutLogServiceImpl workoutLogService;

    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private WorkoutLogRepository mockWorkoutLogRepository;
    @Mock
    private WorkoutPlanRepository mockWorkoutPlanRepository;
    @Mock
    private BadgeAwardService mockBadgeAwardService;
    @Mock
    private BadgeNotificationService mockBadgeNotificationService;

    private User testUser;
    private final String username = "testuser";
    private final UUID logId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        workoutLogService = new WorkoutLogServiceImpl(
                mockUserRepository,
                mockWorkoutLogRepository,
                mockWorkoutPlanRepository,
                mockBadgeAwardService,
                mockBadgeNotificationService
        );

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername(username);
    }

    @Test
    void testLogWorkout_ShouldSaveCorrectLog() {
        WorkoutLogDto logDto = new WorkoutLogDto();
        logDto.setDate(LocalDate.now());
        logDto.setHours(1);
        logDto.setMinutes(30);
        logDto.setDescription("Good session");

        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(mockWorkoutLogRepository.findByUser(testUser)).thenReturn(Collections.emptyList());

        workoutLogService.logWorkout(logDto, username);

        ArgumentCaptor<WorkoutLog> logCaptor = ArgumentCaptor.forClass(WorkoutLog.class);
        verify(mockWorkoutLogRepository).saveAndFlush(logCaptor.capture());

        WorkoutLog savedLog = logCaptor.getValue();
        assertEquals(90, savedLog.getDurationMinutes());
        assertEquals("Good session", savedLog.getDescription());
        assertEquals(testUser, savedLog.getUser());
        verify(mockBadgeAwardService).checkWorkoutBadges(any(UUID.class), any(int.class));
    }

    @Test
    void testUpdateLog_ShouldUpdateCorrectly() {
        WorkoutLog existingLog = new WorkoutLog();
        existingLog.setId(logId);
        existingLog.setUser(testUser);

        WorkoutLogDto logDto = new WorkoutLogDto();
        logDto.setId(logId);
        logDto.setHours(0);
        logDto.setMinutes(45);
        logDto.setDate(LocalDate.now().minusDays(1));
        logDto.setDescription("Updated Description");

        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(mockWorkoutLogRepository.findByIdAndUser(logId, testUser)).thenReturn(Optional.of(existingLog));

        workoutLogService.updateLog(logDto, username);

        ArgumentCaptor<WorkoutLog> logCaptor = ArgumentCaptor.forClass(WorkoutLog.class);
        verify(mockWorkoutLogRepository).save(logCaptor.capture());
        WorkoutLog updatedLog = logCaptor.getValue();

        assertEquals(45, updatedLog.getDurationMinutes());
        assertEquals("Updated Description", updatedLog.getDescription());
    }

    @Test
    void testDeleteLog_ShouldCallDelete() {
        WorkoutLog logToDelete = new WorkoutLog();
        logToDelete.setId(logId);
        logToDelete.setUser(testUser);

        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(mockWorkoutLogRepository.findByIdAndUser(logId, testUser)).thenReturn(Optional.of(logToDelete));

        workoutLogService.deleteLog(logId, username);

        verify(mockWorkoutLogRepository, times(1)).delete(logToDelete);
    }

    @Test
    void testLogWorkout_WhenDurationIsZero_ShouldThrowException() {

        WorkoutLogDto logDto = new WorkoutLogDto();
        logDto.setHours(0);
        logDto.setMinutes(0);

        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        assertThrows(WorkoutLogException.class, () -> {
            workoutLogService.logWorkout(logDto, username);
        });
    }
}
