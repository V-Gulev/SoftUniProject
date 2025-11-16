package com.fittrack.badgeservice.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@Setter
@Getter
public class BadgeAwardDto {

    @NotEmpty(message = "Badge name cannot be empty.")
    private String name;

    @NotEmpty(message = "Icon URL cannot be empty.")
    private String iconUrl;

    @NotNull(message = "User ID cannot be null.")
    private UUID userId;

    public BadgeAwardDto(String name, String iconUrl, UUID userId) {
        this.name = name;
        this.iconUrl = iconUrl;
        this.userId = userId;
    }
}