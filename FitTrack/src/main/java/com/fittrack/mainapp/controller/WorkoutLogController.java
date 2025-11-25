package com.fittrack.mainapp.controller;

import com.fittrack.mainapp.badge.dto.BadgeDto;
import com.fittrack.mainapp.model.dto.WorkoutLogDto;
import com.fittrack.mainapp.service.WorkoutLogService;
import com.fittrack.mainapp.service.WorkoutPlanService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
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
    public String showLogWorkoutForm(Model model, Principal principal) {
        if (!model.containsAttribute("logDto")) {
            model.addAttribute("logDto", new WorkoutLogDto());
        }
        model.addAttribute("workoutPlans", workoutPlanService.getPlansForUser(principal.getName()));
        return "log-add";
    }

    @PostMapping("/add")
    public String logWorkout(@Valid @ModelAttribute("logDto") WorkoutLogDto logDto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Principal principal) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("logDto", logDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.logDto", bindingResult);
            return "redirect:/logs/add";
        }

        workoutLogService.logWorkout(logDto, principal.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Workout logged successfully!");

        BadgeDto awardedBadge = workoutLogService.getAndClearLastAwardedBadge();
        redirectAttributes.addFlashAttribute("awardedBadge", awardedBadge);

        return "redirect:/logs/history";
    }

    @GetMapping("/history")
    public String showWorkoutHistory(Model model, Principal principal) {
        model.addAttribute("logs", workoutLogService.getLogsForUser(principal.getName()));
        return "logs-history";
    }

    @GetMapping("/edit/{id}")
    public String showEditLogForm(@PathVariable("id") UUID id, Model model, Principal principal) {
        if (!model.containsAttribute("logDto")) {
            model.addAttribute("logDto", workoutLogService.getLogById(id, principal.getName()));
        }
        model.addAttribute("workoutPlans", workoutPlanService.getPlansForUser(principal.getName()));
        return "log-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateLog(@PathVariable("id") UUID id,
                            @Valid @ModelAttribute("logDto") WorkoutLogDto logDto,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            Principal principal) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("logDto", logDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.logDto", bindingResult);
            return "redirect:/logs/edit/" + id;
        }

        logDto.setId(id);
        workoutLogService.updateLog(logDto, principal.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Workout log updated successfully.");

        return "redirect:/logs/history";
    }

    @PostMapping("/delete/{id}")
    public String deleteLog(@PathVariable("id") UUID id, Principal principal, RedirectAttributes redirectAttributes) {
        workoutLogService.deleteLog(id, principal.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Workout log deleted successfully.");
        return "redirect:/logs/history";
    }
}