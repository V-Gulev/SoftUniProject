package com.fittrack.mainapp.controller;

import com.fittrack.mainapp.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "admin/users";
    }

    @PostMapping("/users/block/{id}")
    public String blockUser(@PathVariable("id") UUID id) {
        userService.blockUser(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/unblock/{id}")
    public String unblockUser(@PathVariable("id") UUID id) {
        userService.unblockUser(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/switch-role/{id}")
    public String switchUserRole(@PathVariable("id") UUID id) {
        userService.switchUserRole(id);
        return "redirect:/admin/users";
    }
}
