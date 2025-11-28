package com.fittrack.mainapp.service.impl;

import com.fittrack.mainapp.model.entity.Role;
import com.fittrack.mainapp.model.entity.User;
import com.fittrack.mainapp.model.enums.RoleEnum;
import com.fittrack.mainapp.repository.RoleRepository;
import com.fittrack.mainapp.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(UserRepository userRepository, RoleRepository roleRepository,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            OAuth2Error oauth2Error = new OAuth2Error("email_not_found",
                    "Email not found from OAuth2 provider", null);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.getDescription());
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> registerNewUser(oAuth2User));

        Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .collect(Collectors.toSet());

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("username", user.getUsername());

        return new DefaultOAuth2User(authorities, attributes, "username");
    }

    private User registerNewUser(OAuth2User oAuth2User) {
        User user = new User();
        String email = oAuth2User.getAttribute("email");
        String username = oAuth2User.getAttribute("login");
        if (username == null) {
            username = oAuth2User.getAttribute("name");
        }
        if (username == null) {
            username = email;
        }
        if (username != null && username.length() > 50) {
            username = username.substring(0, 50);
        }
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

        Role userRole = roleRepository.findByName(RoleEnum.USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        user.setRoles(new HashSet<>(Collections.singletonList(userRole)));

        return userRepository.save(user);
    }
}
