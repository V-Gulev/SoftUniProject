package com.fittrack.mainapp.badge.client;

import com.fittrack.mainapp.badge.dto.BadgeAwardDto;
import com.fittrack.mainapp.badge.dto.BadgeDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "badge-service", url = "${badge-service.url}")
public interface BadgeServiceClient {

    @PostMapping("/badges")
    BadgeDto awardBadge(@RequestBody BadgeAwardDto badgeAwardDto);

    @GetMapping("/badges/user/{userId}")
    List<BadgeDto> getBadgesForUser(@PathVariable("userId") UUID userId);

    @DeleteMapping("/badges/{badgeId}")
    void deleteBadge(@PathVariable("badgeId") UUID badgeId);

}