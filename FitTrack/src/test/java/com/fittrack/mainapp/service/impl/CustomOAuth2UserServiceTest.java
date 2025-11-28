package com.fittrack.mainapp.service.impl;

import com.fittrack.mainapp.model.entity.Role;
import com.fittrack.mainapp.model.entity.User;
import com.fittrack.mainapp.model.enums.RoleEnum;
import com.fittrack.mainapp.repository.RoleRepository;
import com.fittrack.mainapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestOperations;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RestOperations restOperations;

    private CustomOAuth2UserService service;

    private ClientRegistration clientRegistration;
    private OAuth2UserRequest oAuth2UserRequest;

    @BeforeEach
    void setUp() {
        service = new CustomOAuth2UserService(userRepository, roleRepository, passwordEncoder);

        service.setRestOperations(restOperations);

        clientRegistration = ClientRegistration.withRegistrationId("google")
                .clientId("test-client-id")
                .clientSecret("test-client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .redirectUri("http://localhost/login/oauth2/code/google")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://api.googleapis.com/userinfo")
                .userNameAttributeName("sub")
                .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "test-token",
                Instant.now(),
                Instant.now().plusSeconds(60)
        );

        oAuth2UserRequest = new OAuth2UserRequest(clientRegistration, accessToken);
    }

    @Test
    void loadUser_WhenUserExists_ShouldReturnUserWithRoles() {
        String email = "test@example.com";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "123456789");
        attributes.put("email", email);
        attributes.put("name", "Test User");

        mockExternalUserInfoSuccess(attributes);

        User existingUser = new User();
        existingUser.setUsername("existingUser");
        existingUser.setEmail(email);
        Role userRole = new Role();
        userRole.setName(RoleEnum.USER);
        existingUser.setRoles(new HashSet<>(Collections.singletonList(userRole)));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        OAuth2User result = service.loadUser(oAuth2UserRequest);

        assertNotNull(result);
        assertEquals("existingUser", result.getAttribute("username"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loadUser_WhenUserDoesNotExist_ShouldCreateNewUser() {
        String email = "new@example.com";
        String name = "New User";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "987654321");
        attributes.put("email", email);
        attributes.put("name", name);

        mockExternalUserInfoSuccess(attributes);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Role userRole = new Role();
        userRole.setName(RoleEnum.USER);
        when(roleRepository.findByName(RoleEnum.USER)).thenReturn(Optional.of(userRole));

        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OAuth2User result = service.loadUser(oAuth2UserRequest);

        assertNotNull(result);
        assertEquals(email, result.getAttribute("email"));

        assertEquals(name, result.getAttribute("username"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(email, savedUser.getEmail());
        assertEquals(name, savedUser.getUsername());
        assertNotNull(savedUser.getPassword());
    }

    @Test
    void loadUser_WhenEmailIsMissing_ShouldThrowException() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "12345");

        mockExternalUserInfoSuccess(attributes);

        OAuth2AuthenticationException exception = assertThrows(OAuth2AuthenticationException.class, () -> {
            service.loadUser(oAuth2UserRequest);
        });

        assertEquals("Email not found from OAuth2 provider", exception.getMessage());
        verify(userRepository, never()).findByEmail(anyString());
    }


    private void mockExternalUserInfoSuccess(Map<String, Object> attributes) {
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(attributes, HttpStatus.OK);

        when(restOperations.exchange(any(RequestEntity.class), any(ParameterizedTypeReference.class))
        ).thenReturn(responseEntity);
    }
}