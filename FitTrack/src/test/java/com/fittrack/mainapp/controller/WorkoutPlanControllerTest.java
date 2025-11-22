package com.fittrack.mainapp.controller;

import com.fittrack.mainapp.model.dto.WorkoutPlanDto;
import com.fittrack.mainapp.service.WorkoutPlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WorkoutPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkoutPlanService mockWorkoutPlanService;

    @Test
    @WithMockUser(username = "testuser")
    void testGetAllPlans_ShouldReturnPlansView() throws Exception {
        WorkoutPlanDto plan1 = new WorkoutPlanDto(UUID.randomUUID(), "Plan 1", "Description 1"
                , null, null
                , null, null
                , null, null
                , null);

        WorkoutPlanDto plan2 = new WorkoutPlanDto(UUID.randomUUID(), "Plan 2", "Description 2"
                , null, null
                , null, null
                , null, null
                , null);

        when(mockWorkoutPlanService.getActivePlan("testuser")).thenReturn(null);
        when(mockWorkoutPlanService.getInactivePlansForUser("testuser"))
                .thenReturn(Arrays.asList(plan1, plan2));

        mockMvc.perform(get("/workout-plans"))
                .andExpect(status().isOk())
                .andExpect(view().name("plans"))
                .andExpect(model().attribute("activePlan", is(nullValue())))
                .andExpect(model().attribute("inactivePlans", hasSize(2)))
                .andExpect(model().attribute("inactivePlans", containsInAnyOrder(plan1, plan2)));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testShowEditPlanForm_ShouldReturnEditView() throws Exception {
        UUID planId = UUID.randomUUID();
        WorkoutPlanDto planDto = new WorkoutPlanDto();
        planDto.setId(planId);
        planDto.setName("Test Plan");

        when(mockWorkoutPlanService.getPlanById(planId, "testuser")).thenReturn(planDto);

        mockMvc.perform(get("/workout-plans/edit/" + planId))
                .andExpect(status().isOk())
                .andExpect(view().name("plan-edit"))
                .andExpect(model().attributeExists("planDto"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testAddPlan_WhenNameIsEmpty_ShouldRedirectToForm() throws Exception {
        mockMvc.perform(post("/workout-plans/add")
                        .with(csrf())
                        .param("name", "")
                        .param("description", "Some description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/workout-plans/add"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.planDto"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testDeletePlan_ShouldRedirectWithSuccessMessage() throws Exception {
        UUID planId = UUID.randomUUID();
        doNothing().when(mockWorkoutPlanService).deletePlan(planId, "testuser");

        mockMvc.perform(post("/workout-plans/delete/" + planId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/workout-plans"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testSetActivePlan_ShouldRedirect() throws Exception {
        UUID planId = UUID.randomUUID();
        doNothing().when(mockWorkoutPlanService).setActivePlan(planId, "testuser");

        mockMvc.perform(post("/workout-plans/set-active/" + planId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/workout-plans"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testEditPlan_WhenValidData_ShouldRedirect() throws Exception {
        UUID planId = UUID.randomUUID();
        doNothing().when(mockWorkoutPlanService).updatePlan(eq(planId), any(), eq("testuser"));

        mockMvc.perform(post("/workout-plans/edit/" + planId)
                        .with(csrf())
                        .param("name", "Updated Plan")
                        .param("description", "Updated Desc"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/workout-plans"));
    }
}
