package com.fittrack.mainapp.controller;

import com.fittrack.mainapp.badge.dto.BadgeDto;
import com.fittrack.mainapp.model.dto.WorkoutLogDto;
import com.fittrack.mainapp.service.WorkoutLogService;
import com.fittrack.mainapp.service.WorkoutPlanService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/logs")
public class WorkoutLogController {

    private final WorkoutLogService workoutLogService;
    private final WorkoutPlanService workoutPlanService;

    public WorkoutLogController(WorkoutLogService workoutLogService, WorkoutPlanService workoutPlanService) {
        this.workoutLogService = workoutLogService;
        this.workoutPlanService = workoutPlanService;
    }

    @GetMapping("/add")
    public String showLogWorkoutForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (!model.containsAttribute("logDto")) {
            model.addAttribute("logDto", new WorkoutLogDto());
        }
        model.addAttribute("workoutPlans", workoutPlanService.getPlansForUser(userDetails.getUsername()));
        return "log-add";
    }

    @PostMapping("/add")
    public String logWorkout(@Valid @ModelAttribute("logDto") WorkoutLogDto logDto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             @AuthenticationPrincipal UserDetails userDetails) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("logDto", logDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.logDto", bindingResult);
            return "redirect:/logs/add";
        }

        workoutLogService.logWorkout(logDto, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("successMessage", "Workout logged successfully!");

        BadgeDto awardedBadge = workoutLogService.getAndClearLastAwardedBadge();
        redirectAttributes.addFlashAttribute("awardedBadge", awardedBadge);

        return "redirect:/logs/history";
    }

    @GetMapping("/history")
    public String showWorkoutHistory(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("logs", workoutLogService.getLogsForUser(userDetails.getUsername()));
        return "logs-history";
    }

    @GetMapping("/edit/{id}")
    public String showEditLogForm(@PathVariable("id") UUID id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (!model.containsAttribute("logDto")) {
            model.addAttribute("logDto", workoutLogService.getLogById(id, userDetails.getUsername()));
        }
        model.addAttribute("workoutPlans", workoutPlanService.getPlansForUser(userDetails.getUsername()));
        return "log-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateLog(@PathVariable("id") UUID id,
                            @Valid @ModelAttribute("logDto") WorkoutLogDto logDto,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            @AuthenticationPrincipal UserDetails userDetails) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("logDto", logDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.logDto", bindingResult);
            return "redirect:/logs/edit/" + id;
        }

        logDto.setId(id);
        workoutLogService.updateLog(logDto, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("successMessage", "Workout log updated successfully.");

        return "redirect:/logs/history";
    }

    @PostMapping("/delete/{id}")
    public String deleteLog(@PathVariable("id") UUID id, @AuthenticationPrincipal UserDetails userDetails,RedirectAttributes redirectAttributes) {
        workoutLogService.deleteLog(id, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("successMessage", "Workout log deleted successfully.");
        return "redirect:/logs/history";
    }
}