package com.fittrack.mainapp.controller;

import com.fittrack.mainapp.model.dto.DashboardSummaryDto;
import com.fittrack.mainapp.service.DashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public String showDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        DashboardSummaryDto summary = dashboardService.buildDashboard(userDetails.getUsername());

        model.addAttribute("goals", summary.getGoals());
        model.addAttribute("completedGoalsCount", summary.getCompletedGoalsCount());
        model.addAttribute("activeGoalsCount", summary.getActiveGoalsCount());
        model.addAttribute("plans", summary.getPlans());
        model.addAttribute("logs", summary.getLogs());
        model.addAttribute("recentLogs", summary.getRecentLogs());
        model.addAttribute("recentGoals", summary.getRecentGoals());

        return "dashboard";
    }
}
