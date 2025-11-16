package com.fittrack.badgeservice.service.impl;

import com.fittrack.badgeservice.exceptions.ResourceNotFoundException;
import com.fittrack.badgeservice.model.dto.BadgeAwardDto;
import com.fittrack.badgeservice.model.dto.BadgeDto;
import com.fittrack.badgeservice.model.entity.Badge;
import com.fittrack.badgeservice.repository.BadgeRepository;
import com.fittrack.badgeservice.service.BadgeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BadgeServiceImpl implements BadgeService {

    private final BadgeRepository badgeRepository;

    public BadgeServiceImpl(BadgeRepository badgeRepository) {
        this.badgeRepository = badgeRepository;
    }

    @Override
    public BadgeDto awardBadge(BadgeAwardDto badgeAwardDto) {
        Badge newBadge = new Badge();
        newBadge.setName(badgeAwardDto.getName());
        newBadge.setIconUrl(badgeAwardDto.getIconUrl());
        newBadge.setUserId(badgeAwardDto.getUserId());

        Badge savedBadge = badgeRepository.save(newBadge);
        return mapToDto(savedBadge);
    }

    @Override
    public List<BadgeDto> getBadgesForUser(UUID userId) {
        return badgeRepository.findAllByUserId(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteBadge(UUID badgeId) {
        if (!badgeRepository.existsById(badgeId)) {
            throw new ResourceNotFoundException("Badge not found with ID: " + badgeId);
        }
        badgeRepository.deleteById(badgeId);
    }

    private BadgeDto mapToDto(Badge badge) {
        return new BadgeDto(badge.getId(), badge.getName(), badge.getIconUrl(), badge.getUserId());
    }
}