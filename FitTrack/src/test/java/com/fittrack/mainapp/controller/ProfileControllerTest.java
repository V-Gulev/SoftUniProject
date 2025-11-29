package com.fittrack.mainapp.controller;

import com.fittrack.mainapp.model.dto.ProfileEditDto;
import com.fittrack.mainapp.model.dto.ProfileViewDto;
import com.fittrack.mainapp.model.entity.User;
import com.fittrack.mainapp.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileService mockProfileService;

    @Test
    @WithMockUser(username = "testuser")
    void testViewProfile_ShouldReturnProfileView() throws Exception {
        ProfileViewDto profileViewDto = new ProfileViewDto();
        User user = new User();
        user.setUsername("testuser");
        profileViewDto.setUserProfile(user);

        when(mockProfileService.buildProfile("testuser")).thenReturn(profileViewDto);

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("userProfile"))
                .andExpect(model().attribute("userProfile", hasProperty("username", is("testuser"))));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testShowEditProfileForm_ShouldReturnEditView() throws Exception {
        ProfileEditDto profileEditDto = new ProfileEditDto();
        profileEditDto.setUsername("testuser");
        profileEditDto.setEmail("test@example.com");

        when(mockProfileService.buildProfileEditDto("testuser")).thenReturn(profileEditDto);

        mockMvc.perform(get("/profile/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile-edit"))
                .andExpect(model().attributeExists("profileDto"))
                .andExpect(model().attribute("profileDto", hasProperty("username", is("testuser"))));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testEditProfile_WhenValidData_ShouldRedirectToProfile() throws Exception {
        when(mockProfileService.updateProfile(any(ProfileEditDto.class), eq("testuser"))).thenReturn(false);

        mockMvc.perform(post("/profile/edit")
                        .with(csrf())
                        .param("username", "newuser")
                        .param("email", "new@email.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testEditProfile_WhenUsernameIsEmpty_ShouldRedirectToEditForm() throws Exception {
        mockMvc.perform(post("/profile/edit")
                        .with(csrf())
                        .param("username", "")
                        .param("email", "new@email.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile/edit"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.profileDto"));
    }
}
