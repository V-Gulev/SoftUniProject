package com.fittrack.mainapp.controller;

import com.fittrack.mainapp.badge.dto.BadgeDto;
import com.fittrack.mainapp.model.dto.WorkoutPlanDto;
import com.fittrack.mainapp.badge.service.BadgeNotificationService;
import com.fittrack.mainapp.service.WorkoutPlanService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/workout-plans")
public class WorkoutPlanController {

    private final WorkoutPlanService workoutPlanService;
    private final BadgeNotificationService badgeNotificationService;

    public WorkoutPlanController(WorkoutPlanService workoutPlanService, BadgeNotificationService badgeNotificationService) {
        this.workoutPlanService = workoutPlanService;
        this.badgeNotificationService = badgeNotificationService;
    }

    @GetMapping
    public String getAllPlans(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        WorkoutPlanDto activePlan = workoutPlanService.getActivePlan(userDetails.getUsername());
        List<WorkoutPlanDto> inactivePlans = workoutPlanService.getInactivePlansForUser(userDetails.getUsername());

        model.addAttribute("activePlan", activePlan);
        model.addAttribute("inactivePlans", inactivePlans);

        return "plans";
    }

    @GetMapping("/add")
    public String showAddPlanForm(Model model) {
        if (!model.containsAttribute("planDto")) {
            model.addAttribute("planDto", new WorkoutPlanDto());
        }
        return "plan-add";
    }

    @PostMapping("/add")
    public String addPlan(@Valid @ModelAttribute("planDto") WorkoutPlanDto planDto,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes, @AuthenticationPrincipal UserDetails userDetails) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("planDto", planDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.planDto", bindingResult);
            return "redirect:/workout-plans/add";
        }
        workoutPlanService.createPlan(planDto, userDetails.getUsername());

        BadgeDto awardedBadge = badgeNotificationService.getAndClearLastAwardedBadge();
        redirectAttributes.addFlashAttribute("awardedBadge", awardedBadge);
        
        return "redirect:/workout-plans";
    }

    @GetMapping("/edit/{id}")
    public String showEditPlanForm(@PathVariable("id") UUID id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("planDto", workoutPlanService.getPlanById(id, userDetails.getUsername()));
        return "plan-edit";
    }

    @PostMapping("/edit/{id}")
    public String editPlan(@PathVariable("id") UUID id, @Valid @ModelAttribute("planDto") WorkoutPlanDto planDto,
                           BindingResult bindingResult, RedirectAttributes redirectAttributes,
                           @AuthenticationPrincipal UserDetails userDetails) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("planDto", planDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.planDto", bindingResult);
            redirectAttributes.addFlashAttribute("errorMessage", "Please fix the validation errors below and try again.");
            return "redirect:/workout-plans/edit/" + id;
        }

        workoutPlanService.updatePlan(id, planDto, userDetails.getUsername());
        return "redirect:/workout-plans";
    }

    @PostMapping("/delete/{id}")
    public String deletePlan(@PathVariable("id") UUID id, @AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
        workoutPlanService.deletePlan(id, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("successMessage", "Workout plan deleted successfully.");
        return "redirect:/workout-plans";
    }

    @PostMapping("/set-active/{id}")
    public String setActivePlan(@PathVariable("id") UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        workoutPlanService.setActivePlan(id, userDetails.getUsername());
        return "redirect:/workout-plans";
    }
}