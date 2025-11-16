package com.fittrack.mainapp.badge.service;

import com.fittrack.mainapp.badge.dto.BadgeDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BadgeNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BadgeNotificationService.class);
    private static final ThreadLocal<BadgeDto> lastAwardedBadge = new ThreadLocal<>();

    public void setLastAwardedBadge(BadgeDto badge) {
        LOGGER.info("Setting badge notification for display: {} (Badge ID: {})", badge.getName(), badge.getId());
        lastAwardedBadge.set(badge);
    }

    public BadgeDto getAndClearLastAwardedBadge() {
        BadgeDto badge = lastAwardedBadge.get();
        lastAwardedBadge.remove();
        if (badge != null) {
            LOGGER.info("Retrieved badge notification for display: {}", badge.getName());
        } else {
            LOGGER.debug("No badge notification to display");
        }
        return badge;
    }
}