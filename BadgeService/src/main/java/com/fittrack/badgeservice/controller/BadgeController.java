package com.fittrack.badgeservice.controller;

import com.fittrack.badgeservice.model.dto.BadgeAwardDto;
import com.fittrack.badgeservice.model.dto.BadgeDto;
import com.fittrack.badgeservice.service.BadgeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/badges")
public class BadgeController {

    private final BadgeService badgeService;

    public BadgeController(BadgeService badgeService) {
        this.badgeService = badgeService;
    }

    @PostMapping
    public ResponseEntity<BadgeDto> awardBadge(@Valid @RequestBody BadgeAwardDto badgeAwardDto) {
        BadgeDto createdBadge = badgeService.awardBadge(badgeAwardDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBadge);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BadgeDto>> getBadgesForUser(@PathVariable UUID userId) {
        List<BadgeDto> badges = badgeService.getBadgesForUser(userId);
        return ResponseEntity.ok(badges);
    }

    @DeleteMapping("/{badgeId}")
    public ResponseEntity<Void> deleteBadge(@PathVariable UUID badgeId) {
        badgeService.deleteBadge(badgeId);
        return ResponseEntity.noContent().build();
    }
}