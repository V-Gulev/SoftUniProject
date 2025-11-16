package com.fittrack.mainapp.controller;

import com.fittrack.mainapp.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/login-error")
    public String onLoginError(@RequestParam(value = "username", required = false) String username, RedirectAttributes redirectAttributes) {
        String errorMessage = userService.getLoginErrorMessage(username);

        redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        redirectAttributes.addFlashAttribute("bad_credentials", true);
        return "redirect:/login";
    }
}