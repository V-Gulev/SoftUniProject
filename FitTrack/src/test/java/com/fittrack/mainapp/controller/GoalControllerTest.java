package com.fittrack.mainapp.controller;

import com.fittrack.mainapp.model.dto.GoalDto;
import com.fittrack.mainapp.model.enums.GoalCategory;
import com.fittrack.mainapp.model.enums.GoalStatus;
import com.fittrack.mainapp.service.GoalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GoalService mockGoalService;

    @Test
    @WithMockUser(username = "testuser")
    void testShowGoalsPage() throws Exception {
        mockMvc.perform(get("/goals"))
                .andExpect(status().isOk())
                .andExpect(view().name("goals"))
                .andExpect(model().attributeExists("goals"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testShowEditGoalForm_ShouldReturnEditView() throws Exception {
        UUID goalId = UUID.randomUUID();
        GoalDto goalDto = new GoalDto();
        goalDto.setId(goalId);
        goalDto.setName("Test Goal");
        goalDto.setCategory(GoalCategory.WEIGHT_LOSS);
        goalDto.setStartValue(80.0);

        when(mockGoalService.getGoalById(goalId, "testuser")).thenReturn(goalDto);

        mockMvc.perform(get("/goals/edit/" + goalId))
                .andExpect(status().isOk())
                .andExpect(view().name("goal-edit"))
                .andExpect(model().attributeExists("goalDto"))
                .andExpect(model().attributeExists("goalUnits"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testAddGoal_WhenValidData_ShouldRedirectToGoals() throws Exception {
        doNothing().when(mockGoalService).createGoal(any(GoalDto.class), eq("testuser"));

        mockMvc.perform(post("/goals/add")
                        .with(csrf())
                        .param("name", "New Test Goal")
                        .param("description", "A description")
                        .param("category", "WEIGHT_LOSS")
                        .param("targetValue", "75")
                        .param("currentValue", "80")
                        .param("unit", "KILOGRAMS")
                        .param("targetDate", "2025-12-31"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/goals"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testAddGoal_WhenNameIsEmpty_ShouldRedirectToForm() throws Exception {
        mockMvc.perform(post("/goals/add")
                        .with(csrf())
                        .param("name", "")
                        .param("category", "WEIGHT_LOSS")
                        .param("targetValue", "75")
                        .param("currentValue", "80")
                        .param("unit", "KILOGRAMS")
                        .param("targetDate", "2025-12-31"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/goals/add"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.goalDto"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testEditGoal_WhenValidData_ShouldRedirect() throws Exception {
        UUID goalId = UUID.randomUUID();
        doNothing().when(mockGoalService).updateGoal(eq(goalId), any(GoalDto.class), eq("testuser"));

        mockMvc.perform(post("/goals/edit/" + goalId)
                        .with(csrf())
                        .param("id", goalId.toString())
                        .param("name", "Updated Name")
                        .param("description", "A new description")
                        .param("category", "WEIGHT_LOSS")
                        .param("status", "ACTIVE")
                        .param("targetValue", "75")
                        .param("currentValue", "78")
                        .param("startValue", "80")
                        .param("unit", "KILOGRAMS")
                        .param("targetDate", "2026-01-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/goals"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testDeleteGoal_ShouldRedirectAndShowSuccess() throws Exception {
        UUID goalId = UUID.randomUUID();
        doNothing().when(mockGoalService).deleteGoal(goalId, "testuser");

        mockMvc.perform(post("/goals/delete/" + goalId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/goals"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCompleteGoal_ShouldRedirectAndShowSuccess() throws Exception {
        UUID goalId = UUID.randomUUID();
        doNothing().when(mockGoalService).completeGoal(goalId, "testuser");

        mockMvc.perform(post("/goals/complete/" + goalId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/goals"))
                .andExpect(flash().attributeExists("successMessage"));
    }
}
