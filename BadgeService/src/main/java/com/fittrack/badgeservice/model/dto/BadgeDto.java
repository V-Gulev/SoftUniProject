package com.fittrack.badgeservice.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@Setter
@Getter
public class BadgeDto {

    private UUID id;
    private String name;
    private String iconUrl;
    private UUID userId;

    public BadgeDto(UUID id, String name, String iconUrl, UUID userId) {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
        this.userId = userId;
    }
}