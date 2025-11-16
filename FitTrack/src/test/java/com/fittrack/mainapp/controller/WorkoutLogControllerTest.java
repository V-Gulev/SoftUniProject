package com.fittrack.mainapp.controller;

import com.fittrack.mainapp.model.dto.WorkoutLogDto;
import com.fittrack.mainapp.service.WorkoutLogService;
import com.fittrack.mainapp.service.WorkoutPlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
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
class WorkoutLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkoutLogService mockWorkoutLogService; // Changed from ApplicationService
    @MockBean
    private WorkoutPlanService mockWorkoutPlanService;

    @Test
    @WithMockUser(username = "testuser")
    void testShowLogWorkoutForm_ShouldReturnFormView() throws Exception {
        when(mockWorkoutPlanService.getPlansForUser("testuser")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/logs/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("log-add"))
                .andExpect(model().attributeExists("logDto"))
                .andExpect(model().attributeExists("workoutPlans"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testShowEditLogForm_ShouldReturnEditView() throws Exception {
        UUID logId = UUID.randomUUID();
        WorkoutLogDto logDto = new WorkoutLogDto();
        logDto.setId(logId);
        logDto.setDate(LocalDate.now());
        logDto.setHours(1);
        logDto.setMinutes(0);

        when(mockWorkoutLogService.getLogById(logId, "testuser")).thenReturn(logDto); // Changed call
        when(mockWorkoutPlanService.getPlansForUser("testuser")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/logs/edit/" + logId))
                .andExpect(status().isOk())
                .andExpect(view().name("log-edit"))
                .andExpect(model().attributeExists("logDto"))
                .andExpect(model().attributeExists("workoutPlans"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testLogWorkout_WhenValidData_ShouldRedirectToHistory() throws Exception {
        doNothing().when(mockWorkoutLogService).logWorkout(any(WorkoutLogDto.class), eq("testuser")); // Changed call

        mockMvc.perform(post("/logs/add")
                        .with(csrf())
                        .param("date", "2024-01-01")
                        .param("hours", "1")
                        .param("minutes", "15")
                        .param("description", "A good run"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/logs/history"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testLogWorkout_WhenDateIsNull_ShouldRedirectToForm() throws Exception {
        mockMvc.perform(post("/logs/add")
                        .with(csrf())
                        .param("date", "") // Invalid empty date
                        .param("hours", "1")
                        .param("minutes", "15"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/logs/add"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.logDto"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateLog_WhenValidData_ShouldRedirectToHistory() throws Exception {
        UUID logId = UUID.randomUUID();
        doNothing().when(mockWorkoutLogService).updateLog(any(WorkoutLogDto.class), eq("testuser"));

        mockMvc.perform(post("/logs/edit/" + logId)
                        .with(csrf())
                        .param("date", "2024-01-01")
                        .param("hours", "1")
                        .param("minutes", "15")
                        .param("description", "Updated run"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/logs/history"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testDeleteLog_ShouldRedirectToHistory() throws Exception {
        UUID logId = UUID.randomUUID();
        doNothing().when(mockWorkoutLogService).deleteLog(logId, "testuser");

        mockMvc.perform(post("/logs/delete/" + logId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/logs/history"))
                .andExpect(flash().attributeExists("successMessage"));
    }
}
