package com.fittrack.mainapp.controller;

import com.fittrack.mainapp.badge.dto.BadgeDto;
import com.fittrack.mainapp.model.dto.GoalDto;
import com.fittrack.mainapp.model.enums.GoalCategory;
import com.fittrack.mainapp.model.enums.GoalStatus;
import com.fittrack.mainapp.model.enums.GoalUnit;
import com.fittrack.mainapp.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequestMapping("/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @GetMapping
    public String showGoals(Model model, Principal principal) {
        model.addAttribute("goals", goalService.getGoalsForUser(principal.getName()));
        return "goals";
    }

    @GetMapping("/add")
    public String showAddGoalForm(Model model) {
        if (!model.containsAttribute("goalDto"))
            model.addAttribute("goalDto", new GoalDto());
        model.addAttribute("goalUnits", GoalUnit.values());
        model.addAttribute("goalCategories", GoalCategory.values());
        model.addAttribute("goalStatuses", GoalStatus.values());
        return "goal-add";
    }

    @PostMapping("/add")
    public String addGoal(@Valid @ModelAttribute("goalDto") GoalDto goalDto,
                          BindingResult bindingResult, RedirectAttributes redirectAttributes,
                          Principal principal) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("goalDto", goalDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.goalDto", bindingResult);
            return "redirect:/goals/add";
        }

        goalService.createGoal(goalDto, principal.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Goal created successfully!");

        BadgeDto awardedBadge = goalService.getAndClearLastAwardedBadge();
        redirectAttributes.addFlashAttribute("awardedBadge", awardedBadge);
        return "redirect:/goals";
    }

    @GetMapping("/edit/{id}")
    public String showEditGoalForm(@PathVariable("id") UUID id, Model model, Principal principal) {
        model.addAttribute("goalDto", goalService.getGoalById(id, principal.getName()));
        model.addAttribute("goalUnits", GoalUnit.values());
        model.addAttribute("goalCategories", GoalCategory.values());
        model.addAttribute("goalStatuses", GoalStatus.values());
        return "goal-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateGoal(@PathVariable("id") UUID id,
                             @Valid @ModelAttribute("goalDto") GoalDto goalDto,
                             BindingResult bindingResult, RedirectAttributes redirectAttributes,
                             Principal principal) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("goalDto", goalDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.goalDto", bindingResult);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Please fix the validation errors below and try again.");
            return "redirect:/goals/edit/" + id;
        }

        goalService.updateGoal(id, goalDto, principal.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Goal updated successfully!");

        BadgeDto awardedBadge = goalService.getAndClearLastAwardedBadge();
        redirectAttributes.addFlashAttribute("awardedBadge", awardedBadge);
        return "redirect:/goals";
    }

    @PostMapping("/delete/{id}")
    public String deleteGoal(@PathVariable("id") UUID id,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {

        goalService.deleteGoal(id, principal.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Goal deleted successfully.");
        return "redirect:/goals";
    }

    @PostMapping("/complete/{id}")
    public String completeGoal(@PathVariable("id") UUID id,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {

        goalService.completeGoal(id, principal.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Goal marked as complete!");

        BadgeDto awardedBadge = goalService.getAndClearLastAwardedBadge();
        redirectAttributes.addFlashAttribute("awardedBadge", awardedBadge);
        return "redirect:/goals";
    }
}