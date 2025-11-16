package com.fittrack.mainapp.controller;

import com.fittrack.mainapp.exceptions.GoalException;
import com.fittrack.mainapp.model.dto.GoalDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        // Arrange
        GoalDto goalDto = new GoalDto(); // ID is null
        GoalException ex = new GoalException("Test error message", goalDto);

        // Act
        String redirectUrl = globalExceptionHandler.handleGoalException(ex, redirectAttributes);

        // Assert
        assertEquals("redirect:/goals/add", redirectUrl);
        assertTrue(redirectAttributes.getFlashAttributes().containsKey("errorMessage"));
        assertTrue(redirectAttributes.getFlashAttributes().containsKey("goalDto"));
        assertEquals("Test error message", redirectAttributes.getFlashAttributes().get("errorMessage"));
    }
}
