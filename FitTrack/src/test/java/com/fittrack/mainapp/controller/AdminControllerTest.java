package com.fittrack.mainapp.controller;

import com.fittrack.mainapp.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService mockUserService;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testManageUsers_AsAdmin_ShouldReturnUsersView() throws Exception {
        // Arrange
        when(mockUserService.findAllUsers()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"))
                .andExpect(model().attributeExists("users"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testManageUsers_AsUser_ShouldBeForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }
}
