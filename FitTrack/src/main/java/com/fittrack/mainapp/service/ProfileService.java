package com.fittrack.mainapp.service;

import com.fittrack.mainapp.model.dto.ProfileEditDto;
import com.fittrack.mainapp.model.dto.ProfileViewDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface ProfileService {

    ProfileViewDto buildProfile(String username);

    ProfileEditDto buildProfileEditDto(String username);

    boolean updateProfile(ProfileEditDto profileDto, String oldUsername);

    void logout(HttpServletRequest request, HttpServletResponse response);

}