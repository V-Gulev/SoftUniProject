package com.fittrack.mainapp.service;

import com.fittrack.mainapp.model.dto.ProfileEditDto;
import com.fittrack.mainapp.model.dto.UserDto;
import com.fittrack.mainapp.model.dto.UserRegistrationDto;
import com.fittrack.mainapp.model.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    void registerUser(UserRegistrationDto registrationDto);

    List<UserDto> findAllUsers();

    void blockUser(UUID userId);

    void unblockUser(UUID userId);

    User findUserByUsername(String username);

    void updateProfile(ProfileEditDto profileDto, String username);

    String getLoginErrorMessage(String username);

    void switchUserRole(UUID id);
}