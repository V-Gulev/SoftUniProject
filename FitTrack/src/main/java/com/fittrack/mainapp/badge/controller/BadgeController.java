package com.fittrack.mainapp.badge.controller;

import com.fittrack.mainapp.badge.service.BadgeAwardService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
@RequestMapping("/badges")
public class BadgeController {

    private final BadgeAwardService badgeAwardService;

    public BadgeController(BadgeAwardService badgeAwardService) {
        this.badgeAwardService = badgeAwardService;
    }

    @PostMapping("/revoke/{badgeId}")
    public String revokeBadge(@PathVariable("badgeId") UUID badgeId) {
        badgeAwardService.revokeBadge(badgeId);
        return "redirect:/profile";
    }
}
