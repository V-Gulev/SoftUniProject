package com.fittrack.mainapp.badge.service;

import com.fittrack.mainapp.badge.client.BadgeServiceClient;
import com.fittrack.mainapp.badge.dto.BadgeAwardDto;
import com.fittrack.mainapp.badge.dto.BadgeDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BadgeAwardServiceTest {

    private BadgeAwardService badgeAwardService;

    @Mock
    private BadgeServiceClient mockBadgeServiceClient;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        // The setUp method should only contain setup that is common to ALL tests.
        badgeAwardService = new BadgeAwardService(mockBadgeServiceClient);
    }

    @Test
    void testAwardBadgeIfNotExists_WhenUserDoesNotHaveBadge() {
        // Arrange: Mocks are now specific to this test
        when(mockBadgeServiceClient.getBadgesForUser(userId)).thenReturn(Collections.emptyList());
        when(mockBadgeServiceClient.awardBadge(any(BadgeAwardDto.class))).thenReturn(new BadgeDto());

        String badgeName = "Test Badge";
        String iconUrl = "/img/test.png";

        // Act
        BadgeDto result = badgeAwardService.awardBadgeIfNotExists(userId, badgeName, iconUrl);

        // Assert
        assertNotNull(result);
        ArgumentCaptor<BadgeAwardDto> captor = ArgumentCaptor.forClass(BadgeAwardDto.class);
        verify(mockBadgeServiceClient).awardBadge(captor.capture());
        assertEquals(badgeName, captor.getValue().getName());
    }

    @Test
    void testAwardBadgeIfNotExists_WhenUserAlreadyHasBadge() {
        // Arrange: Mock is specific to this test
        String badgeName = "Existing Badge";
        BadgeDto existingBadge = new BadgeDto(UUID.randomUUID(), badgeName, "", userId);
        when(mockBadgeServiceClient.getBadgesForUser(userId)).thenReturn(List.of(existingBadge));

        // Act
        BadgeDto result = badgeAwardService.awardBadgeIfNotExists(userId, badgeName, "/img/test.png");

        // Assert
        assertNull(result);
        verify(mockBadgeServiceClient, never()).awardBadge(any());
    }

    private void setupBadgeMocks() {
        when(mockBadgeServiceClient.getBadgesForUser(any())).thenReturn(Collections.emptyList());
        when(mockBadgeServiceClient.awardBadge(any(BadgeAwardDto.class))).thenReturn(new BadgeDto());
    }

    @ParameterizedTest
    @CsvSource({
            "1, First Goal Completed",
            "5, Goal Master",
            "10, Goal Champion",
            "25, Goal Legend",
            "50, Goal Hero",
            "100, Goal God"
    })
    void testCheckGoalBadges_AwardsCorrectBadgeForCompletedGoals(int completedGoals, String expectedBadgeName) {
        // Arrange
        setupBadgeMocks();

        // Act
        badgeAwardService.checkGoalBadges(userId, 10, completedGoals);

        // Assert
        ArgumentCaptor<BadgeAwardDto> captor = ArgumentCaptor.forClass(BadgeAwardDto.class);
        verify(mockBadgeServiceClient).awardBadge(captor.capture());
        assertEquals(expectedBadgeName, captor.getValue().getName());
    }

    @Test
    void testCheckGoalBadges_AwardsGoalSetterBadge() {
        // Arrange
        setupBadgeMocks();

        // Act
        badgeAwardService.checkGoalBadges(userId, 1, 0);

        // Assert
        ArgumentCaptor<BadgeAwardDto> captor = ArgumentCaptor.forClass(BadgeAwardDto.class);
        verify(mockBadgeServiceClient).awardBadge(captor.capture());
        assertEquals("Goal Setter", captor.getValue().getName());
    }

    @ParameterizedTest
    @CsvSource({
            "1, First Workout",
            "10, Workout Beginner",
            "50, Workout Warrior",
            "100, Workout Champion",
            "500, Workout Legend"
    })
    void testCheckWorkoutBadges_AwardsCorrectBadgeForTotalWorkouts(int totalWorkouts, String expectedBadgeName) {
        // Arrange
        setupBadgeMocks();

        // Act
        badgeAwardService.checkWorkoutBadges(userId, totalWorkouts);

        // Assert
        ArgumentCaptor<BadgeAwardDto> captor = ArgumentCaptor.forClass(BadgeAwardDto.class);
        verify(mockBadgeServiceClient).awardBadge(captor.capture());
        assertEquals(expectedBadgeName, captor.getValue().getName());
    }

    @Test
    void testCheckPlanBadges_AwardsPlanCreatorBadge() {
        // Arrange
        setupBadgeMocks();

        // Act
        badgeAwardService.checkPlanBadges(userId, 1);

        // Assert
        ArgumentCaptor<BadgeAwardDto> captor = ArgumentCaptor.forClass(BadgeAwardDto.class);
        verify(mockBadgeServiceClient).awardBadge(captor.capture());
        assertEquals("Plan Creator", captor.getValue().getName());
    }

    @Test
    void testRevokeBadge_ShouldCallDeleteOnClient() {
        // Arrange - No mocks needed for get/award
        UUID badgeId = UUID.randomUUID();
        doNothing().when(mockBadgeServiceClient).deleteBadge(badgeId);

        // Act
        badgeAwardService.revokeBadge(badgeId);

        // Assert
        verify(mockBadgeServiceClient, times(1)).deleteBadge(badgeId);
    }
}
