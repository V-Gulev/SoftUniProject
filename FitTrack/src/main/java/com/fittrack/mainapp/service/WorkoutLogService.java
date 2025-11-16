package com.fittrack.mainapp.service;

import com.fittrack.mainapp.badge.dto.BadgeDto;
import com.fittrack.mainapp.model.dto.WorkoutLogDto;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public interface WorkoutLogService {
    void logWorkout(WorkoutLogDto logDto, String username);

    List<WorkoutLogDto> getLogsForUser(String username);

    WorkoutLogDto getLogById(UUID id, String username);

    void updateLog(@Valid WorkoutLogDto logDto, String username);

    void deleteLog(UUID id, String username);

    BadgeDto getAndClearLastAwardedBadge();
}
