package com.fittrack.mainapp.controller;

import com.fittrack.mainapp.exceptions.RegistrationException;
import com.fittrack.mainapp.model.dto.UserRegistrationDto;
import com.fittrack.mainapp.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService mockUserService;

    @Test
    void testRegisterUser_Success() throws Exception {
        doNothing().when(mockUserService).registerUser(any(UserRegistrationDto.class));

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "testuser")
                        .param("email", "test@test.com")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("registrationSuccess"));
    }

    @Test
    void testRegisterUser_WhenServiceThrowsException_ShouldRedirectToRegister() throws Exception {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("testuser");
        
        doThrow(new RegistrationException("Username is already taken.", dto))
                .when(mockUserService).registerUser(any(UserRegistrationDto.class));

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "testuser")
                        .param("email", "test@test.com")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("registrationError", "Username is already taken."))
                .andExpect(flash().attributeExists("registrationDto"));
    }
}
