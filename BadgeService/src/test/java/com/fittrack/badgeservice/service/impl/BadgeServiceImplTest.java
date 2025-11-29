package com.fittrack.badgeservice.service.impl;

import com.fittrack.badgeservice.exceptions.ResourceNotFoundException;
import com.fittrack.badgeservice.model.dto.BadgeAwardDto;
import com.fittrack.badgeservice.model.dto.BadgeDto;
import com.fittrack.badgeservice.model.entity.Badge;
import com.fittrack.badgeservice.repository.BadgeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BadgeServiceImplTest {

    private BadgeServiceImpl badgeService;

    @Mock
    private BadgeRepository mockBadgeRepository;

    private UUID userId;
    private UUID badgeId;

    @BeforeEach
    void setUp() {
        badgeService = new BadgeServiceImpl(mockBadgeRepository);
        userId = UUID.randomUUID();
        badgeId = UUID.randomUUID();
    }

    @Test
    void testAwardBadge_ShouldCreateAndReturnBadge() {
        BadgeAwardDto awardDto = new BadgeAwardDto("First Workout", "/images/icon.png", userId);
        Badge savedBadge = new Badge();
        savedBadge.setId(badgeId);
        savedBadge.setName("First Workout");
        savedBadge.setIconUrl("/images/icon.png");
        savedBadge.setUserId(userId);

        when(mockBadgeRepository.save(any(Badge.class))).thenReturn(savedBadge);

        BadgeDto result = badgeService.awardBadge(awardDto);

        assertNotNull(result);
        assertEquals(badgeId, result.getId());
        assertEquals("First Workout", result.getName());
        assertEquals("/images/icon.png", result.getIconUrl());
        assertEquals(userId, result.getUserId());

        ArgumentCaptor<Badge> badgeCaptor = ArgumentCaptor.forClass(Badge.class);
        verify(mockBadgeRepository).save(badgeCaptor.capture());
        Badge capturedBadge = badgeCaptor.getValue();
        assertEquals("First Workout", capturedBadge.getName());
        assertEquals("/images/icon.png", capturedBadge.getIconUrl());
        assertEquals(userId, capturedBadge.getUserId());
    }

    @Test
    void testAwardBadge_WithMaliciousName_ShouldSanitize() {
        BadgeAwardDto awardDto = new BadgeAwardDto("<script>alert(1)</script>", "/images/icon.png", userId);
        Badge savedBadge = new Badge();
        savedBadge.setId(badgeId);
        savedBadge.setName("&lt;script&gt;alert(1)&lt;/script&gt;");
        savedBadge.setIconUrl("/images/icon.png");
        savedBadge.setUserId(userId);

        when(mockBadgeRepository.save(any(Badge.class))).thenReturn(savedBadge);

        BadgeDto result = badgeService.awardBadge(awardDto);

        assertEquals("&lt;script&gt;alert(1)&lt;/script&gt;", result.getName());

        ArgumentCaptor<Badge> badgeCaptor = ArgumentCaptor.forClass(Badge.class);
        verify(mockBadgeRepository).save(badgeCaptor.capture());
        assertEquals("&lt;script&gt;alert(1)&lt;/script&gt;", badgeCaptor.getValue().getName());
    }

    @Test
    void testAwardBadge_WithInvalidUrl_ShouldThrowException() {
        BadgeAwardDto awardDto = new BadgeAwardDto("Badge", "javascript:alert(1)", userId);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            badgeService.awardBadge(awardDto);
        });

        assertEquals("Invalid icon path: javascript:alert(1)", exception.getMessage());
        verify(mockBadgeRepository, never()).save(any());
    }

    @Test
    void testGetBadgesForUser_WhenUserHasNoBadges_ShouldReturnEmptyList() {
        when(mockBadgeRepository.findAllByUserId(userId)).thenReturn(Collections.emptyList());

        List<BadgeDto> result = badgeService.getBadgesForUser(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mockBadgeRepository).findAllByUserId(userId);
    }

    @Test
    void testGetBadgesForUser_WhenUserHasBadges_ShouldReturnBadgeList() {
        Badge badge1 = new Badge();
        badge1.setId(UUID.randomUUID());
        badge1.setName("Badge 1");
        badge1.setIconUrl("url1");
        badge1.setUserId(userId);

        Badge badge2 = new Badge();
        badge2.setId(UUID.randomUUID());
        badge2.setName("Badge 2");
        badge2.setIconUrl("url2");
        badge2.setUserId(userId);

        when(mockBadgeRepository.findAllByUserId(userId)).thenReturn(Arrays.asList(badge1, badge2));

        List<BadgeDto> result = badgeService.getBadgesForUser(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Badge 1", result.get(0).getName());
        assertEquals("Badge 2", result.get(1).getName());
        verify(mockBadgeRepository).findAllByUserId(userId);
    }

    @Test
    void testDeleteBadge_WhenBadgeExists_ShouldDeleteSuccessfully() {
        when(mockBadgeRepository.existsById(badgeId)).thenReturn(true);

        badgeService.deleteBadge(badgeId);

        verify(mockBadgeRepository).existsById(badgeId);
        verify(mockBadgeRepository).deleteById(badgeId);
    }

    @Test
    void testDeleteBadge_WhenBadgeDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(mockBadgeRepository.existsById(badgeId)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> badgeService.deleteBadge(badgeId));

        assertTrue(exception.getMessage().contains(badgeId.toString()));
        verify(mockBadgeRepository).existsById(badgeId);
        verify(mockBadgeRepository, never()).deleteById(any());
    }
}
