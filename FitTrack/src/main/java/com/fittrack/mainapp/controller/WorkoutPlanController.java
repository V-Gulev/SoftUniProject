package com.fittrack.mainapp.controller;

import com.fittrack.mainapp.badge.dto.BadgeDto;
import com.fittrack.mainapp.model.dto.WorkoutPlanDto;
import com.fittrack.mainapp.badge.service.BadgeNotificationService;
import com.fittrack.mainapp.service.WorkoutPlanService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/workout-plans")
public class WorkoutPlanController {

    private final WorkoutPlanService workoutPlanService;
    private final BadgeNotificationService badgeNotificationService;

    public WorkoutPlanController(WorkoutPlanService workoutPlanService,
                                 BadgeNotificationService badgeNotificationService) {
        this.workoutPlanService = workoutPlanService;
        this.badgeNotificationService = badgeNotificationService;
    }

    @GetMapping
    public String getAllPlans(Model model, Principal principal) {
        WorkoutPlanDto activePlan = workoutPlanService.getActivePlan(principal.getName());
        List<WorkoutPlanDto> inactivePlans = workoutPlanService.getInactivePlansForUser(principal.getName());

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
                          RedirectAttributes redirectAttributes, Principal principal) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("planDto", planDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.planDto", bindingResult);
            return "redirect:/workout-plans/add";
        }
        workoutPlanService.createPlan(planDto, principal.getName());

        BadgeDto awardedBadge = badgeNotificationService.getAndClearLastAwardedBadge();
        redirectAttributes.addFlashAttribute("awardedBadge", awardedBadge);

        return "redirect:/workout-plans";
    }

    @GetMapping("/edit/{id}")
    public String showEditPlanForm(@PathVariable("id") UUID id, Model model, Principal principal) {
        model.addAttribute("planDto", workoutPlanService.getPlanById(id, principal.getName()));
        return "plan-edit";
    }

    @PostMapping("/edit/{id}")
    public String editPlan(@PathVariable("id") UUID id, @Valid @ModelAttribute("planDto") WorkoutPlanDto planDto,
                           BindingResult bindingResult, RedirectAttributes redirectAttributes,
                           Principal principal) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("planDto", planDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.planDto", bindingResult);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Please fix the validation errors below and try again.");
            return "redirect:/workout-plans/edit/" + id;
        }

        workoutPlanService.updatePlan(id, planDto, principal.getName());
        return "redirect:/workout-plans";
    }

    @PostMapping("/delete/{id}")
    public String deletePlan(@PathVariable("id") UUID id, Principal principal, RedirectAttributes redirectAttributes) {
        workoutPlanService.deletePlan(id, principal.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Workout plan deleted successfully.");
        return "redirect:/workout-plans";
    }

    @PostMapping("/set-active/{id}")
    public String setActivePlan(@PathVariable("id") UUID id, Principal principal) {
        workoutPlanService.setActivePlan(id, principal.getName());
        return "redirect:/workout-plans";
    }
}