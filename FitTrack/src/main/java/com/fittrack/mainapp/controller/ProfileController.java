package com.fittrack.mainapp.controller;

import com.fittrack.mainapp.model.dto.ProfileEditDto;
import com.fittrack.mainapp.model.dto.ProfileViewDto;
import com.fittrack.mainapp.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public String viewProfile(Model model, Principal principal) {
        ProfileViewDto profile = profileService.buildProfile(principal.getName());
        model.addAttribute("userProfile", profile.getUserProfile());
        model.addAttribute("totalGoals", profile.getTotalGoals());
        model.addAttribute("activeGoals", profile.getActiveGoals());
        model.addAttribute("completedGoals", profile.getCompletedGoals());
        model.addAttribute("totalPlans", profile.getTotalPlans());
        model.addAttribute("totalLogs", profile.getTotalLogs());
        model.addAttribute("activePlan", profile.getActivePlan());
        model.addAttribute("badges", profile.getBadges());

        return "profile";
    }

    @GetMapping("/edit")
    public String showEditProfileForm(Model model, Principal principal) {
        if (!model.containsAttribute("profileDto")) {
            ProfileEditDto profileDto = profileService.buildProfileEditDto(principal.getName());
            model.addAttribute("profileDto", profileDto);
        }
        return "profile-edit";
    }

    @PostMapping("/edit")
    public String editProfile(@Valid @ModelAttribute("profileDto") ProfileEditDto profileDto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Principal principal,
                              HttpServletRequest request,
                              HttpServletResponse response) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("profileDto", profileDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.profileDto",
                    bindingResult);
            return "redirect:/profile/edit";
        }

        boolean credentialsChanged = profileService.updateProfile(profileDto, principal.getName());

        if (credentialsChanged) {
            profileService.logout(request, response);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Profile updated successfully! Please log in again with your updated credentials.");
            return "redirect:/login";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return "redirect:/profile";
    }
}