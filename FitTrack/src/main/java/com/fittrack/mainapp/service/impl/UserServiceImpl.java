package com.fittrack.mainapp.service.impl;

import com.fittrack.mainapp.exceptions.RegistrationException;
import com.fittrack.mainapp.exceptions.ResourceNotFoundException;
import com.fittrack.mainapp.model.dto.ProfileEditDto;
import com.fittrack.mainapp.model.dto.UserDto;
import com.fittrack.mainapp.model.dto.UserRegistrationDto;
import com.fittrack.mainapp.model.entity.Role;
import com.fittrack.mainapp.model.enums.RoleEnum;
import com.fittrack.mainapp.model.entity.User;
import com.fittrack.mainapp.repository.RoleRepository;
import com.fittrack.mainapp.repository.UserRepository;
import com.fittrack.mainapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void registerUser(UserRegistrationDto registrationDto) {

        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            throw new RegistrationException("Passwords do not match.", registrationDto);
        }
        if (userRepository.findByUsername(registrationDto.getUsername()).isPresent()) {
            throw new RegistrationException("Username is already taken.", registrationDto);
        }
        if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
            throw new RegistrationException("Email is already registered.", registrationDto);
        }

        User newUser = new User();
        newUser.setUsername(registrationDto.getUsername());
        newUser.setEmail(registrationDto.getEmail());
        newUser.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

        Role userRole = roleRepository.findByName(RoleEnum.USER)
                .orElseThrow(() -> new IllegalStateException("USER role not found! Please ensure roles are seeded in the database."));
        newUser.setRoles(new HashSet<>(Set.of(userRole)));

        userRepository.save(newUser);
        LOGGER.info("User with username '{}' registered successfully.", newUser.getUsername());
    }

    @Override
    public List<UserDto> findAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void blockUser(UUID userId) {
        User user = findUserById(userId);
        user.setBlocked(true);
        userRepository.save(user);
        LOGGER.warn("Admin has BLOCKED user with ID '{}'", userId);
    }

    @Override
    public void unblockUser(UUID userId) {
        User user = findUserById(userId);
        user.setBlocked(false);
        userRepository.save(user);
        LOGGER.warn("Admin has UNBLOCKED user with ID '{}'", userId);
    }

    @Override
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    private UserDto mapToUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setBlocked(user.isBlocked());
        userDto.setRoles(user.getRoles());
        return userDto;
    }

    @Override
    @Transactional
    public void updateProfile(ProfileEditDto profileDto, String username) {
        User user = findUserByUsername(username);

        if (profileDto.getUsername() != null && !profileDto.getUsername().equals(user.getUsername())) {
            Optional<User> existingUser = userRepository.findByUsername(profileDto.getUsername());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
                throw new IllegalArgumentException("Username is already taken.");
            }
            user.setUsername(profileDto.getUsername());
            LOGGER.info("Username updated from '{}' to '{}'", username, profileDto.getUsername());
        }

        user.setEmail(profileDto.getEmail());

        if (profileDto.getNewPassword() != null && !profileDto.getNewPassword().isEmpty()) {
            if (profileDto.getNewPassword().length() < 5) {
                throw new IllegalArgumentException("New password must be at least 5 characters long.");
            }
            if (!passwordEncoder.matches(profileDto.getOldPassword(), user.getPassword())) {
                throw new IllegalArgumentException("Incorrect old password.");
            }
            if (!profileDto.getNewPassword().equals(profileDto.getConfirmNewPassword())) {
                throw new IllegalArgumentException("New passwords do not match.");
            }
            user.setPassword(passwordEncoder.encode(profileDto.getNewPassword()));
            LOGGER.info("Password updated for user '{}'", user.getUsername());
        }

        userRepository.save(user);
        LOGGER.info("Profile updated for user '{}'", user.getUsername());
    }

    @Override
    public String getLoginErrorMessage(String username) {
        if (username == null || username.isEmpty()) {
            return "Invalid username or password.";
        }

        return userRepository.findByUsername(username)
                .map(user -> user.isBlocked()
                        ? "Your account has been blocked. Please contact an administrator."
                        : "Invalid password. Please check your password and try again.")
                .orElse("Invalid username. The username you entered does not exist.");
    }

    @Override
    @Transactional
    public void switchUserRole(UUID id) {
        User user = findUserById(id);

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleEnum.ADMIN);

        RoleEnum newRoleEnum = isAdmin ? RoleEnum.USER : RoleEnum.ADMIN;
        Role newRole = roleRepository.findByName(newRoleEnum)
                .orElseThrow(() -> new IllegalStateException(newRoleEnum + " role not found!"));

        user.getRoles().clear();
        user.getRoles().add(newRole);

        userRepository.save(user);
        LOGGER.info("User {}'s role has been switched to {}", user.getUsername(), newRole.getName());
    }
}