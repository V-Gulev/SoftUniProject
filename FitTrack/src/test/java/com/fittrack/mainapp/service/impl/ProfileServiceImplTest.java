package com.fittrack.mainapp.service.impl;

import com.fittrack.mainapp.badge.client.BadgeServiceClient;
import com.fittrack.mainapp.model.dto.ProfileEditDto;
import com.fittrack.mainapp.model.dto.ProfileViewDto;
import com.fittrack.mainapp.model.entity.User;
import com.fittrack.mainapp.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    private ProfileServiceImpl profileService;

    @Mock
    private UserService mockUserService;
    @Mock
    private GoalService mockGoalService;
    @Mock
    private WorkoutPlanService mockWorkoutPlanService;
    @Mock
    private WorkoutLogService mockWorkoutLogService;
    @Mock
    private BadgeServiceClient mockBadgeServiceClient;

    @BeforeEach
    void setUp() {
        profileService = new ProfileServiceImpl(
                mockUserService,
                mockGoalService,
                mockWorkoutPlanService,
                mockWorkoutLogService,
                mockBadgeServiceClient
        );
    }

    @Test
    void testBuildProfile_ShouldConstructCorrectDto() {
        // Arrange
        String username = "testuser";
        User testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername(username);

        when(mockUserService.findUserByUsername(username)).thenReturn(testUser);
        when(mockGoalService.getGoalsForUser(username)).thenReturn(Collections.emptyList());
        when(mockWorkoutPlanService.getPlansForUser(username)).thenReturn(Collections.emptyList());
        when(mockWorkoutLogService.getLogsForUser(username)).thenReturn(Collections.emptyList());
        when(mockBadgeServiceClient.getBadgesForUser(testUser.getId())).thenReturn(Collections.emptyList());

        // Act
        ProfileViewDto result = profileService.buildProfile(username);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUserProfile().getUsername());
        assertEquals(0, result.getTotalGoals());
        assertEquals(0, result.getTotalLogs());
        assertTrue(result.getBadges().isEmpty());
    }

    @Test
    void testUpdateProfile_ShouldReturnTrueIfCredentialsChanged() {
        // Arrange
        String oldUsername = "oldUser";
        ProfileEditDto profileDto = new ProfileEditDto();
        profileDto.setUsername("newUser");
        profileDto.setNewPassword("newPassword");

        // Act
        boolean result = profileService.updateProfile(profileDto, oldUsername);

        // Assert
        assertTrue(result);
        verify(mockUserService).updateProfile(profileDto, oldUsername);
    }

    @Test
    void testUpdateProfile_ShouldReturnFalseIfCredentialsNotChanged() {
        // Arrange
        String username = "sameUser";
        ProfileEditDto profileDto = new ProfileEditDto();
        profileDto.setUsername(username);
        profileDto.setNewPassword(null); // No password change

        // Act
        boolean result = profileService.updateProfile(profileDto, username);

        // Assert
        assertFalse(result);
        verify(mockUserService).updateProfile(profileDto, username);
    }
}
