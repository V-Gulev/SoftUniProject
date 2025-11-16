package com.fittrack.mainapp.badge.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BadgeAwardDto {

    @NotEmpty(message = "Badge name cannot be empty.")
    private String name;

    @NotEmpty(message = "Icon URL cannot be empty.")
    private String iconUrl;

    @NotNull(message = "User ID cannot be null.")
    private UUID userId;
}
