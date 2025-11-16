package com.fittrack.badgeservice.service;

import com.fittrack.badgeservice.model.dto.BadgeAwardDto;
import com.fittrack.badgeservice.model.dto.BadgeDto;

import java.util.List;
import java.util.UUID;

public interface BadgeService {
    BadgeDto awardBadge(BadgeAwardDto badgeAwardDto);

    List<BadgeDto> getBadgesForUser(UUID userId);

    void deleteBadge(UUID badgeId);
}