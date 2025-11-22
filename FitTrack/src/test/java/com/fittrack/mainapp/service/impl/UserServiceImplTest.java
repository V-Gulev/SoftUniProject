package com.fittrack.mainapp.service.impl;

import com.fittrack.mainapp.exceptions.RegistrationException;
import com.fittrack.mainapp.model.dto.ProfileEditDto;
import com.fittrack.mainapp.model.dto.UserRegistrationDto;
import com.fittrack.mainapp.model.entity.Role;
import com.fittrack.mainapp.model.enums.RoleEnum;
import com.fittrack.mainapp.model.entity.User;
import com.fittrack.mainapp.repository.RoleRepository;
import com.fittrack.mainapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private UserServiceImpl userService;

    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private RoleRepository mockRoleRepository;
    @Mock
    private PasswordEncoder mockPasswordEncoder;

    private User testUser;
    private final String username = "testuser";
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(mockRoleRepository, mockUserRepository, mockPasswordEncoder);
        testUser = new User();
        testUser.setId(userId);
        testUser.setUsername(username);
        testUser.setEmail("test@example.com");
        testUser.setPassword(mockPasswordEncoder.encode("oldPassword"));
    }

    @Test
    void testRegisterUser_Success() {
        UserRegistrationDto registrationDto = new UserRegistrationDto();
        registrationDto.setUsername("newUser");
        registrationDto.setEmail("new@test.com");
        registrationDto.setPassword("password123");
        registrationDto.setConfirmPassword("password123");

        Role userRole = new Role();
        userRole.setName(RoleEnum.USER);

        when(mockUserRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(mockUserRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(mockRoleRepository.findByName(RoleEnum.USER)).thenReturn(Optional.of(userRole));
        when(mockPasswordEncoder.encode("password123")).thenReturn("encodedPassword");

        userService.registerUser(registrationDto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(mockUserRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("newUser", savedUser.getUsername());
        assertEquals("new@test.com", savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertTrue(savedUser.getRoles().contains(userRole));
    }

    @Test
    void testRegisterUser_WhenUsernameExists_ShouldThrowException() {
        UserRegistrationDto registrationDto = new UserRegistrationDto();
        registrationDto.setUsername("existingUser");
        registrationDto.setPassword("password123");
        registrationDto.setConfirmPassword("password123");

        when(mockUserRepository.findByUsername("existingUser")).thenReturn(Optional.of(new User()));

        RegistrationException exception = assertThrows(RegistrationException.class, () -> {
            userService.registerUser(registrationDto);
        });

        assertEquals("Username is already taken.", exception.getMessage());
    }

    @Test
    void testRegisterUser_WhenPasswordsDoNotMatch_ShouldThrowException() {
        UserRegistrationDto registrationDto = new UserRegistrationDto();
        registrationDto.setUsername("newUser");
        registrationDto.setPassword("password123");
        registrationDto.setConfirmPassword("password456");

        RegistrationException exception = assertThrows(RegistrationException.class, () -> {
            userService.registerUser(registrationDto);
        });

        assertEquals("Passwords do not match.", exception.getMessage());
    }

    @Test
    void testBlockUser_ShouldSetBlockedToTrue() {
        User user = new User();
        user.setBlocked(false);

        when(mockUserRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.blockUser(userId);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(mockUserRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertTrue(savedUser.isBlocked());
    }

    @Test
    void testUnblockUser_ShouldSetBlockedToFalse() {
        User user = new User();
        user.setBlocked(true);

        when(mockUserRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.unblockUser(userId);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(mockUserRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertFalse(savedUser.isBlocked());
    }

    @Test
    void testGetLoginErrorMessage_WhenUserIsBlocked() {
        String username = "blockedUser";
        User user = new User();
        user.setBlocked(true);

        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(user));

        String message = userService.getLoginErrorMessage(username);

        assertEquals("Your account has been blocked. Please contact an administrator.", message);
    }

    @Test
    void testGetLoginErrorMessage_WhenUserNotFound() {
        String username = "unknownUser";
        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.empty());

        String message = userService.getLoginErrorMessage(username);

        assertEquals("Invalid username. The username you entered does not exist.", message);
    }

    @Test
    void testUpdateProfile_Success_NoPasswordChange() {
        ProfileEditDto profileDto = new ProfileEditDto();
        profileDto.setUsername("updatedUser");
        profileDto.setEmail("updated@example.com");

        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(mockUserRepository.findByUsername("updatedUser")).thenReturn(Optional.empty());

        userService.updateProfile(profileDto, username);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(mockUserRepository).save(userCaptor.capture());
        User updatedUser = userCaptor.getValue();

        assertEquals("updatedUser", updatedUser.getUsername());
        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals(testUser.getPassword(), updatedUser.getPassword());
    }

    @Test
    void testUpdateProfile_Success_PasswordChange() {
        ProfileEditDto profileDto = new ProfileEditDto();
        profileDto.setUsername(username);
        profileDto.setEmail("updated@example.com");
        profileDto.setOldPassword("oldPassword");
        profileDto.setNewPassword("newPassword123");
        profileDto.setConfirmNewPassword("newPassword123");

        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(mockPasswordEncoder.matches("oldPassword", testUser.getPassword())).thenReturn(true);
        when(mockPasswordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");

        userService.updateProfile(profileDto, username);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(mockUserRepository).save(userCaptor.capture());
        User updatedUser = userCaptor.getValue();

        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals("encodedNewPassword", updatedUser.getPassword());
    }

    @Test
    void testUpdateProfile_ThrowsException_UsernameTaken() {
        ProfileEditDto profileDto = new ProfileEditDto();
        profileDto.setUsername("takenUser");
        profileDto.setEmail("updated@example.com");

        User existingUserWithSameName = new User();
        existingUserWithSameName.setId(UUID.randomUUID());

        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(mockUserRepository.findByUsername("takenUser")).thenReturn(Optional.of(existingUserWithSameName));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateProfile(profileDto, username);
        });

        assertEquals("Username is already taken.", exception.getMessage());
    }

    @Test
    void testUpdateProfile_ThrowsException_IncorrectOldPassword() {
        ProfileEditDto profileDto = new ProfileEditDto();
        profileDto.setUsername(username);
        profileDto.setEmail("updated@example.com");
        profileDto.setOldPassword("wrongPassword");
        profileDto.setNewPassword("newPassword123");
        profileDto.setConfirmNewPassword("newPassword123");

        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(mockPasswordEncoder.matches("wrongPassword", testUser.getPassword())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateProfile(profileDto, username);
        });

        assertEquals("Incorrect old password.", exception.getMessage());
    }

    @Test
    void testUpdateProfile_ThrowsException_NewPasswordsMismatch() {
        ProfileEditDto profileDto = new ProfileEditDto();
        profileDto.setUsername(username);
        profileDto.setEmail("updated@example.com");
        profileDto.setOldPassword("oldPassword");
        profileDto.setNewPassword("newPassword123");
        profileDto.setConfirmNewPassword("mismatchedPassword");

        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(mockPasswordEncoder.matches("oldPassword", testUser.getPassword())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateProfile(profileDto, username);
        });

        assertEquals("New passwords do not match.", exception.getMessage());
    }

    @Test
    void testUpdateProfile_ThrowsException_NewPasswordTooShort() {
        ProfileEditDto profileDto = new ProfileEditDto();
        profileDto.setUsername(username);
        profileDto.setEmail("updated@example.com");
        profileDto.setOldPassword("oldPassword");
        profileDto.setNewPassword("four");
        profileDto.setConfirmNewPassword("four");

        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateProfile(profileDto, username);
        });

        assertEquals("New password must be at least 5 characters long.", exception.getMessage());
    }

    @Test
    void testRegisterUser_WhenEmailExists_ShouldThrowException() {
        UserRegistrationDto registrationDto = new UserRegistrationDto();
        registrationDto.setUsername("newUser");
        registrationDto.setEmail("existing@test.com");
        registrationDto.setPassword("password123");
        registrationDto.setConfirmPassword("password123");

        when(mockUserRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(mockUserRepository.findByEmail("existing@test.com")).thenReturn(Optional.of(new User()));

        RegistrationException exception = assertThrows(RegistrationException.class, () -> {
            userService.registerUser(registrationDto);
        });

        assertEquals("Email is already registered.", exception.getMessage());
    }

    @Test
    void testFindUserByUsername_ShouldReturnUser() {
        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        User result = userService.findUserByUsername(username);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
    }

    @Test
    void testSwitchUserRole_ShouldToggleRole() {
        Role userRole = new Role();
        userRole.setName(RoleEnum.USER);
        testUser.setRoles(new HashSet<>(Set.of(userRole)));

        Role adminRole = new Role();
        adminRole.setName(RoleEnum.ADMIN);

        when(mockUserRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(mockRoleRepository.findByName(RoleEnum.ADMIN)).thenReturn(Optional.of(adminRole));

        userService.switchUserRole(userId);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(mockUserRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertTrue(savedUser.getRoles().contains(adminRole));
        assertFalse(savedUser.getRoles().contains(userRole));
    }

    @Test
    void testFindAllUsers_ShouldReturnUserDtos() {
        when(mockUserRepository.findAll()).thenReturn(java.util.Collections.singletonList(testUser));

        java.util.List<com.fittrack.mainapp.model.dto.UserDto> result = userService.findAllUsers();

        assertEquals(1, result.size());
        assertEquals(username, result.get(0).getUsername());
    }
}
