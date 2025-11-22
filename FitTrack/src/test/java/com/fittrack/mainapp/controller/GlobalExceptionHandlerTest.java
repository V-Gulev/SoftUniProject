package com.fittrack.mainapp.controller;

import com.fittrack.mainapp.exceptions.*;
import com.fittrack.mainapp.model.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
        redirectAttributes = new RedirectAttributesModelMap();
    }

    @Test
    void testHandleGoalException_ForNewGoal_ShouldAddAttributesAndRedirect() {
        GoalDto goalDto = new GoalDto();
        GoalException ex = new GoalException("Test error message", goalDto);

        String redirectUrl = globalExceptionHandler.handleGoalException(ex, redirectAttributes);

        assertEquals("redirect:/goals/add", redirectUrl);
        assertTrue(redirectAttributes.getFlashAttributes().containsKey("errorMessage"));
        assertTrue(redirectAttributes.getFlashAttributes().containsKey("goalDto"));
        assertEquals("Test error message", redirectAttributes.getFlashAttributes().get("errorMessage"));
    }

    @Test
    void testHandleGoalException_ForExistingGoal_ShouldRedirectToEdit() {
        GoalDto goalDto = new GoalDto();
        goalDto.setId(UUID.randomUUID());
        GoalException ex = new GoalException("Edit error", goalDto);

        String redirectUrl = globalExceptionHandler.handleGoalException(ex, redirectAttributes);

        assertTrue(redirectUrl.startsWith("redirect:/goals/edit/"));
        assertEquals("Edit error", redirectAttributes.getFlashAttributes().get("errorMessage"));
    }

    @Test
    void testHandleWorkoutPlanException_ForNewPlan_ShouldRedirectToAdd() {
        WorkoutPlanDto planDto = new WorkoutPlanDto();
        WorkoutPlanException ex = new WorkoutPlanException("Plan error", planDto);

        String redirectUrl = globalExceptionHandler.handleWorkoutPlanException(ex, redirectAttributes);

        assertEquals("redirect:/workout-plans/add", redirectUrl);
        assertEquals("Plan error", redirectAttributes.getFlashAttributes().get("errorMessage"));
    }

    @Test
    void testHandleWorkoutLogException_ForNewLog_ShouldRedirectToAdd() {
        WorkoutLogDto logDto = new WorkoutLogDto();
        WorkoutLogException ex = new WorkoutLogException("Log error", logDto);

        String redirectUrl = globalExceptionHandler.handleWorkoutLogException(ex, redirectAttributes);

        assertEquals("redirect:/logs/add", redirectUrl);
        assertEquals("Log error", redirectAttributes.getFlashAttributes().get("errorMessage"));
    }

    @Test
    void testHandleRegistrationException_ShouldRedirectToRegister() {
        UserRegistrationDto userDto = new UserRegistrationDto();
        RegistrationException ex = new RegistrationException("Username taken", userDto);

        String redirectUrl = globalExceptionHandler.handleRegistrationException(ex, redirectAttributes);

        assertEquals("redirect:/register", redirectUrl);
        assertEquals("Username taken", redirectAttributes.getFlashAttributes().get("registrationError"));
    }

    @Test
    void testHandleResourceNotFoundException_ShouldReturn404View() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found");

        ModelAndView mav = globalExceptionHandler.handleResourceNotFound(ex);

        assertEquals("error/404", mav.getViewName());
        assertEquals("Resource not found", mav.getModel().get("errorMessage"));
    }

    @Test
    void testHandleUnauthorizedOperationException_ShouldReturn403View() {
        UnauthorizedOperationException ex = new UnauthorizedOperationException("Access denied");

        ModelAndView mav = globalExceptionHandler.handleUnauthorizedOperation(ex);

        assertEquals("error/403", mav.getViewName());
        assertEquals("Access denied", mav.getModel().get("errorMessage"));
    }

    @Test
    void testHandleIllegalArgumentException_ShouldReturn400View() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");

        ModelAndView mav = globalExceptionHandler.handleIllegalArgumentException(ex);

        assertEquals("error/400", mav.getViewName());
        assertEquals("Invalid argument", mav.getModel().get("errorMessage"));
    }

    @Test
    void testHandleRuntimeException_ShouldRedirectWithError() {
        RuntimeException ex = new RuntimeException("Runtime error");
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getHeader("Referer")).thenReturn("/previous-page");

        String redirectUrl = globalExceptionHandler.handleRuntimeException(ex, mockRequest, redirectAttributes);

        assertEquals("redirect:/previous-page", redirectUrl);
        assertTrue(((String) redirectAttributes.getFlashAttributes().get("errorMessage")).contains("Runtime error"));
    }

    @Test
    void testHandleGenericException_ShouldReturnErrorView() {
        Exception ex = new Exception("Generic error");

        ModelAndView mav = globalExceptionHandler.handleGenericException(ex);

        assertEquals("error/error", mav.getViewName());
        assertNotNull(mav.getModel().get("errorMessage"));
    }
}
