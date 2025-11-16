package com.fittrack.mainapp.controller;

import com.fittrack.mainapp.exceptions.*;
import com.fittrack.mainapp.exceptions.WorkoutLogException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleResourceNotFound(ResourceNotFoundException ex) {
        LOGGER.error("Resource not found: {}", ex.getMessage());
        ModelAndView modelAndView = new ModelAndView("error/404");
        modelAndView.addObject("errorMessage", ex.getMessage());
        return modelAndView;
    }

    @ExceptionHandler(UnauthorizedOperationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleUnauthorizedOperation(UnauthorizedOperationException ex) {
        LOGGER.error("Unauthorized operation: {}", ex.getMessage());
        ModelAndView modelAndView = new ModelAndView("error/403");
        modelAndView.addObject("errorMessage", ex.getMessage());
        return modelAndView;
    }

    @ExceptionHandler(RegistrationException.class)
    public String handleRegistrationException(RegistrationException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("registrationDto", ex.getRegistrationDto());
        redirectAttributes.addFlashAttribute("registrationError", ex.getMessage());
        return "redirect:/register";
    }

    @ExceptionHandler(WorkoutLogException.class)
    public String handleWorkoutLogException(WorkoutLogException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("logDto", ex.getWorkoutLogDto());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());

        if (ex.getWorkoutLogDto().getId() == null) {
            return "redirect:/logs/add";
        } else {
            return "redirect:/logs/edit/" + ex.getWorkoutLogDto().getId();
        }
    }

    @ExceptionHandler(WorkoutPlanException.class)
    public String handleWorkoutPlanException(WorkoutPlanException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("planDto", ex.getWorkoutPlanDto());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());

        if (ex.getWorkoutPlanDto().getId() == null) {
            return "redirect:/workout-plans/add";
        } else {
            return "redirect:/workout-plans/edit/" + ex.getWorkoutPlanDto().getId();
        }
    }

    @ExceptionHandler(GoalException.class)
    public String handleGoalException(GoalException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("goalDto", ex.getGoalDto());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());

        if (ex.getGoalDto().getId() == null) {
            return "redirect:/goals/add";
        } else {
            return "redirect:/goals/edit/" + ex.getGoalDto().getId();
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleIllegalArgumentException(IllegalArgumentException ex) {
        LOGGER.error("Illegal argument: {}", ex.getMessage());
        ModelAndView modelAndView = new ModelAndView("error/400");
        modelAndView.addObject("errorMessage", ex.getMessage());
        return modelAndView;
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException ex, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        LOGGER.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + ex.getMessage());
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGenericException(Exception ex) {
        LOGGER.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        ModelAndView modelAndView = new ModelAndView("error/error");
        modelAndView.addObject("errorMessage", "An unexpected internal error occurred.");
        return modelAndView;
    }
}