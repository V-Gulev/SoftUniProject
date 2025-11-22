package com.fittrack.mainapp.service.impl;

import com.fittrack.mainapp.exceptions.WorkoutPlanException;
import com.fittrack.mainapp.model.dto.WorkoutPlanDto;
import com.fittrack.mainapp.model.entity.User;
import com.fittrack.mainapp.model.entity.WorkoutPlan;
import com.fittrack.mainapp.repository.UserRepository;
import com.fittrack.mainapp.repository.WorkoutPlanRepository;
import com.fittrack.mainapp.badge.service.BadgeAwardService;
import com.fittrack.mainapp.badge.service.BadgeNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutPlanServiceImplTest {

    private WorkoutPlanServiceImpl workoutPlanService;

    @Mock
    private WorkoutPlanRepository mockWorkoutPlanRepository;
    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private BadgeAwardService mockBadgeAwardService;
    @Mock
    private BadgeNotificationService mockBadgeNotificationService;

    private User testUser;
    private final String username = "testuser";
    private final UUID planId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        workoutPlanService = new WorkoutPlanServiceImpl(mockWorkoutPlanRepository, mockUserRepository,
                mockBadgeAwardService, mockBadgeNotificationService);
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername(username);
    }

    @Test
    void testCreatePlan_ShouldSaveCorrectPlan() {
        WorkoutPlanDto planDto = new WorkoutPlanDto(null, "Morning Routine", "A simple morning workout."
                , null, null
                , null, null
                , null, null
                , null);

        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(mockWorkoutPlanRepository.findByUser(testUser)).thenReturn(Collections.singletonList(new WorkoutPlan()));

        workoutPlanService.createPlan(planDto, username);

        ArgumentCaptor<WorkoutPlan> planCaptor = ArgumentCaptor.forClass(WorkoutPlan.class);
        verify(mockWorkoutPlanRepository).save(planCaptor.capture());
        WorkoutPlan savedPlan = planCaptor.getValue();

        assertEquals("Morning Routine", savedPlan.getName());
        assertEquals(username, savedPlan.getUser().getUsername());
        verify(mockBadgeAwardService, times(1)).checkPlanBadges(any(), eq(1));
    }

    @Test
    void testUpdatePlan_Success() {
        WorkoutPlan existingPlan = new WorkoutPlan();
        existingPlan.setId(planId);
        existingPlan.setUser(testUser);

        WorkoutPlanDto planDto = new WorkoutPlanDto(planId, "Evening Routine", "An evening workout."
                , null, null
                , null, null
                , null, null
                , null);

        when(mockWorkoutPlanRepository.findById(planId)).thenReturn(Optional.of(existingPlan));

        workoutPlanService.updatePlan(planId, planDto, username);

        ArgumentCaptor<WorkoutPlan> planCaptor = ArgumentCaptor.forClass(WorkoutPlan.class);
        verify(mockWorkoutPlanRepository).save(planCaptor.capture());
        WorkoutPlan updatedPlan = planCaptor.getValue();

        assertEquals("Evening Routine", updatedPlan.getName());
    }

    @Test
    void testUpdatePlan_ThrowsException_WhenUserNotAuthorized() {
        User otherUser = new User();
        otherUser.setUsername("otherUser");

        WorkoutPlan existingPlan = new WorkoutPlan();
        existingPlan.setId(planId);
        existingPlan.setUser(otherUser);

        WorkoutPlanDto planDto = new WorkoutPlanDto();

        when(mockWorkoutPlanRepository.findById(planId)).thenReturn(Optional.of(existingPlan));

        assertThrows(WorkoutPlanException.class, () -> {
            workoutPlanService.updatePlan(planId, planDto, username);
        });
    }

    @Test
    void testDeletePlan_Success() {
        WorkoutPlan planToDelete = new WorkoutPlan();
        planToDelete.setId(planId);
        planToDelete.setUser(testUser);

        when(mockUserRepository.getUserByUsername(username)).thenReturn(testUser);
        when(mockWorkoutPlanRepository.findByIdAndUser(planId, testUser)).thenReturn(Optional.of(planToDelete));

        workoutPlanService.deletePlan(planId, username);

        verify(mockWorkoutPlanRepository, times(1)).delete(planToDelete);
    }

    @Test
    void testSetActivePlan_Success() {
        WorkoutPlan planToActivate = new WorkoutPlan();
        planToActivate.setId(planId);
        planToActivate.setUser(testUser);

        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(mockWorkoutPlanRepository.findById(planId)).thenReturn(Optional.of(planToActivate));

        workoutPlanService.setActivePlan(planId, username);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(mockUserRepository).save(userCaptor.capture());
        User updatedUser = userCaptor.getValue();

        assertEquals(planId, updatedUser.getActiveWorkoutPlan().getId());
    }

    @Test
    void testGetPlansForUser_ShouldReturnPlans() {
        WorkoutPlan plan = new WorkoutPlan();
        plan.setId(UUID.randomUUID());
        plan.setName("Test Plan");

        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(mockWorkoutPlanRepository.findByUser(testUser)).thenReturn(Collections.singletonList(plan));

        List<WorkoutPlanDto> result = workoutPlanService.getPlansForUser(username);

        assertEquals(1, result.size());
        assertEquals("Test Plan", result.get(0).getName());
    }

    @Test
    void testGetInactivePlansForUser_ShouldFilterActivePlan() {
        WorkoutPlan activePlan = new WorkoutPlan();
        activePlan.setId(planId);
        activePlan.setName("Active Plan");

        WorkoutPlan inactivePlan = new WorkoutPlan();
        inactivePlan.setId(UUID.randomUUID());
        inactivePlan.setName("Inactive Plan");

        testUser.setActiveWorkoutPlan(activePlan);

        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(mockWorkoutPlanRepository.findByUser(testUser)).thenReturn(Arrays.asList(activePlan, inactivePlan));

        List<WorkoutPlanDto> result = workoutPlanService.getInactivePlansForUser(username);

        assertEquals(1, result.size());
        assertEquals("Inactive Plan", result.get(0).getName());
    }

    @Test
    void testGetActivePlan_WhenExists_ShouldReturnPlan() {
        WorkoutPlan activePlan = new WorkoutPlan();
        activePlan.setId(planId);
        activePlan.setName("Active Plan");

        testUser.setActiveWorkoutPlan(activePlan);

        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        WorkoutPlanDto result = workoutPlanService.getActivePlan(username);

        assertNotNull(result);
        assertEquals("Active Plan", result.getName());
    }
}
