package com.fittrack.mainapp.service.impl;

import com.fittrack.mainapp.badge.client.BadgeServiceClient;
import com.fittrack.mainapp.badge.dto.BadgeDto;
import com.fittrack.mainapp.model.dto.*;
import com.fittrack.mainapp.model.entity.User;
import com.fittrack.mainapp.model.enums.GoalStatus;
import com.fittrack.mainapp.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final UserService userService;
    private final GoalService goalService;
    private final WorkoutPlanService workoutPlanService;
    private final WorkoutLogService workoutLogService;
    private final BadgeServiceClient badgeServiceClient;

    public ProfileServiceImpl(UserService userService,
                              GoalService goalService,
                              WorkoutPlanService workoutPlanService,
                              WorkoutLogService workoutLogService,
                              BadgeServiceClient badgeServiceClient) {
        this.userService = userService;
        this.goalService = goalService;
        this.workoutPlanService = workoutPlanService;
        this.workoutLogService = workoutLogService;
        this.badgeServiceClient = badgeServiceClient;
    }

    @Override
    public ProfileViewDto buildProfile(String username) {
        User user = userService.findUserByUsername(username);
        List<GoalDto> goals = goalService.getGoalsForUser(username);
        List<WorkoutPlanDto> plans = workoutPlanService.getPlansForUser(username);
        List<WorkoutLogDto> logs = workoutLogService.getLogsForUser(username);

        long totalGoals = goals.size();
        long activeGoals = goals.stream().filter(g -> g.getStatus() == GoalStatus.ACTIVE).count();
        long completedGoals = goals.stream().filter(g -> g.getStatus() == GoalStatus.COMPLETED).count();

        List<BadgeDto> badges;
        try {
            badges = badgeServiceClient.getBadgesForUser(user.getId());
        } catch (Exception e) {
            badges = List.of();
        }

        ProfileViewDto dto = new ProfileViewDto();
        dto.setUserProfile(user);
        dto.setTotalGoals(totalGoals);
        dto.setActiveGoals(activeGoals);
        dto.setCompletedGoals(completedGoals);
        dto.setTotalPlans(plans.size());
        dto.setTotalLogs(logs.size());
        dto.setActivePlan(workoutPlanService.getActivePlan(username));
        dto.setBadges(badges);
        return dto;
    }

    @Override
    public ProfileEditDto buildProfileEditDto(String username) {
        User user = userService.findUserByUsername(username);
        ProfileEditDto profileDto = new ProfileEditDto();
        profileDto.setUsername(user.getUsername());
        profileDto.setEmail(user.getEmail());
        return profileDto;
    }

    @Override
    public boolean updateProfile(ProfileEditDto profileDto, String oldUsername) {
        boolean usernameChanged = profileDto.getUsername() != null && !profileDto.getUsername().equals(oldUsername);
        boolean passwordChanged = profileDto.getNewPassword() != null && !profileDto.getNewPassword().isEmpty();

        userService.updateProfile(profileDto, oldUsername);

        return usernameChanged || passwordChanged;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(request, response, SecurityContextHolder.getContext().getAuthentication());
    }
}