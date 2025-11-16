package com.fittrack.mainapp.badge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BadgeDto {

    private UUID id;

    private String name;

    private String iconUrl;

    private UUID userId;
}
